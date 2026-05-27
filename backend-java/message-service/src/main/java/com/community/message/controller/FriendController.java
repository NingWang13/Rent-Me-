package com.community.message.controller;

import com.community.common.response.ApiResponse;
import com.community.message.entity.FriendRelation;
import com.community.message.service.FriendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/apply")
    public ApiResponse<FriendRelation> applyFriend(@RequestBody FriendRelation relation) {
        FriendRelation result = friendService.applyFriend(relation);
        return ApiResponse.success(result);
    }

    @PostMapping("/{relationId}/accept")
    public ApiResponse<FriendRelation> acceptFriend(
            @PathVariable Long relationId,
            @RequestHeader("X-User-Id") Long userId) {
        FriendRelation result = friendService.acceptFriend(relationId, userId);
        return ApiResponse.success(result);
    }

    @PostMapping("/{relationId}/reject")
    public ApiResponse<Void> rejectFriend(
            @PathVariable Long relationId,
            @RequestHeader("X-User-Id") Long userId) {
        friendService.rejectFriend(relationId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(
            @PathVariable Long friendId,
            @RequestHeader("X-User-Id") Long userId) {
        friendService.deleteFriend(userId, friendId);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<FriendRelation>> getFriends(@RequestHeader("X-User-Id") Long userId) {
        List<FriendRelation> result = friendService.getFriends(userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/applications")
    public ApiResponse<List<FriendRelation>> getApplications(@RequestHeader("X-User-Id") Long userId) {
        List<FriendRelation> result = friendService.getFriendApplications(userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/pending-count")
    public ApiResponse<Long> getPendingCount(@RequestHeader("X-User-Id") Long userId) {
        long count = friendService.getPendingApplicationCount(userId);
        return ApiResponse.success(count);
    }
}
