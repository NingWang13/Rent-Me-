package com.community.message.controller;

import com.community.common.response.ApiResponse;
import com.community.message.entity.Message;
import com.community.message.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 创建消息
     */
    @PostMapping("/create")
    public ApiResponse<Message> createMessage(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody MessageCreateRequest request) {
        log.info("创建消息: userId={}, type={}", userId, request.getType());
        try {
            Message message = new Message();
            message.setUserId(userId);
            message.setType(request.getType());
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setRelatedType(request.getRelatedType());
            message.setRelatedId(request.getRelatedId());

            Message created = messageService.createMessage(message);
            return ApiResponse.success(created);
        } catch (Exception e) {
            log.error("创建消息失败", e);
            return ApiResponse.error(500, "创建消息失败: " + e.getMessage());
        }
    }

    public static class MessageCreateRequest {
        private Integer type;
        private String title;
        private String content;
        private String relatedType;
        private String relatedId;

        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getRelatedType() { return relatedType; }
        public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
        public String getRelatedId() { return relatedId; }
        public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    }

    /**
     * 根据用户ID查询消息列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Message>> getMessagesByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer type) {
        log.info("查询用户消息: userId={}, type={}", userId, type);
        try {
            List<Message> messages = messageService.getMessagesByUserId(userId, type);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            log.error("查询用户消息失败", e);
            return ApiResponse.error(500, "查询消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{messageId}")
    public ApiResponse<Void> markAsRead(@PathVariable Long messageId) {
        log.info("标记消息已读: messageId={}", messageId);
        try {
            messageService.markAsRead(messageId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return ApiResponse.error(500, "标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/read-all/{userId}")
    public ApiResponse<Void> markAllAsRead(@PathVariable Long userId) {
        log.info("全部标记已读: userId={}", userId);
        try {
            messageService.markAllAsRead(userId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("全部标记已读失败", e);
            return ApiResponse.error(500, "全部标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long messageId) {
        log.info("删除消息: messageId={}", messageId);
        try {
            messageService.deleteMessage(messageId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("删除消息失败", e);
            return ApiResponse.error(500, "删除消息失败: " + e.getMessage());
        }
    }

    /**
     * 清空消息
     */
    @DeleteMapping("/clear/{userId}")
    public ApiResponse<Void> clearMessages(@PathVariable Long userId) {
        log.info("清空消息: userId={}", userId);
        try {
            messageService.clearMessages(userId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("清空消息失败", e);
            return ApiResponse.error(500, "清空消息失败: " + e.getMessage());
        }
    }
}
