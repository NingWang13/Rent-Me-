package com.community.message.service;

import com.community.common.exception.BusinessException;
import com.community.message.entity.Message;
import com.community.message.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setUserId(100L);
        testMessage.setTitle("Test Message");
        testMessage.setContent("Test content");
        testMessage.setType(1);
        testMessage.setIsRead(0);
        testMessage.setStatus(1);
        testMessage.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getMessageById_Success() {
        when(messageMapper.selectById(eq(1L))).thenReturn(testMessage);

        Message result = messageService.getMessageById(1L);

        assertNotNull(result);
        assertEquals("Test Message", result.getTitle());
        verify(messageMapper).selectById(eq(1L));
    }

    @Test
    void getMessageById_NotFound() {
        when(messageMapper.selectById(eq(999L))).thenReturn(null);

        assertThrows(BusinessException.class, () -> messageService.getMessageById(999L));
    }

    @Test
    void markAsRead_Success() {
        when(messageMapper.selectById(eq(1L))).thenReturn(testMessage);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        Message result = messageService.markAsRead(1L, 100L);

        assertNotNull(result);
        assertEquals(1, result.getIsRead());
        assertNotNull(result.getReadTime());
    }

    @Test
    void markAsRead_Forbidden() {
        when(messageMapper.selectById(eq(1L))).thenReturn(testMessage);

        assertThrows(BusinessException.class, () -> messageService.markAsRead(1L, 999L));
    }

    @Test
    void getUnreadCount_Success() {
        when(messageMapper.selectCount(any())).thenReturn(5L);

        long count = messageService.getUnreadCount(100L);

        assertEquals(5, count);
    }

    @Test
    void sendMessage_Success() {
        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        Message newMessage = new Message();
        newMessage.setUserId(100L);
        newMessage.setTitle("New Message");
        newMessage.setContent("New content");

        messageService.sendMessage(newMessage);

        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    void deleteMessage_Success() {
        when(messageMapper.selectById(eq(1L))).thenReturn(testMessage);
        when(messageMapper.deleteById(eq(1L))).thenReturn(1);

        messageService.deleteMessage(1L, 100L);

        verify(messageMapper).deleteById(eq(1L));
    }

    @Test
    void deleteMessage_Forbidden() {
        when(messageMapper.selectById(eq(1L))).thenReturn(testMessage);

        assertThrows(BusinessException.class, () -> messageService.deleteMessage(1L, 999L));
    }
}
