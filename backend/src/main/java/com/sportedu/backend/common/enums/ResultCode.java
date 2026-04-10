package com.sportedu.backend.common.enums;

public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(40001, "参数错误"),
    BUSINESS_ERROR(40002, "业务校验失败"),
    UNAUTHORIZED(40101, "未登录或 token 失效"),
    FORBIDDEN(40301, "无权限"),
    NOT_FOUND(40401, "资源不存在"),
    CONFLICT(40901, "状态冲突"),
    INTERNAL_ERROR(50000, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
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
