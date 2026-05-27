package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.common.exception.ErrorCode;
import com.community.common.redis.RedisLockService;
import com.community.common.response.PageResponse;
import com.community.credit.entity.CreditLog;
import com.community.credit.mapper.CreditLogMapper;
import com.community.credit.service.CreditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CreditServiceImpl extends ServiceImpl<CreditLogMapper, CreditLog> implements CreditService {

    private final RedisLockService redisLockService;

    public CreditServiceImpl(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    @Override
    public int getCreditBalance(Long userId) {
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditLog::getUserId, userId);
        wrapper.orderByDesc(CreditLog::getCreateTime);
        wrapper.last("LIMIT 1");
        CreditLog lastLog = this.getOne(wrapper);
        return lastLog != null ? lastLog.getBalanceAfter() : 0;
    }

    @Override
    public PageResponse<CreditLog> getCreditLogs(int page, int size, Long userId) {
        Page<CreditLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditLog::getUserId, userId);
        wrapper.orderByDesc(CreditLog::getCreateTime);
        Page<CreditLog> result = this.page(pageParam, wrapper);
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditLog checkin(Long userId) {
        String lockKey = "credit:checkin:lock:" + userId;
        String lockValue = String.valueOf(System.currentTimeMillis());

        boolean locked = false;
        try {
            locked = redisLockService.tryLock(lockKey, lockValue, 5);
            if (!locked) {
                throw new BusinessException(500, "系统繁忙，请稍后重试");
            }

            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CreditLog::getUserId, userId);
            wrapper.eq(CreditLog::getType, 1);
            wrapper.ge(CreditLog::getCreateTime, startOfDay);
            wrapper.lt(CreditLog::getCreateTime, endOfDay);
            long count = this.count(wrapper);

            if (count > 0) {
                throw new BusinessException(ErrorCode.CHECKIN_DUPLICATE);
            }

            int currentBalance = getCreditBalance(userId);
            int checkinAmount = 10;
            int newBalance = currentBalance + checkinAmount;

            CreditLog log = new CreditLog();
            log.setUserId(userId);
            log.setChangeAmount(checkinAmount);
            log.setBalanceAfter(newBalance);
            log.setType(1);
            log.setDescription("每日签到");
            this.save(log);
            return log;
        } finally {
            if (locked) {
                redisLockService.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditLog addCredit(Long userId, int amount, int type, Long relatedId, String relatedType, String description) {
        int currentBalance = getCreditBalance(userId);
        int newBalance = currentBalance + amount;

        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeAmount(amount);
        log.setBalanceAfter(newBalance);
        log.setType(type);
        log.setRelatedId(relatedId);
        log.setRelatedType(relatedType);
        log.setDescription(description);
        this.save(log);
        return log;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditLog deductCredit(Long userId, int amount, int type, Long relatedId, String relatedType, String description) {
        int currentBalance = getCreditBalance(userId);
        if (currentBalance < amount) {
            throw new BusinessException(ErrorCode.CREDIT_NOT_ENOUGH);
        }
        int newBalance = currentBalance - amount;

        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeAmount(-amount);
        log.setBalanceAfter(newBalance);
        log.setType(type);
        log.setRelatedId(relatedId);
        log.setRelatedType(relatedType);
        log.setDescription(description);
        this.save(log);
        return log;
    }
}
