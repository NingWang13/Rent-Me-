package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.user.entity.AccessibilitySettings;
import com.community.user.mapper.AccessibilitySettingsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AccessibilityService {

    private static final Logger log = LoggerFactory.getLogger(AccessibilityService.class);

    private final AccessibilitySettingsMapper mapper;

    public AccessibilityService(AccessibilitySettingsMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getAccessibilitySettings(Long userId) {
        LambdaQueryWrapper<AccessibilitySettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessibilitySettings::getUserId, userId);
        AccessibilitySettings settings = mapper.selectOne(wrapper);

        if (settings == null) {
            return Map.of(
                    "largeFont", false,
                    "highContrast", false,
                    "simplifyMode", false,
                    "reduceMotion", false,
                    "fontSize", 32
            );
        }

        return Map.of(
                "largeFont", settings.getLargeFont() != null && settings.getLargeFont(),
                "highContrast", settings.getHighContrast() != null && settings.getHighContrast(),
                "simplifyMode", settings.getSimplifyMode() != null && settings.getSimplifyMode(),
                "reduceMotion", settings.getReduceMotion() != null && settings.getReduceMotion(),
                "fontSize", settings.getFontSize() != null ? settings.getFontSize() : 32
        );
    }

    public void updateAccessibilitySettings(Long userId, Map<String, Object> settings) {
        LambdaQueryWrapper<AccessibilitySettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessibilitySettings::getUserId, userId);
        AccessibilitySettings existing = mapper.selectOne(wrapper);

        AccessibilitySettings entity = (existing != null) ? existing : new AccessibilitySettings();

        entity.setUserId(userId);
        entity.setLargeFont((Boolean) settings.getOrDefault("largeFont", false));
        entity.setHighContrast((Boolean) settings.getOrDefault("highContrast", false));
        entity.setSimplifyMode((Boolean) settings.getOrDefault("simplifyMode", false));
        entity.setReduceMotion((Boolean) settings.getOrDefault("reduceMotion", false));
        entity.setFontSize((Integer) settings.getOrDefault("fontSize", 32));
        entity.setUpdatedAt(LocalDateTime.now());

        if (existing != null) {
            mapper.updateById(entity);
            log.info("Updated accessibility settings for user {}", userId);
        } else {
            mapper.insert(entity);
            log.info("Created accessibility settings for user {}", userId);
        }
    }

    public Map<String, Object> getDisplayConfig(Long userId) {
        Map<String, Object> userSettings = getAccessibilitySettings(userId);

        int baseFontSize = 28;
        boolean largeFont = (boolean) userSettings.getOrDefault("largeFont", false);
        if (largeFont) {
            baseFontSize = 36;
        }

        return Map.of(
                "fontSize", baseFontSize,
                "buttonHeight", largeFont ? 56 : 48,
                "iconSize", largeFont ? 32 : 24,
                "padding", largeFont ? 20 : 16,
                "borderRadius", 12
        );
    }
}
