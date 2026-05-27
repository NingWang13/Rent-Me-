package com.community.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.entity.AdminUser;
import com.community.admin.service.AdminService;
import com.community.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        AdminUser admin = adminService.login(username, password);
        return ApiResponse.success(Map.of("admin", admin));
    }

    @GetMapping("/dashboard/statistics")
    public ApiResponse<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = adminService.getDashboardStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/user/statistics")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        Map<String, Object> statistics = adminService.getUserStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/order/statistics")
    public ApiResponse<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> statistics = adminService.getOrderStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/payment/statistics")
    public ApiResponse<Map<String, Object>> getPaymentStatistics() {
        Map<String, Object> statistics = adminService.getPaymentStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/admin/list")
    public ApiResponse<Page<AdminUser>> getAdminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<AdminUser> list = adminService.getAdminList(page, size, keyword);
        return ApiResponse.success(list);
    }

    @PostMapping("/admin")
    public ApiResponse<AdminUser> createAdmin(@RequestBody Map<String, Object> params) {
        AdminUser admin = adminService.createAdmin(
                (String) params.get("username"),
                (String) params.get("password"),
                (String) params.get("realName"),
                (String) params.get("phone"),
                (Integer) params.get("role")
        );
        return ApiResponse.success(admin);
    }

    @PutMapping("/admin/{adminId}/status")
    public ApiResponse<Void> updateAdminStatus(
            @PathVariable Long adminId,
            @RequestParam int status) {
        adminService.updateAdminStatus(adminId, status);
        return ApiResponse.success(null);
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getAdminStatistics(
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        if (adminId == null) {
            return ApiResponse.error(401, "Unauthorized");
        }
        Map<String, Object> statistics = adminService.getAdminStatistics(adminId);
        return ApiResponse.success(statistics);
    }
}
