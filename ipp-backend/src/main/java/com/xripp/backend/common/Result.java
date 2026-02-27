package com.xripp.backend.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一返回结构
 *
 * @author XRIPP Team
 * @since 2026-02-08
 *
 * 使用示例:
 * - 成功返回数据: return Result.success(data);
 * - 成功无数据: return Result.success();
 * - 失败返回: return Result.error("错误信息");
 * - 自定义状态码: return Result.error(ResultCode.UNAUTHORIZED);
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 0-成功, 其他-失败
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    // ========================================================================
    // 构造方法（私有，只能通过静态方法创建）
    // ========================================================================

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ========================================================================
    // 成功返回方法
    // ========================================================================

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回（自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    // ========================================================================
    // 失败返回方法
    // ========================================================================

    /**
     * 失败返回（默认消息）
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg(), null);
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    /**
     * 失败返回（状态码枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // ========================================================================
    // 判断方法
    // ========================================================================

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}