package com.community.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单数据访问接口
 */
@Mapper
public interface OrderRepository extends BaseMapper<Order> {
}
