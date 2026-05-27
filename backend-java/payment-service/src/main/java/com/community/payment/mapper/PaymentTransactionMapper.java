package com.community.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.payment.entity.PaymentTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentTransactionMapper extends BaseMapper<PaymentTransaction> {
}
