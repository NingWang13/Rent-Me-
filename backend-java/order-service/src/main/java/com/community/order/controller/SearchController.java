package com.community.order.controller;

import com.community.common.response.ApiResponse;
import com.community.order.service.SearchFilterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchFilterService searchFilterService;

    public SearchController(SearchFilterService searchFilterService) {
        this.searchFilterService = searchFilterService;
    }

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> searchOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Map<String, Object> result = searchFilterService.searchOrders(
                userId, keyword, category, status, page, size
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/wishes")
    public ApiResponse<Map<String, Object>> searchWishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Map<String, Object> result = searchFilterService.searchWishes(
                keyword, category, status, latitude, longitude, radiusKm, page, size
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/hot-tags")
    public ApiResponse<List<Map<String, Object>>> getHotTags() {
        List<Map<String, Object>> tags = searchFilterService.getHotTags();
        return ApiResponse.success(tags);
    }

    @GetMapping("/recommended-users")
    public ApiResponse<List<Map<String, Object>>> getRecommendedUsers(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<Map<String, Object>> users = searchFilterService.getRecommendedUsers(userId, limit);
        return ApiResponse.success(users);
    }
}
