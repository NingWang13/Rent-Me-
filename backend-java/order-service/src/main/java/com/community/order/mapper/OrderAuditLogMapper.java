package com.community.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.order.entity.OrderAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderAuditLogMapper extends BaseMapper<OrderAuditLog> {
}
