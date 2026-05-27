package com.community.order.controller;

import com.community.common.response.ApiResponse;
import com.community.order.entity.Order;
import com.community.order.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ApiResponse<Order> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody OrderCreateRequest request) {
        log.info("创建订单: userId={}, title={}", userId, request.getTitle());
        try {
            Order order = new Order();
            order.setUserId(userId);
            order.setTitle(request.getTitle());
            order.setDescription(request.getDescription());
            order.setAmount(request.getAmount());
            order.setServiceType(request.getServiceType());
            order.setAddress(request.getAddress());
            order.setContactPhone(request.getContactPhone());
            order.setStatus(0);

            Order created = orderService.createOrder(order);
            return ApiResponse.success(created);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return ApiResponse.error(500, "创建订单失败: " + e.getMessage());
        }
    }

    public static class OrderCreateRequest {
        private String title;
        private String description;
        private Double amount;
        private String serviceType;
        private String address;
        private String contactPhone;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> getOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String orderNo) {
        log.info("查询订单: orderNo={}, userId={}", orderNo, userId);
        try {
            Order order = orderService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return ApiResponse.error(404, "订单不存在");
            }
            if (!order.getUserId().equals(userId)) {
                return ApiResponse.error(403, "无权查看此订单");
            }
            return ApiResponse.success(order);
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return ApiResponse.error(500, "查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询订单列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Order>> getOrdersByUserId(@RequestHeader("X-User-Id") Long userId) {
        log.info("查询用户订单: userId={}", userId);
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ApiResponse.success(orders);
        } catch (Exception e) {
            log.error("查询用户订单失败", e);
            return ApiResponse.error(500, "查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付成功回调
     */
    @PostMapping("/payment-success/{orderNo}")
    public ApiResponse<Void> paymentSuccess(@PathVariable String orderNo) {
        log.info("订单支付成功: orderNo={}", orderNo);
        try {
            orderService.paySuccess(orderNo);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("订单支付成功处理失败", e);
            return ApiResponse.error(500, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 完成订单
     */
    @PostMapping("/complete/{orderNo}")
    public ApiResponse<Void> completeOrder(@PathVariable String orderNo) {
        log.info("完成订单: orderNo={}", orderNo);
        try {
            orderService.completeOrder(orderNo);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("完成订单失败", e);
            return ApiResponse.error(500, "完成订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderNo}")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderNo) {
        log.info("取消订单: orderNo={}", orderNo);
        try {
            orderService.cancelOrder(orderNo);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return ApiResponse.error(500, "取消订单失败: " + e.getMessage());
        }
    }
}
