package com.community.credit.service;

import com.community.common.response.PageResponse;
import com.community.credit.entity.CreditLog;

/**
 * 积分服务接口
 */
public interface CreditService {

    /**
     * 获取积分余额
     */
    int getCreditBalance(Long userId);

    /**
     * 分页查询积分记录
     */
    PageResponse<CreditLog> getCreditLogs(int page, int size, Long userId);

    /**
     * 每日签到
     */
    CreditLog checkin(Long userId);

    /**
     * 增加积分
     */
    CreditLog addCredit(Long userId, int amount, int type, Long relatedId, String relatedType, String description);

    /**
     * 扣除积分
     */
    CreditLog deductCredit(Long userId, int amount, int type, Long relatedId, String relatedType, String description);
}
