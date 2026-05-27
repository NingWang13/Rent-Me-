package com.community.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.entity.AdminUser;
import com.community.admin.mapper.AdminUserMapper;
import com.community.admin.service.AdminService;
import com.community.common.exception.BusinessException;
import com.community.common.security.PasswordEncoder;
import com.community.order.entity.Order;
import com.community.order.mapper.OrderMapper;
import com.community.order.mapper.WishMapper;
import com.community.payment.entity.PaymentTransaction;
import com.community.payment.mapper.PaymentTransactionMapper;
import com.community.user.entity.User;
import com.community.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final WishMapper wishMapper;
    private final PaymentTransactionMapper paymentTransactionMapper;

    public AdminServiceImpl(PasswordEncoder passwordEncoder, UserMapper userMapper, OrderMapper orderMapper, WishMapper wishMapper, PaymentTransactionMapper paymentTransactionMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.wishMapper = wishMapper;
        this.paymentTransactionMapper = paymentTransactionMapper;
    }

    public static final int ROLE_SUPER_ADMIN = 1;
    public static final int ROLE_ADMIN = 2;
    public static final int ROLE_OPERATOR = 3;

    @Override
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

    @Override
    public AdminUser createAdmin(String username, String password, String realName, String phone, Integer role) {
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

    @Override
    public Page<AdminUser> getAdminList(int page, int size, String keyword) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<AdminUser>()
                        .like(keyword != null, AdminUser::getRealName, keyword)
                        .orderByDesc(AdminUser::getCreateTime)
        );
    }

    @Override
    public void updateAdminStatus(Long adminId, int status) {
        AdminUser admin = getById(adminId);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setStatus(status);
        updateById(admin);
    }

    @Override
    public Map<String, Object> getDashboardStatistics() {
        Long totalUsers = userMapper.selectCount(null);
        Long totalOrders = orderMapper.selectCount(null);
        Long totalWishes = wishMapper.selectCount(null);
        Long totalPayments = paymentTransactionMapper.selectCount(null);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Long todayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart)
                .le(User::getCreateTime, todayEnd));

        Long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd));

        Long todayWishes = wishMapper.selectCount(new LambdaQueryWrapper<com.community.order.entity.Wish>()
                .ge(com.community.order.entity.Wish::getCreateTime, todayStart)
                .le(com.community.order.entity.Wish::getCreateTime, todayEnd));

        BigDecimal todayPayments = paymentTransactionMapper.selectList(new LambdaQueryWrapper<PaymentTransaction>()
                .ge(PaymentTransaction::getCreateTime, todayStart)
                .le(PaymentTransaction::getCreateTime, todayEnd))
                .stream()
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", totalUsers);
        result.put("totalOrders", totalOrders);
        result.put("totalWishes", totalWishes);
        result.put("totalPayments", totalPayments);
        result.put("todayNewUsers", todayNewUsers);
        result.put("todayOrders", todayOrders);
        result.put("todayWishes", todayWishes);
        result.put("todayPayments", todayPayments);
        return result;
    }

    @Override
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

    @Override
    public Map<String, Object> getUserStatistics() {
        Long total = userMapper.selectCount(null);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusWeeks(1), LocalTime.MIN);

        Long activeToday = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)
                .ge(User::getLastLoginTime, todayStart));

        Long activeWeek = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)
                .ge(User::getLastLoginTime, weekStart));

        Long newToday = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart));

        Long newWeek = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, weekStart));

        Long verifiedIdCard = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getIdCardVerified, true));

        Long verifiedFace = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getFaceVerified, true));

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("activeToday", activeToday);
        result.put("activeWeek", activeWeek);
        result.put("newToday", newToday);
        result.put("newWeek", newWeek);
        result.put("verifiedIdCard", verifiedIdCard);
        result.put("verifiedFace", verifiedFace);
        return result;
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        Long total = orderMapper.selectCount(null);

        Long pending = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1));

        Long inProgress = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 2));

        Long completed = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 3));

        Long cancelled = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 4));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayNew = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart));

        double completionRate = total > 0 ? (completed.doubleValue() / total.doubleValue()) * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pending", pending);
        result.put("inProgress", inProgress);
        result.put("completed", completed);
        result.put("cancelled", cancelled);
        result.put("todayNew", todayNew);
        result.put("completionRate", Math.round(completionRate * 10.0) / 10.0);
        return result;
    }

    @Override
    public Map<String, Object> getPaymentStatistics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusWeeks(1), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        BigDecimal totalAmount = paymentTransactionMapper.selectList(new LambdaQueryWrapper<PaymentTransaction>()
                .eq(PaymentTransaction::getStatus, 1))
                .stream()
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayAmount = paymentTransactionMapper.selectList(new LambdaQueryWrapper<PaymentTransaction>()
                .eq(PaymentTransaction::getStatus, 1)
                .ge(PaymentTransaction::getCreateTime, todayStart))
                .stream()
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weekAmount = paymentTransactionMapper.selectList(new LambdaQueryWrapper<PaymentTransaction>()
                .eq(PaymentTransaction::getStatus, 1)
                .ge(PaymentTransaction::getCreateTime, weekStart))
                .stream()
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthAmount = paymentTransactionMapper.selectList(new LambdaQueryWrapper<PaymentTransaction>()
                .eq(PaymentTransaction::getStatus, 1)
                .ge(PaymentTransaction::getCreateTime, monthStart))
                .stream()
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalCount = paymentTransactionMapper.selectCount(new LambdaQueryWrapper<PaymentTransaction>()
                .eq(PaymentTransaction::getStatus, 1));

        BigDecimal averageAmount = totalCount > 0 ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal refundAmount = paymentTransactionMapper.selectList(new LambdaQueryWrapper<PaymentTransaction>()
                .eq(PaymentTransaction::getStatus, 2))
                .stream()
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("todayAmount", todayAmount);
        result.put("weekAmount", weekAmount);
        result.put("monthAmount", monthAmount);
        result.put("averageAmount", averageAmount);
        result.put("refundAmount", refundAmount);
        return result;
    }
}
