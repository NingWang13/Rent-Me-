package com.community.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.order.entity.Wish;
import com.community.order.mapper.WishMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class WishService extends ServiceImpl<WishMapper, Wish> {

    @Transactional(rollbackFor = Exception.class)
    public void claimWish(Long wishId, Long userId) {
        Wish wish = getById(wishId);
        if (wish == null) {
            throw new BusinessException(404, "心愿不存在");
        }
        if (wish.getStatus() != 1) {
            throw new BusinessException(400, "心愿已被认领，无法重复认领");
        }
        if (wish.getUserId().equals(userId)) {
            throw new BusinessException(400, "不能认领自己发布的心愿");
        }
        wish.setClaimedBy(userId);
        wish.setStatus(2);
        wish.setClaimTime(java.time.LocalDateTime.now());
        updateById(wish);
    }

    public Map<String, Object> getWishList(Long userId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Wish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wish::getUserId, userId);
        if (status != null) {
            wrapper.eq(Wish::getStatus, status);
        }
        wrapper.orderByDesc(Wish::getCreateTime);

        Page<Wish> wishPage = page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", wishPage.getRecords());
        result.put("total", wishPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", wishPage.getCurrent() < wishPage.getPages());
        return result;
    }

    public Map<String, Object> getAvailableWishes(Integer category, Integer page, Integer size) {
        LambdaQueryWrapper<Wish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wish::getStatus, 1);
        if (category != null) {
            wrapper.eq(Wish::getCategory, category);
        }
        wrapper.orderByDesc(Wish::getCreateTime);

        Page<Wish> wishPage = page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", wishPage.getRecords());
        result.put("total", wishPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", wishPage.getCurrent() < wishPage.getPages());
        return result;
    }
}
