package com.community.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.order.entity.Order;
import com.community.order.mapper.OrderMapper;
import com.community.order.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public Order createOrder(Order order) {
        // 生成订单号
        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
        order.setOrderNo(orderNo);
        order.setStatus(1); // 待支付
        this.save(order);
        return order;
    }

    @Override
    public List<Order> getOrderList(int page, int size, Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.eq(Order::getDeleted, 0);
        wrapper.orderByDesc(Order::getCreateTime);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public long countOrders(Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.eq(Order::getDeleted, 0);
        return this.count(wrapper);
    }

    @Override
    public Order getOrderById(Long id) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, id);
        wrapper.eq(Order::getDeleted, 0);
        return this.getOne(wrapper);
    }

    @Override
    public Order acceptOrder(Long id, Long providerId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getId, id);
        Order order = this.getOne(queryWrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不允许接单");
        }
        
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, id);
        updateWrapper.set(Order::getProviderId, providerId);
        updateWrapper.set(Order::getStatus, 3); // 服务中
        updateWrapper.set(Order::getAcceptTime, new Date());
        
        this.update(updateWrapper);
        
        return this.getOrderById(id);
    }

    @Override
    public Order completeOrder(Long id) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getId, id);
        Order order = this.getOne(queryWrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单状态不允许完成");
        }
        
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, id);
        updateWrapper.set(Order::getStatus, 4); // 已完成
        updateWrapper.set(Order::getCompleteTime, new Date());
        
        this.update(updateWrapper);
        
        return this.getOrderById(id);
    }

    @Override
    public Order cancelOrder(Long id, Long userId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getId, id);
        queryWrapper.eq(Order::getUserId, userId);
        Order order = this.getOne(queryWrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在或无权限");
        }
        
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new RuntimeException("订单状态不允许取消");
        }
        
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, id);
        updateWrapper.set(Order::getStatus, 5); // 已取消
        
        this.update(updateWrapper);
        
        return this.getOrderById(id);
    }
}
