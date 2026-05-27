package com.community.message.service;

import com.community.message.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 创建消息
     */
    Message createMessage(Message message);

    /**
     * 根据用户ID查询消息列表
     */
    List<Message> getMessagesByUserId(Long userId, Integer type);

    /**
     * 分页查询消息列表
     */
    List<Message> getMessageList(int page, int size, Long userId, Integer type);

    /**
     * 分页查询未读消息
     */
    List<Message> getUnreadMessages(int page, int size, Long userId);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    Message markAsRead(Long messageId);

    /**
     * 标记消息为已读（带用户ID校验）
     */
    Message markAsRead(Long messageId, Long userId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除消息
     */
    void deleteMessage(Long messageId);

    /**
     * 删除消息（带用户ID校验）
     */
    void deleteMessage(Long messageId, Long userId);

    /**
     * 发送消息
     */
    void sendMessage(Message message);
}
