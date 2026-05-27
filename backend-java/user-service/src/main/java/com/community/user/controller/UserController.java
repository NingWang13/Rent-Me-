package com.community.user.controller;

import com.community.common.exception.BusinessException;
import com.community.common.exception.ErrorCode;
import com.community.common.response.ApiResponse;
import com.community.common.response.PageResponse;
import com.community.common.security.JwtTokenProvider;
import com.community.common.security.PasswordEncoder;
import com.community.user.entity.User;
import com.community.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度不能少于6位");
        }
        User existing = userService.getUserByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreditScore(100);
        user.setStatus(1);
        User created = userService.createUser(user);
        created.setPassword(null);
        return ApiResponse.success(created);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getUserType() != null && user.getUserType() == 3 ? "ADMIN" : "USER");
        return ApiResponse.success(token);
    }

    @GetMapping("/info")
    public ApiResponse<User> getUserInfo(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/info")
    public ApiResponse<User> updateUserInfo(@RequestHeader("X-User-Id") Long userId, @RequestBody User user) {
        user.setId(userId);
        User updated = userService.updateUser(userId, user);
        updated.setPassword(null);
        return ApiResponse.success(updated);
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestHeader("X-User-Id") Long userId, @RequestBody PasswordUpdateRequest request) {
        if (request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new BusinessException(400, "密码不能为空");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(400, "新密码不能与旧密码相同");
        }
        if (request.getNewPassword().length() < 6) {
            throw new BusinessException(400, "新密码长度不能少于6位");
        }
        User user = userService.getUserById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateUser(userId, user);
        return ApiResponse.success();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<User>> searchUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResponse<User> result = userService.getUserList(page, size, keyword);
        result.getList().forEach(u -> u.setPassword(null));
        return ApiResponse.success(result);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class PasswordUpdateRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
