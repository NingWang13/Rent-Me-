package com.community.payment.controller;

import com.community.common.response.ApiResponse;
import com.community.payment.dto.CreateOrderRequest;
import com.community.payment.dto.RefundRequest;
import com.community.payment.service.WechatPayService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信支付控制器
 */
@RestController
@RequestMapping("/api/v1/wechat-pay")
public class WechatPayController {

    private static final Logger log = LoggerFactory.getLogger(WechatPayController.class);

    private final WechatPayService wechatPayService;

    public WechatPayController(WechatPayService wechatPayService) {
        this.wechatPayService = wechatPayService;
    }

    /**
     * 统一下单接口
     * @param request 下单请求
     * @return 支付参数
     */
    @PostMapping("/create-order")
    public ApiResponse<Map<String, String>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("创建微信支付订单: orderId={}, amount={}", request.getOrderId(), request.getAmount());
        try {
            Map<String, String> payParams = wechatPayService.createOrder(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getDescription(),
                    request.getOpenid()
            );
            return ApiResponse.success(payParams);
        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            return ApiResponse.error(500, "创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单状态
     * @param transactionNo 商户订单号
     * @return 订单状态
     */
    @GetMapping("/query-order/{transactionNo}")
    public ApiResponse<Map<String, Object>> queryOrder(@PathVariable String transactionNo) {
        log.info("查询微信支付订单: transactionNo={}", transactionNo);
        try {
            Map<String, Object> orderInfo = wechatPayService.queryOrder(transactionNo);
            return ApiResponse.success(orderInfo);
        } catch (Exception e) {
            log.error("查询微信支付订单失败", e);
            return ApiResponse.error(500, "查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 关闭订单
     * @param transactionNo 商户订单号
     * @return 成功
     */
    @PostMapping("/close-order/{transactionNo}")
    public ApiResponse<Void> closeOrder(@PathVariable String transactionNo) {
        log.info("关闭微信支付订单: transactionNo={}", transactionNo);
        try {
            wechatPayService.closeOrder(transactionNo);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("关闭微信支付订单失败", e);
            return ApiResponse.error(500, "关闭订单失败: " + e.getMessage());
        }
    }

    /**
     * 申请退款
     * @param request 退款请求
     * @return 退款单号
     */
    @PostMapping("/refund")
    public ApiResponse<String> refund(@Valid @RequestBody RefundRequest request) {
        log.info("申请微信支付退款: transactionNo={}, amount={}", request.getTransactionNo(), request.getRefundAmount());
        try {
            String refundNo = wechatPayService.refund(
                    request.getTransactionNo(),
                    request.getRefundAmount(),
                    request.getReason()
            );
            return ApiResponse.success(refundNo);
        } catch (Exception e) {
            log.error("申请微信支付退款失败", e);
            return ApiResponse.error(500, "申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付回调接口
     * @param callbackData 回调数据
     * @return 成功
     */
    @PostMapping("/callback")
    public ApiResponse<Void> paymentCallback(@RequestBody Map<String, Object> callbackData) {
        log.info("收到微信支付回调");
        try {
            wechatPayService.handlePaymentCallback(callbackData);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return ApiResponse.error(500, "处理回调失败: " + e.getMessage());
        }
    }
}
