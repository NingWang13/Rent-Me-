package com.community.payment.service;

import com.community.payment.entity.PaymentTransaction;
import com.community.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testCreateTransaction() {
        // Given
        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(1L)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(1)
                .status(0)
                .build();

        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(transaction);

        // When
        PaymentTransaction result = paymentService.createTransaction(transaction);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void testProcessPaymentCallback_Success() {
        // Given
        String transactionNo = "TXN123456";
        String channelTransactionNo = "CHN789012";
        boolean success = true;

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionNo(transactionNo)
                .status(0) // 待支付
                .build();

        when(transactionRepository.findByTransactionNo(transactionNo)).thenReturn(Optional.of(transaction));

        // When
        paymentService.processPaymentCallback(transactionNo, channelTransactionNo, success);

        // Then
        assertEquals(1, transaction.getStatus()); // 已支付
        assertEquals(channelTransactionNo, transaction.getChannelTransactionNo());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void testProcessPaymentCallback_Failure() {
        // Given
        String transactionNo = "TXN123456";
        String channelTransactionNo = "CHN789012";
        boolean success = false;

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionNo(transactionNo)
                .status(0) // 待支付
                .build();

        when(transactionRepository.findByTransactionNo(transactionNo)).thenReturn(Optional.of(transaction));

        // When
        paymentService.processPaymentCallback(transactionNo, channelTransactionNo, success);

        // Then
        assertEquals(2, transaction.getStatus()); // 支付失败
        verify(transactionRepository, times(1)).save(transaction);
    }
}
