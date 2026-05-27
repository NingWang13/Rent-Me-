package com.community.order.service;

import com.community.common.exception.BusinessException;
import com.community.order.entity.Order;
import com.community.order.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD1234567890");
        testOrder.setUserId(1L);
        testOrder.setProviderId(null);
        testOrder.setServiceType("errand");
        testOrder.setTitle("Test Order");
        testOrder.setAmount(new BigDecimal("100.00"));
        testOrder.setStatus(0);
    }

    @Test
    void getOrderById_Success() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals("ORD1234567890", result.getOrderNo());
        verify(orderMapper).selectById(1L);
    }

    @Test
    void getOrderById_NotFound() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void createOrder_Success() {
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        Order newOrder = new Order();
        newOrder.setUserId(1L);
        newOrder.setServiceType("errand");
        newOrder.setTitle("New Order");
        newOrder.setAmount(new BigDecimal("50.00"));

        Order result = orderService.createOrder(newOrder);

        assertNotNull(result);
        assertNotNull(result.getOrderNo());
        assertEquals(0, result.getStatus());
        verify(orderMapper).insert(newOrder);
    }

    @Test
    void acceptOrder_Success() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        Order result = orderService.acceptOrder(1L, 2L);

        assertNotNull(result);
        assertEquals(1, result.getStatus());
        assertEquals(2L, result.getProviderId());
        assertNotNull(result.getAcceptTime());
        verify(orderMapper).updateById(any(Order.class));
    }

    @Test
    void acceptOrder_InvalidStatus() {
        testOrder.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.acceptOrder(1L, 2L));
    }

    @Test
    void completeOrder_Success() {
        testOrder.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        Order result = orderService.completeOrder(1L);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
        assertNotNull(result.getCompleteTime());
        verify(orderMapper).updateById(any(Order.class));
    }

    @Test
    void completeOrder_InvalidStatus() {
        testOrder.setStatus(0);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.completeOrder(1L));
    }

    @Test
    void cancelOrder_Success() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        Order result = orderService.cancelOrder(1L, 1L);

        assertNotNull(result);
        assertEquals(3, result.getStatus());
        verify(orderMapper).updateById(any(Order.class));
    }

    @Test
    void cancelOrder_Forbidden() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 999L));
    }

    @Test
    void cancelOrder_CompletedOrder() {
        testOrder.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 1L));
    }
}
