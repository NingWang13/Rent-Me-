package com.community.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.payment.entity.PaymentTransaction;
import com.community.payment.mapper.PaymentTransactionMapper;
import com.community.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentTransactionMapper, PaymentTransaction> implements PaymentService {

    @Override
    public PaymentTransaction createTransaction(PaymentTransaction transaction) {
        this.save(transaction);
        return transaction;
    }

    @Override
    public void processPaymentCallback(String transactionNo, String channelTransactionNo, boolean success) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getTransactionNo, transactionNo);
        PaymentTransaction transaction = this.getOne(wrapper);
        
        if (transaction != null) {
            transaction.setChannelTransactionNo(channelTransactionNo);
            transaction.setStatus(success ? 2 : 3); // 2-成功 3-失败
            this.updateById(transaction);
        }
    }

    @Override
    public PaymentTransaction getTransactionByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getOrderId, orderId);
        return this.getOne(wrapper);
    }

    @Override
    public List<PaymentTransaction> getTransactions(int page, int size, Long userId) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getUserId, userId);
        wrapper.orderByDesc(PaymentTransaction::getCreateTime);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public long countTransactions(Long userId) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getUserId, userId);
        return this.count(wrapper);
    }
}
