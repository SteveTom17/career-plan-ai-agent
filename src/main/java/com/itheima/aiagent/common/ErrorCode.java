package com.itheima.aiagent.common;

public enum ErrorCode {

    SUCCESS(0, "ok"),

    PARAM_ERROR(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未登录或无权限"),
    FORBIDDEN(40300, "禁止访问"),
    NOT_FOUND(40400, "资源不存在"),
    REQUEST_TIMEOUT(40800, "请求超时"),

    SYSTEM_ERROR(50000, "系统内部异常"),
    AI_SERVICE_ERROR(50010, "AI 服务调用失败"),
    VECTOR_STORE_ERROR(50020, "知识库检索失败"),
    TOOL_CALL_ERROR(50030, "工具调用失败"),
    MCP_SERVICE_ERROR(50040, "MCP 服务调用失败");

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
