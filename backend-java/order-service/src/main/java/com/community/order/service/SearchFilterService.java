package com.community.order.service;

import com.community.order.entity.Order;
import com.community.order.entity.Wish;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchFilterService {

    private final ElasticsearchService elasticsearchService;
    private final WishSearchService wishSearchService;

    public SearchFilterService(ElasticsearchService elasticsearchService, WishSearchService wishSearchService) {
        this.elasticsearchService = elasticsearchService;
        this.wishSearchService = wishSearchService;
    }

    public static final Map<Integer, String> ORDER_CATEGORIES = Map.of(
            1, "代购服务",
            2, "上门服务",
            3, "技能交换",
            4, "跑腿帮忙",
            5, "陪伴照顾",
            6, "其他互助"
    );

    public static final Map<Integer, String> ORDER_STATUS = Map.of(
            1, "待接单",
            2, "进行中",
            3, "已完成",
            4, "已取消"
    );

    public static final Map<Integer, String> WISH_CATEGORIES = Map.of(
            1, "学习培训",
            2, "生活服务",
            3, "技能分享",
            4, "陪伴聊天",
            5, "物品借用",
            6, "其他心愿"
    );

    public static final Map<Integer, String> WISH_STATUS = Map.of(
            1, "待帮助",
            2, "已认领",
            3, "已完成",
            4, "已取消"
    );

    public Map<String, Object> searchOrders(Long userId, String keyword, Integer category,
                                            String status, Integer page, Integer size) {
        int from = (page - 1) * size;

        List<Order> orders = elasticsearchService.searchOrders(
                userId, keyword, status, from, size
        );

        long total = orders.size();
        int totalPages = (int) Math.ceil((double) total / size);

        return Map.of(
                "list", orders,
                "total", total,
                "page", page,
                "size", size,
                "totalPages", totalPages,
                "categories", ORDER_CATEGORIES,
                "statusOptions", ORDER_STATUS
        );
    }

    public Map<String, Object> searchWishes(String keyword, Integer category, Integer status,
                                             Double latitude, Double longitude, Double radiusKm,
                                             Integer page, Integer size) {
        int from = (page - 1) * size;

        List<Wish> wishes = wishSearchService.searchWishes(
                keyword, category, status, latitude, longitude, radiusKm, from, size
        );

        long total = wishes.size();
        int totalPages = (int) Math.ceil((double) total / size);

        return Map.of(
                "list", wishes,
                "total", total,
                "page", page,
                "size", size,
                "totalPages", totalPages,
                "categories", WISH_CATEGORIES,
                "statusOptions", WISH_STATUS
        );
    }

    public List<Map<String, Object>> getHotTags() {
        return List.of(
                Map.of("name", "代购", "count", 156),
                Map.of("name", "上门", "count", 132),
                Map.of("name", "陪伴", "count", 98),
                Map.of("name", "跑腿", "count", 87),
                Map.of("name", "技能交换", "count", 76),
                Map.of("name", "照顾", "count", 65),
                Map.of("name", "学习", "count", 54),
                Map.of("name", "聊天", "count", 43)
        );
    }

    public List<Map<String, Object>> getRecommendedUsers(Long userId, int limit) {
        return List.of(
                Map.of("userId", 1001, "username", "热心邻居", "creditScore", 950, "helpCount", 128),
                Map.of("userId", 1002, "username", "互助达人", "creditScore", 920, "helpCount", 95),
                Map.of("userId", 1003, "username", "可靠伙伴", "creditScore", 890, "helpCount", 76)
        );
    }
}
