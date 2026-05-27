package com.community.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.entity.AdminUser;

import java.util.Map;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    AdminUser login(String username, String password);

    /**
     * 创建管理员
     */
    AdminUser createAdmin(String username, String password, String realName, String phone, Integer role);

    /**
     * 分页查询管理员列表
     */
    Page<AdminUser> getAdminList(int page, int size, String keyword);

    /**
     * 更新管理员状态
     */
    void updateAdminStatus(Long adminId, int status);

    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getDashboardStatistics();

    /**
     * 获取管理员统计数据
     */
    Map<String, Object> getAdminStatistics(Long adminId);

    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStatistics();

    /**
     * 获取订单统计数据
     */
    Map<String, Object> getOrderStatistics();

    /**
     * 获取支付统计数据
     */
    Map<String, Object> getPaymentStatistics();
}
