package com.community.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class SqlInjectionProtectionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SqlInjectionProtectionFilter.class);

    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("('.+--)|(--\\s*$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bunion\\b.*\\bselect\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bselect\\b.*\\bfrom\\b.*\\bwhere\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\binsert\\b.*\\binto\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bdelete\\b.*\\bfrom\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bdrop\\b.*\\btable\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bupdate\\b.*\\bset\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bexec\\b.*\\bxp_\\b)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(;\\s*(drop|alter|create|truncate)\\s)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bor\\b\\s+\\d+\\s*=\\s*\\d+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\band\\b\\s+\\d+\\s*=\\s*\\d+)", Pattern.CASE_INSENSITIVE)
    };

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String queryString = request.getQueryString();
        if (queryString != null && containsSqlInjection(queryString)) {
            logger.warn("SQL injection attempt detected in query string: {}", queryString);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"message\":\"请求参数包含非法字符\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }
}
