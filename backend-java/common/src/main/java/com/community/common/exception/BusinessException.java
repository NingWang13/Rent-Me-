package com.community.common.exception;

/**
 * 业务异常类
 * 用于抛出业务逻辑相关的异常
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 创建业务异常
     */
    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 创建业务异常（使用默认状态码500）
     */
    public static BusinessException of(String message) {
        return new BusinessException(500, message);
    }
}
