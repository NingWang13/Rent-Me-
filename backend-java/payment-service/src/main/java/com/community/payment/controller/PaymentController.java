package com.community.payment.controller;

import com.community.common.response.ApiResponse;
import com.community.common.response.PageResponse;
import com.community.payment.entity.PaymentTransaction;
import com.community.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ApiResponse<PaymentTransaction> createPayment(@RequestBody PaymentTransaction transaction, @RequestHeader("X-User-Id") Long userId) {
        transaction.setUserId(userId);
        PaymentTransaction created = paymentService.createTransaction(transaction);
        return ApiResponse.success(created);
    }

    @PostMapping("/callback")
    public ApiResponse<Void> paymentCallback(@RequestParam String transactionNo, @RequestParam String channelTransactionNo, @RequestParam boolean success) {
        paymentService.processPaymentCallback(transactionNo, channelTransactionNo, success);
        return ApiResponse.success();
    }

    @GetMapping("/status/{orderId}")
    public ApiResponse<PaymentTransaction> getPaymentStatus(@PathVariable Long orderId) {
        PaymentTransaction transaction = paymentService.getTransactionByOrderId(orderId);
        return ApiResponse.success(transaction);
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<PaymentTransaction>> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") Long userId) {
        PageResponse<PaymentTransaction> result = paymentService.getTransactions(page, size, userId);
        return ApiResponse.success(result);
    }
}
