package com.xripp.backend.exception;

import com.xripp.backend.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author XRIPP Team
 * @since 2026-02-08
 *
 * 使用示例:
 * - throw new BusinessException("余额不足");
 * - throw new BusinessException(ResultCode.BENEFIT_INSUFFICIENT);
 * - throw new BusinessException(2001, "会员不存在");
 *
 * ⚠️ 重要约定:
 * 1. 所有Service层的业务错误必须抛出此异常
 * 2. 不要在Controller中try-catch此异常
 * 3. 由GlobalExceptionHandler统一处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    // ========================================================================
    // 构造方法
    // ========================================================================

    /**
     * 构造方法：默认错误码500
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造方法：使用状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    /**
     * 构造方法：自定义错误码和消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法：带原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造方法：状态码枚举 + 原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMsg(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    // ========================================================================
    // 重写方法
    // ========================================================================

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}