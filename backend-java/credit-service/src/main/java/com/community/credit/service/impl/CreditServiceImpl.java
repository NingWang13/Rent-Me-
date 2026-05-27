package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.entity.CreditLog;
import com.community.credit.mapper.CreditLogMapper;
import com.community.credit.service.CreditService;
import com.community.user.entity.User;
import com.community.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreditServiceImpl extends ServiceImpl<CreditLogMapper, CreditLog> implements CreditService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void addCredit(Long userId, int amount, String type, String source, String sourceId, String description) {
        // 获取用户当前积分
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        int newBalance = user.getCreditScore() + amount;
        if (newBalance < 0) {
            throw new RuntimeException("积分不足");
        }
        
        // 更新用户积分
        user.setCreditScore(newBalance);
        userMapper.updateById(user);
        
        // 记录积分变动
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeAmount(amount);
        log.setBalance(newBalance);
        log.setType(type);
        log.setSource(source);
        log.setSourceId(sourceId);
        log.setDescription(description);
        
        this.save(log);
    }

    @Override
    public List<CreditLog> getCreditLogs(int page, int size, Long userId) {
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditLog::getUserId, userId);
        wrapper.orderByDesc(CreditLog::getCreateTime);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public long countCreditLogs(Long userId) {
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditLog::getUserId, userId);
        return this.count(wrapper);
    }

    @Override
    public int getUserBalance(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getCreditScore() : 0;
    }
}
