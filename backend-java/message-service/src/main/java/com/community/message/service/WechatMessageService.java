package com.community.message.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WechatMessageService {

    private static final Logger log = LoggerFactory.getLogger(WechatMessageService.class);
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SUBSCRIBE_MSG_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    private static final String TOKEN_CACHE_KEY = "wechat:access_token";

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wechat.app-id:}")
    private String appId;

    @Value("${wechat.app-secret:}")
    private String appSecret;

    public WechatMessageService(RestTemplate restTemplate, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSubscribeMessage(Long userId, String templateId, Map<String, Object> data) {
        try {
            if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
                log.warn("WeChat app-id or app-secret not configured, skipping message send for user {}", userId);
                return;
            }

            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("Failed to obtain WeChat access token, cannot send message to user {}", userId);
                return;
            }

            String openId = getOpenIdByUserId(userId);
            if (openId == null || openId.isEmpty()) {
                log.warn("User {} has no associated WeChat openId, skipping message", userId);
                return;
            }

            String url = SUBSCRIBE_MSG_URL + "?access_token=" + accessToken;

            Map<String, Object> requestBody = new ConcurrentHashMap<>();
            requestBody.put("touser", openId);
            requestBody.put("template_id", templateId);

            Map<String, Map<String, String>> formattedData = new ConcurrentHashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> mapValue = (Map<String, String>) value;
                    formattedData.put(entry.getKey(), mapValue);
                } else {
                    formattedData.put(entry.getKey(), Map.of("value", String.valueOf(value)));
                }
            }
            requestBody.put("data", formattedData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());

            int errCode = result.get("errcode").asInt();
            if (errCode != 0) {
                String errMsg = result.get("errmsg").asText();
                log.error("WeChat subscribe message send failed: errcode={}, errmsg={}, userId={}, templateId={}",
                        errCode, errMsg, userId, templateId);
            } else {
                log.info("WeChat subscribe message sent successfully to user {}, template: {}", userId, templateId);
            }

        } catch (Exception e) {
            log.error("Failed to send subscribe message to user {}", userId, e);
        }
    }

    @SuppressWarnings("null")
    private String getAccessToken() {
        String cached = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        try {
            String url = TOKEN_URL + "?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());

            if (result.has("access_token")) {
                String token = result.get("access_token").asText();
                if (token == null || token.isEmpty()) {
                    log.error("WeChat access token is empty");
                    return null;
                }
                int expiresIn = result.has("expires_in") ? result.get("expires_in").asInt() : 7200;
                long ttlSeconds = Math.max(expiresIn - 300, 60);
                Duration ttl = Duration.ofSeconds(ttlSeconds);
                redisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, ttl);
                log.info("WeChat access token refreshed, expires in {}s", expiresIn);
                return token;
            } else {
                log.error("Failed to get WeChat access token: {}", response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception while fetching WeChat access token", e);
            return null;
        }
    }

    private String getOpenIdByUserId(Long userId) {
        return redisTemplate.opsForValue().get("wechat:openid:uid:" + userId);
    }

    public void sendOrderStatusMessage(Long userId, String orderTitle, String status) {
        Map<String, Object> data = Map.of(
                "thing1", Map.of("value", orderTitle.length() > 20 ? orderTitle.substring(0, 20) : orderTitle),
                "phrase2", Map.of("value", status),
                "time3", Map.of("value", java.time.LocalDateTime.now().toString())
        );
        sendSubscribeMessage(userId, "ORDER_STATUS_TEMPLATE", data);
    }

    public void sendPaymentSuccessMessage(Long userId, String amount, String orderTitle) {
        Map<String, Object> data = Map.of(
                "character_string1", Map.of("value", "Payment_" + System.currentTimeMillis()),
                "amount2", Map.of("value", amount),
                "thing3", Map.of("value", orderTitle.length() > 20 ? orderTitle.substring(0, 20) : orderTitle)
        );
        sendSubscribeMessage(userId, "PAYMENT_SUCCESS_TEMPLATE", data);
    }

    public void sendCreditChangeMessage(Long userId, int changeAmount, String reason) {
        String changeStr = (changeAmount >= 0 ? "+" : "") + changeAmount;
        Map<String, Object> data = Map.of(
                "number1", Map.of("value", changeStr),
                "thing2", Map.of("value", reason.length() > 20 ? reason.substring(0, 20) : reason)
        );
        sendSubscribeMessage(userId, "CREDIT_CHANGE_TEMPLATE", data);
    }

    public void sendWishClaimedMessage(Long userId, String wishTitle, String claimerName) {
        Map<String, Object> data = Map.of(
                "thing1", Map.of("value", wishTitle.length() > 20 ? wishTitle.substring(0, 20) : wishTitle),
                "name2", Map.of("value", claimerName.length() > 10 ? claimerName.substring(0, 10) : claimerName),
                "time3", Map.of("value", java.time.LocalDateTime.now().toString())
        );
        sendSubscribeMessage(userId, "WISH_CLAIMED_TEMPLATE", data);
    }
}
