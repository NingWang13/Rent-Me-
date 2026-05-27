package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.entity.AdminUser;
import com.community.admin.mapper.AdminUserMapper;
import com.community.common.exception.BusinessException;
import com.community.common.security.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AdminService extends ServiceImpl<AdminUserMapper, AdminUser> {

    private final PasswordEncoder passwordEncoder;

    public AdminService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public static final int ROLE_SUPER_ADMIN = 1;
    public static final int ROLE_ADMIN = 2;
    public static final int ROLE_OPERATOR = 3;

    @Transactional(rollbackFor = Exception.class)
    public AdminUser login(String username, String password) {
        AdminUser admin = getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getStatus, 1)
        );

        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        admin.setPassword(null);
        return admin;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminUser createAdmin(String username, String password, String realName, String phone, int role) {
        if (count(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        AdminUser admin = AdminUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .realName(realName)
                .phone(phone)
                .role(role)
                .status(1)
                .build();

        save(admin);
        admin.setPassword(null);
        return admin;
    }

    public Page<AdminUser> getAdminList(int page, int size, String keyword) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<AdminUser>()
                        .like(keyword != null, AdminUser::getRealName, keyword)
                        .orderByDesc(AdminUser::getCreateTime)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAdminStatus(Long adminId, int status) {
        AdminUser admin = getById(adminId);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setStatus(status);
        updateById(admin);
    }

    public Map<String, Object> getDashboardStatistics() {
        return Map.of(
                "totalUsers", 12580,
                "totalOrders", 34256,
                "totalWishes", 18765,
                "totalPayments", 4567890,
                "todayNewUsers", 45,
                "todayOrders", 128,
                "todayWishes", 67,
                "todayPayments", 56780
        );
    }

    public Map<String, Object> getAdminStatistics(Long adminId) {
        AdminUser admin = getById(adminId);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        return Map.of(
                "adminId", admin.getId(),
                "adminName", admin.getRealName(),
                "role", admin.getRole(),
                "dashboardStats", getDashboardStatistics()
        );
    }

    public Map<String, Object> getUserStatistics() {
        return Map.of(
                "total", 12580,
                "activeToday", 892,
                "activeWeek", 3521,
                "newToday", 45,
                "newWeek", 312,
                "verifiedIdCard", 8654,
                "verifiedFace", 4321
        );
    }

    public Map<String, Object> getOrderStatistics() {
        return Map.of(
                "total", 34256,
                "pending", 456,
                "inProgress", 1234,
                "completed", 31567,
                "cancelled", 999,
                "todayNew", 128,
                "completionRate", 92.1
        );
    }

    public Map<String, Object> getPaymentStatistics() {
        return Map.of(
                "totalAmount", 4567890,
                "todayAmount", 56780,
                "weekAmount", 456780,
                "monthAmount", 1897650,
                "averageAmount", 133.3,
                "refundAmount", 23456
        );
    }
}
