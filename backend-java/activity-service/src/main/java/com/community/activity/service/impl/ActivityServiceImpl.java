package com.community.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.activity.entity.Activity;
import com.community.activity.entity.ActivitySignup;
import com.community.activity.mapper.ActivityMapper;
import com.community.activity.mapper.ActivitySignupMapper;
import com.community.activity.service.ActivityService;
import com.community.common.exception.BusinessException;
import com.community.common.exception.ErrorCode;
import com.community.common.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    private final ActivitySignupMapper activitySignupMapper;

    public ActivityServiceImpl(ActivitySignupMapper activitySignupMapper) {
        this.activitySignupMapper = activitySignupMapper;
    }

    @Override
    public Activity getActivityById(Long id) {
        Activity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    @Override
    public PageResponse<Activity> getActivityList(int page, int size, Integer status, String category) {
        Page<Activity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Activity::getCategory, category);
        }
        wrapper.orderByDesc(Activity::getStartTime);
        Page<Activity> result = this.page(pageParam, wrapper);
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public List<Activity> getMyActivities(Long userId) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrganizerId, userId);
        wrapper.orderByDesc(Activity::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activity createActivity(Activity activity) {
        activity.setStatus(0);
        activity.setCurrentParticipants(0);
        this.save(activity);
        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activity updateActivity(Long id, Activity activity, Long organizerId) {
        Activity existing = getActivityById(id);
        if (!existing.getOrganizerId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        activity.setId(id);
        this.updateById(activity);
        return getActivityById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id, Long organizerId) {
        Activity activity = getActivityById(id);
        if (!activity.getOrganizerId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivitySignup signupActivity(Long activityId, Long userId) {
        Activity activity = getActivityById(activityId);
        if (activity.getStatus() == 2 || activity.getStatus() == 3) {
            throw new BusinessException(ErrorCode.ACTIVITY_ENDED);
        }
        if (activity.getMaxParticipants() != null && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }

        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId);
        wrapper.eq(ActivitySignup::getUserId, userId);
        ActivitySignup existing = activitySignupMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setStatus(0);
        activitySignupMapper.insert(signup);

        int updated = activityMapper.incrementParticipants(activityId, activity.getMaxParticipants());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }

        return signup;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSignup(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId);
        wrapper.eq(ActivitySignup::getUserId, userId);
        ActivitySignup signup = activitySignupMapper.selectOne(wrapper);
        if (signup == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        activitySignupMapper.deleteById(signup.getId());

        Activity activity = getActivityById(activityId);
        activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
        this.updateById(activity);
    }

    @Override
    public List<ActivitySignup> getSignups(Long activityId) {
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId);
        return activitySignupMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId);
        wrapper.eq(ActivitySignup::getUserId, userId);
        ActivitySignup signup = activitySignupMapper.selectOne(wrapper);
        if (signup == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        signup.setStatus(1);
        signup.setCheckInTime(LocalDateTime.now());
        activitySignupMapper.updateById(signup);
    }
}
