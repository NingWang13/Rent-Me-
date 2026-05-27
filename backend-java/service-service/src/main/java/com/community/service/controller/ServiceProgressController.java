package com.community.service.controller;

import com.community.common.response.ApiResponse;
import com.community.service.entity.ServiceProgress;
import com.community.service.service.ServiceProgressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service/progress")
public class ServiceProgressController {

    private final ServiceProgressService serviceProgressService;

    public ServiceProgressController(ServiceProgressService serviceProgressService) {
        this.serviceProgressService = serviceProgressService;
    }

    @PostMapping("/create")
    public ApiResponse<ServiceProgress> createProgress(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ServiceProgress progress) {

        ServiceProgress result = serviceProgressService.createProgress(
                progress.getOrderId(),
                userId,
                progress.getStepName(),
                progress.getDescription()
        );

        return ApiResponse.success(result);
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<ServiceProgress>> getProgressByOrderId(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long currentUserId) {
        List<ServiceProgress> progressList = serviceProgressService.getProgressByOrderId(orderId, currentUserId);
        return ApiResponse.success(progressList);
    }

    @PutMapping("/{progressId}")
    public ApiResponse<ServiceProgress> updateProgress(
            @PathVariable Long progressId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ServiceProgress progress) {

        ServiceProgress result = serviceProgressService.updateProgress(
                progressId,
                userId,
                progress.getDescription(),
                progress.getLocation(),
                progress.getPhotoUrl()
        );

        return ApiResponse.success(result);
    }

    @PostMapping("/{progressId}/complete")
    public ApiResponse<ServiceProgress> completeProgress(
            @PathVariable Long progressId,
            @RequestHeader("X-User-Id") Long userId) {
        ServiceProgress result = serviceProgressService.completeProgress(progressId, userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/helper/in-progress")
    public ApiResponse<List<ServiceProgress>> getInProgressServices(
            @RequestHeader("X-User-Id") Long userId) {

        List<ServiceProgress> progressList = serviceProgressService.getInProgressServices(userId);
        return ApiResponse.success(progressList);
    }
}
