package com.community.message.controller;

import com.community.common.response.ApiResponse;
import com.community.message.service.GroupChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/group")
public class GroupChatController {

    private final GroupChatService groupChatService;

    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createGroup(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer joinType,
            @RequestParam(defaultValue = "1") Integer chatType) {

        var group = groupChatService.createGroup(userId, name, description, joinType, chatType);
        return ApiResponse.success(Map.of(
                "id", group.getId(),
                "groupNo", group.getGroupNo(),
                "name", group.getName()
        ));
    }

    @PostMapping("/{groupId}/join")
    public ApiResponse<Void> joinGroup(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long groupId,
            @RequestParam(required = false) String applyMessage) {

        groupChatService.joinGroup(userId, groupId, applyMessage);
        return ApiResponse.success();
    }

    @PostMapping("/{groupId}/quit")
    public ApiResponse<Void> quitGroup(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long groupId) {

        groupChatService.quitGroup(userId, groupId);
        return ApiResponse.success();
    }

    @PostMapping("/{groupId}/dismiss")
    public ApiResponse<Void> dismissGroup(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long groupId) {

        groupChatService.dismissGroup(userId, groupId);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getGroupList(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Map<String, Object> result = groupChatService.getGroupList(userId, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/{groupId}/members")
    public ApiResponse<Map<String, Object>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Map<String, Object> result = groupChatService.getGroupMembers(groupId, page, size);
        return ApiResponse.success(result);
    }
}
