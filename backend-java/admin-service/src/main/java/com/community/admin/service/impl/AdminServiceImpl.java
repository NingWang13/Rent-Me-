package com.community.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.entity.AdminUser;
import com.community.admin.mapper.AdminUserMapper;
import com.community.admin.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminService {

    @Override
    public AdminUser login(String username, String password) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, username);
        wrapper.eq(AdminUser::getPassword, password); // 实际应该用加密密码
        wrapper.eq(AdminUser::getStatus, 1);
        return this.getOne(wrapper);
    }

    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userCount", 100);
        statistics.put("orderCount", 50);
        statistics.put("todayNewUsers", 5);
        statistics.put("todayNewOrders", 3);
        return statistics;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", 100);
        statistics.put("activeUsers", 80);
        statistics.put("newUsersToday", 5);
        return statistics;
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalOrders", 50);
        statistics.put("completedOrders", 30);
        statistics.put("pendingOrders", 20);
        return statistics;
    }

    @Override
    public Map<String, Object> getPaymentStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAmount", 10000);
        statistics.put("todayAmount", 500);
        return statistics;
    }

    @Override
    public AdminUser createAdmin(String username, String password, String realName, String phone, Integer role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(password); // 实际应该加密
        admin.setRealName(realName);
        admin.setPhone(phone);
        admin.setRole(role);
        admin.setStatus(1);
        
        this.save(admin);
        return admin;
    }

    @Override
    public void updateAdminStatus(Long adminId, int status) {
        AdminUser admin = new AdminUser();
        admin.setId(adminId);
        admin.setStatus(status);
        this.updateById(admin);
    }

    @Override
    public Map<String, Object> getAdminStatistics(Long adminId) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("managedUsers", 50);
        statistics.put("managedOrders", 30);
        return statistics;
    }
}
