package com.community.activity.service;

import com.community.activity.entity.Activity;
import com.community.activity.entity.ActivitySignup;
import com.community.common.response.PageResponse;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 根据ID获取活动
     */
    Activity getActivityById(Long id);

    /**
     * 分页查询活动列表
     */
    PageResponse<Activity> getActivityList(int page, int size, Integer status, String category);

    /**
     * 查询我组织的活动
     */
    List<Activity> getMyActivities(Long userId);

    /**
     * 创建活动
     */
    Activity createActivity(Activity activity);

    /**
     * 更新活动
     */
    Activity updateActivity(Long id, Activity activity, Long organizerId);

    /**
     * 删除活动
     */
    void deleteActivity(Long id, Long organizerId);

    /**
     * 报名活动
     */
    ActivitySignup signupActivity(Long activityId, Long userId);

    /**
     * 取消报名
     */
    void cancelSignup(Long activityId, Long userId);

    /**
     * 查询活动报名列表
     */
    List<ActivitySignup> getSignups(Long activityId);

    /**
     * 活动签到
     */
    void checkin(Long activityId, Long userId);
}
