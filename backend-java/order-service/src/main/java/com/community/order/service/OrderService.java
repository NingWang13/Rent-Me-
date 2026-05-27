package com.community.order.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.order.entity.Order;
import com.community.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单服务类
 */
@Service
public class OrderService extends ServiceImpl<OrderRepository, Order> {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    /**
     * 创建订单
     */
    public Order createOrder(Order order) {
        // 生成订单号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(1); // 待支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);

        save(order);
        log.info("创建订单成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        return order;
    }

    /**
     * 根据订单号查询订单
     */
    public Order getOrderByOrderNo(String orderNo) {
        return lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getDeleted, 0)
                .one();
    }

    /**
     * 根据用户ID查询订单列表
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime)
                .list();
    }

    /**
     * 支付成功
     */
    public void paySuccess(String orderNo) {
        Order order = getOrderByOrderNo(orderNo);
        if (order == null) {
            throw BusinessException.of("订单不存在: " + orderNo);
        }
        if (order.getStatus() != 1) {
            throw BusinessException.of("订单状态不正确: " + order.getStatus());
        }
        order.setStatus(2); // 已支付
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单支付成功: orderNo={}", orderNo);
    }

    /**
     * 完成订单
     */
    public void completeOrder(String orderNo) {
        Order order = getOrderByOrderNo(orderNo);
        if (order == null) {
            throw BusinessException.of("订单不存在: " + orderNo);
        }
        if (order.getStatus() != 3) {
            throw BusinessException.of("订单状态不正确: " + order.getStatus());
        }
        order.setStatus(4); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单完成: orderNo={}", orderNo);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(String orderNo) {
        Order order = getOrderByOrderNo(orderNo);
        if (order == null) {
            throw BusinessException.of("订单不存在: " + orderNo);
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw BusinessException.of("订单状态不正确: " + order.getStatus());
        }
        order.setStatus(5); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单取消: orderNo={}", orderNo);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORD" + timestamp + uuid;
    }
}
