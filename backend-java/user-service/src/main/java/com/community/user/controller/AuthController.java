package com.community.user.controller;

import com.community.common.response.ApiResponse;
import com.community.common.security.JwtTokenProvider;
import com.community.user.entity.User;
import com.community.user.service.UserService;
import com.community.user.dto.WechatLoginRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Value("${wechat.app-id}")
    private String appid;

    @Value("${wechat.app-secret}")
    private String secret;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 微信小程序登录接口
     * @param request 包含code的请求体
     * @return JWT token
     */
    @PostMapping("/wechat-login")
    public ApiResponse<Map<String, Object>> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        log.info("微信登录请求: code={}", request.getCode());

        // 1. 使用code获取openid
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, request.getCode());

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();

            if (body == null || body.containsKey("errcode")) {
                String errmsg = body != null ? (String) body.get("errmsg") : "未知错误";
                log.error("微信登录失败: {}", errmsg);
                return ApiResponse.error(500, "微信登录失败: " + errmsg);
            }

            String openid = (String) body.get("openid");
            log.info("微信登录成功: openid={}", openid);

            // 2. 根据openid查找用户
            User user = userService.getUserByOpenid(openid);

            // 3. 如果用户不存在，创建新用户
            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setUsername("user_" + UUID.randomUUID().toString().substring(0, 8));
                user.setNickname(request.getNickname() != null ? request.getNickname() : "微信用户");
                user.setAvatar(request.getAvatar());
                user.setCreditScore(100);
                user.setStatus(1);
                user.setDeleted(0);
                user = userService.createUser(user);
                log.info("创建新用户: userId={}", user.getId());
            }

            // 4. 生成JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), "USER");

            // 5. 返回token和用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("微信登录异常", e);
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }
}
