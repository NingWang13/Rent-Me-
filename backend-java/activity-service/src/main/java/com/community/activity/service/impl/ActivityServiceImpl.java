package com.community.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.activity.entity.Activity;
import com.community.activity.mapper.ActivityMapper;
import com.community.activity.service.ActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Override
    public Activity createActivity(Activity activity) {
        activity.setStatus(1); // 1-进行中
        this.save(activity);
        return activity;
    }

    @Override
    public List<Activity> getActivityList(int page, int size, String keyword) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Activity::getTitle, keyword);
        }
        wrapper.eq(Activity::getDeleted, 0);
        wrapper.orderByDesc(Activity::getCreateTime);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public long countActivities(String keyword) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Activity::getTitle, keyword);
        }
        wrapper.eq(Activity::getDeleted, 0);
        return this.count(wrapper);
    }

    @Override
    public Activity getActivityById(Long id) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getId, id);
        wrapper.eq(Activity::getDeleted, 0);
        return this.getOne(wrapper);
    }

    @Override
    public Activity updateActivity(Long id, Activity activity) {
        activity.setId(id);
        this.updateById(activity);
        return this.getActivityById(id);
    }

    @Override
    public void deleteActivity(Long id) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getId, id);
        Activity activity = this.getOne(wrapper);
        
        if (activity != null) {
            activity.setDeleted(1);
            this.updateById(activity);
        }
    }

    @Override
    public void signupActivity(Long activityId, Long userId) {
        // 这里应该有一个活动报名表，简化版直接抛出异常提示
        throw new RuntimeException("报名功能需要实现活动报名表");
    }

    @Override
    public void cancelSignup(Long activityId, Long userId) {
        // 取消报名功能
        throw new RuntimeException("取消报名功能需要实现活动报名表");
    }

    @Override
    public void checkinActivity(Long activityId, Long userId) {
        // 活动签到功能
        throw new RuntimeException("签到功能需要实现活动报名表");
    }
}
