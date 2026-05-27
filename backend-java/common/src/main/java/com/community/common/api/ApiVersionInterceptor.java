package com.community.common.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);
    private static final String CURRENT_VERSION = "v1";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        ApiVersionAnnotation methodAnnotation = handlerMethod.getMethodAnnotation(ApiVersionAnnotation.class);
        ApiVersionAnnotation classAnnotation = handlerMethod.getBeanType().getAnnotation(ApiVersionAnnotation.class);

        ApiVersionAnnotation annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        if (annotation == null) {
            return true;
        }

        String requestedVersion = request.getHeader("X-API-Version");
        if (requestedVersion == null || requestedVersion.isEmpty()) {
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                String[] parts = path.split("/");
                if (parts.length >= 3) {
                    requestedVersion = parts[2];
                }
            }
        }

        if (requestedVersion != null && !requestedVersion.equals(annotation.value())) {
            logger.warn("API version mismatch: requested={}, annotated={}", requestedVersion, annotation.value());
        }

        if (annotation.deprecated()) {
            String sunsetDate = annotation.sunsetDate();
            response.setHeader("Deprecation", "true");
            response.setHeader("Sunset", sunsetDate);
            response.setHeader("Link", "</api/" + CURRENT_VERSION + ">; rel=\"successor-version\"");
            logger.warn("Deprecated API version accessed: {}", annotation.value());
        }

        response.setHeader("X-API-Version", annotation.value());
        return true;
    }
}
