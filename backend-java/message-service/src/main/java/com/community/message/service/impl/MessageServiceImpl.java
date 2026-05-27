package com.community.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.message.entity.Message;
import com.community.message.mapper.MessageMapper;
import com.community.message.service.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public Message createMessage(Message message) {
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        this.save(message);
        return message;
    }

    @Override
    public List<Message> getMessagesByUserId(Long userId, Integer type) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (type != null) {
            wrapper.eq(Message::getType, type);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<Message> getMessageList(int page, int size, Long userId, Integer type) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (type != null) {
            wrapper.eq(Message::getType, type);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public List<Message> getUnreadMessages(int page, int size, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.orderByDesc(Message::getCreateTime);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.eq(Message::getIsRead, 0);
        return this.count(wrapper);
    }

    @Override
    public Message markAsRead(Long messageId) {
        Message message = this.getById(messageId);
        if (message != null) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            this.updateById(message);
        }
        return message;
    }

    @Override
    public Message markAsRead(Long messageId, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getId, messageId);
        wrapper.eq(Message::getUserId, userId);
        Message message = this.getOne(wrapper);

        if (message != null) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            this.updateById(message);
        }

        return message;
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.eq(Message::getIsRead, 0);
        List<Message> messages = this.list(wrapper);

        for (Message message : messages) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
        }

        this.updateBatchById(messages);
    }

    @Override
    public void deleteMessage(Long messageId) {
        this.removeById(messageId);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getId, messageId);
        wrapper.eq(Message::getUserId, userId);
        this.remove(wrapper);
    }

    @Override
    public void sendMessage(Message message) {
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        this.save(message);
    }
}
