package com.itheima.aiagent.exception;

import com.itheima.aiagent.common.ApiResponse;
import com.itheima.aiagent.common.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.concurrent.TimeoutException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getErrorCode(), resolveMessage(e, e.getErrorCode().getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ApiResponse<Void> handleParamException(Exception e) {
        String message = resolveParamMessage(e);
        log.warn("参数异常: {}", message);
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(TimeoutException.class)
    public ApiResponse<Void> handleTimeoutException(TimeoutException e) {
        log.warn("请求超时", e);
        return ApiResponse.fail(ErrorCode.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ApiResponse<Void> handleResourceAccessException(ResourceAccessException e) {
        log.error("AI 或外部服务调用失败", e);
        return ApiResponse.fail(ErrorCode.AI_SERVICE_ERROR, "AI 或外部服务暂时不可用，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.fail(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }

    private String resolveParamMessage(Exception e) {
        if (e instanceof MethodArgumentNotValidException validException) {
            return firstFieldErrorMessage(validException.getBindingResult().getFieldError());
        }
        if (e instanceof BindException bindException) {
            return firstFieldErrorMessage(bindException.getBindingResult().getFieldError());
        }
        if (e instanceof ConstraintViolationException constraintViolationException
                && !constraintViolationException.getConstraintViolations().isEmpty()) {
            return constraintViolationException.getConstraintViolations().iterator().next().getMessage();
        }
        if (e instanceof MethodArgumentTypeMismatchException typeMismatchException) {
            return typeMismatchException.getName() + " 参数类型不正确";
        }
        if (e instanceof HttpMessageNotReadableException) {
            return "请求体格式错误";
        }
        return resolveMessage(e, ErrorCode.PARAM_ERROR.getMessage());
    }

    private String firstFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return ErrorCode.PARAM_ERROR.getMessage();
        }
        return fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : fieldError.getField() + " 参数不正确";
    }

    private String resolveMessage(Exception e, String defaultMessage) {
        return e.getMessage() == null || e.getMessage().isBlank()
                ? defaultMessage
                : e.getMessage();
    }
}
