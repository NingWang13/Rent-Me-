package com.community.message.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.message.entity.Message;
import com.community.message.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务类
 */
@Service
public class MessageService extends ServiceImpl<MessageRepository, Message> {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    /**
     * 创建消息
     */
    public Message createMessage(Message message) {
        message.setIsRead(0); // 未读
        message.setCreateTime(LocalDateTime.now());
        save(message);
        log.info("创建消息成功: messageId={}, userId={}", message.getId(), message.getUserId());
        return message;
    }

    /**
     * 根据用户ID查询消息列表
     */
    public List<Message> getMessagesByUserId(Long userId, Integer type) {
        if (type != null && type > 0) {
            return lambdaQuery()
                    .eq(Message::getUserId, userId)
                    .eq(Message::getType, type)
                    .orderByDesc(Message::getCreateTime)
                    .list();
        } else {
            return lambdaQuery()
                    .eq(Message::getUserId, userId)
                    .orderByDesc(Message::getCreateTime)
                    .list();
        }
    }

    /**
     * 标记消息为已读
     */
    public void markAsRead(Long messageId) {
        Message message = getById(messageId);
        if (message == null) {
            throw BusinessException.of("消息不存在: " + messageId);
        }
        message.setIsRead(1); // 已读
        message.setReadTime(LocalDateTime.now());
        updateById(message);
        log.info("标记消息已读: messageId={}", messageId);
    }

    /**
     * 全部标记为已读
     */
    public void markAllAsRead(Long userId) {
        lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1)
                .set(Message::getReadTime, LocalDateTime.now())
                .update();
        log.info("全部标记已读: userId={}", userId);
    }

    /**
     * 删除消息
     */
    public void deleteMessage(Long messageId) {
        Message message = getById(messageId);
        if (message == null) {
            throw BusinessException.of("消息不存在: " + messageId);
        }
        removeById(messageId);
        log.info("删除消息: messageId={}", messageId);
    }

    /**
     * 清空消息
     */
    public void clearMessages(Long userId) {
        lambdaUpdate()
                .eq(Message::getUserId, userId)
                .remove();
        log.info("清空消息: userId={}", userId);
    }
}
