package com.community.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.common.exception.ErrorCode;
import com.community.common.response.PageResponse;
import com.community.payment.entity.PaymentTransaction;
import com.community.payment.mapper.PaymentTransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentTransactionMapper paymentTransactionMapper;

    public PaymentService(PaymentTransactionMapper paymentTransactionMapper) {
        this.paymentTransactionMapper = paymentTransactionMapper;
    }

    public PaymentTransaction getTransactionById(Long id) {
        PaymentTransaction transaction = paymentTransactionMapper.selectById(id);
        if (transaction == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return transaction;
    }

    public PaymentTransaction getTransactionByTransactionNo(String transactionNo) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getTransactionNo, transactionNo);
        return paymentTransactionMapper.selectOne(wrapper);
    }

    public PaymentTransaction getTransactionByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getOrderId, orderId);
        wrapper.orderByDesc(PaymentTransaction::getCreateTime);
        wrapper.last("LIMIT 1");
        return paymentTransactionMapper.selectOne(wrapper);
    }

    public PageResponse<PaymentTransaction> getTransactions(int page, int size, Long userId) {
        Page<PaymentTransaction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getUserId, userId);
        wrapper.orderByDesc(PaymentTransaction::getCreateTime);
        Page<PaymentTransaction> result = paymentTransactionMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentTransaction createTransaction(PaymentTransaction transaction) {
        String transactionNo = generateTransactionNo();
        transaction.setTransactionNo(transactionNo);
        transaction.setStatus(0);
        paymentTransactionMapper.insert(transaction);
        return transaction;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentTransaction updateTransactionStatus(String transactionNo, Integer status, String channelTransactionNo) {
        PaymentTransaction transaction = getTransactionByTransactionNo(transactionNo);
        transaction.setStatus(status);
        if (channelTransactionNo != null) {
            transaction.setChannelTransactionNo(channelTransactionNo);
        }
        if (status == 2) {
            transaction.setPaymentTime(LocalDateTime.now());
        }
        paymentTransactionMapper.updateById(transaction);
        return transaction;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentTransaction processPaymentCallback(String transactionNo, String channelTransactionNo, boolean success) {
        PaymentTransaction transaction = getTransactionByTransactionNo(transactionNo);
        if (transaction.getStatus() == 2) {
            log.warn("Transaction already paid: {}", transactionNo);
            return transaction;
        }
        if (success) {
            transaction.setStatus(2);
            transaction.setChannelTransactionNo(channelTransactionNo);
            transaction.setPaymentTime(LocalDateTime.now());
        } else {
            transaction.setStatus(3);
        }
        paymentTransactionMapper.updateById(transaction);
        return transaction;
    }

    private String generateTransactionNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "PAY" + timestamp + uuid;
    }
}
