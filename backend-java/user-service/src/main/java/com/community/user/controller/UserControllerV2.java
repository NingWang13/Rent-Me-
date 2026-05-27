package com.community.user.controller;

import com.community.common.annotation.RateLimit;
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
@RequestMapping("/api/v2/user")
public class UserControllerV2 {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserControllerV2(UserService userService, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @RateLimit(key = "register", permitsPerSecond = 5)
    public ApiResponse<User> register(@RequestBody RegisterRequest request) {
        User existing = userService.getUserByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType() != null ? request.getUserType() : 1);
        user.setCreditScore(100);
        user.setStatus(1);
        User created = userService.createUser(user);
        created.setPassword(null);
        return ApiResponse.success(created);
    }

    @PostMapping("/login")
    @RateLimit(key = "login", permitsPerSecond = 10)
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
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
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setToken(token);
        response.setUserType(user.getUserType());
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserInfo(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody UpdateRequest request) {
        User existing = userService.getUserById(id);
        if (request.getNickname() != null) existing.setNickname(request.getNickname());
        if (request.getAvatarUrl() != null) existing.setAvatarUrl(request.getAvatarUrl());
        if (request.getPhone() != null) existing.setPhone(request.getPhone());
        User updated = userService.updateUser(id, existing);
        updated.setPassword(null);
        return ApiResponse.success(updated);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<User>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<User> result = userService.getUserList(page, size, keyword);
        result.getList().forEach(u -> u.setPassword(null));
        return ApiResponse.success(result);
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String phone;
        private Integer userType;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Integer getUserType() { return userType; }
        public void setUserType(Integer userType) { this.userType = userType; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private Long userId;
        private String username;
        private String token;
        private Integer userType;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Integer getUserType() { return userType; }
        public void setUserType(Integer userType) { this.userType = userType; }
    }

    public static class UpdateRequest {
        private String nickname;
        private String avatarUrl;
        private String phone;

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
