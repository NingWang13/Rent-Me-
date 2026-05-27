package com.community.common.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {

    SUCCESS(200, "success"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已被禁用"),
    VERIFY_CODE_ERROR(1003, "验证码错误"),
    VERIFY_CODE_EXPIRED(1004, "验证码已过期"),

    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_INVALID(2002, "订单状态不允许此操作"),
    ORDER_TIMEOUT(2003, "订单已超时"),

    BALANCE_NOT_ENOUGH(3001, "余额不足"),
    PAYMENT_PASSWORD_ERROR(3002, "支付密码错误"),
    PAYMENT_TIMEOUT(3003, "支付已超时"),

    FRIEND_APPLY_EXISTS(4001, "好友申请已存在"),
    ALREADY_FRIENDS(4002, "已是好友"),
    GROUP_FULL(4003, "群聊人数已满"),

    CREDIT_NOT_ENOUGH(5001, "积分不足"),
    CHECKIN_DUPLICATE(5002, "今日已签到"),

    WISH_NOT_FOUND(6001, "心愿不存在"),
    ACTIVITY_NOT_FOUND(7001, "活动不存在"),
    ACTIVITY_FULL(7002, "活动人数已满"),
    ACTIVITY_ENDED(7003, "活动已结束");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
