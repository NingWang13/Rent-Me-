package com.community.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.response.ApiResponse;
import com.community.user.entity.Feedback;
import com.community.user.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/submit")
    public ApiResponse<Feedback> submitFeedback(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> params) {

        Feedback feedback = feedbackService.submitFeedback(
                userId,
                (Integer) params.get("type"),
                (String) params.get("content"),
                (String) params.get("images"),
                (Integer) params.get("rating"),
                (String) params.get("contact")
        );

        return ApiResponse.success(feedback);
    }

    @GetMapping("/list")
    public ApiResponse<Page<Feedback>> getUserFeedbacks(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Feedback> feedbacks = feedbackService.getUserFeedbacks(userId, page, size);
        return ApiResponse.success(feedbacks);
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = feedbackService.getFeedbackStatistics();
        return ApiResponse.success(statistics);
    }
}
