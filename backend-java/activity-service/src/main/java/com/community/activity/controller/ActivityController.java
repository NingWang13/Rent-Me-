package com.community.activity.controller;

import com.community.activity.entity.Activity;
import com.community.activity.entity.ActivitySignup;
import com.community.activity.service.ActivityService;
import com.community.common.response.ApiResponse;
import com.community.common.response.PageResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity, @RequestHeader("X-User-Id") Long userId) {
        activity.setOrganizerId(userId);
        Activity created = activityService.createActivity(activity);
        return ApiResponse.success(created);
    }

    @GetMapping
    public ApiResponse<PageResponse<Activity>> getActivityList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category) {
        PageResponse<Activity> result = activityService.getActivityList(page, size, status, category);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Activity> getActivityDetail(@PathVariable Long id) {
        Activity activity = activityService.getActivityById(id);
        return ApiResponse.success(activity);
    }

    @PutMapping("/{id}")
    public ApiResponse<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity, @RequestHeader("X-User-Id") Long userId) {
        Activity updated = activityService.updateActivity(id, activity, userId);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        activityService.deleteActivity(id, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/signup")
    public ApiResponse<ActivitySignup> signupActivity(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        ActivitySignup signup = activityService.signupActivity(id, userId);
        return ApiResponse.success(signup);
    }

    @PutMapping("/{id}/signup/cancel")
    public ApiResponse<Void> cancelSignup(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        activityService.cancelSignup(id, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/signups")
    public ApiResponse<List<ActivitySignup>> getSignups(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        List<ActivitySignup> signups = activityService.getSignups(id);
        return ApiResponse.success(signups);
    }

    @PostMapping("/{id}/checkin")
    public ApiResponse<Void> checkin(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        activityService.checkin(id, userId);
        return ApiResponse.success();
    }
}
