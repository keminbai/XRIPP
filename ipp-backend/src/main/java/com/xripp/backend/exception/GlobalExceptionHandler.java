package com.xripp.backend.exception;

import com.xripp.backend.common.Result;
import com.xripp.backend.common.ResultCode;
import com.xripp.backend.common.V3Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private boolean isV3(HttpServletRequest request) {
        return request != null
                && request.getRequestURI() != null
                && request.getRequestURI().startsWith("/api/v3/");
    }

    private ResponseEntity<?> buildError(HttpServletRequest request, HttpStatus status, Integer code, String message) {
        if (isV3(request)) {
            return ResponseEntity.status(status).body(V3Response.error(String.valueOf(code), message));
        }
        return ResponseEntity.status(status).body(Result.error(code, message));
    }

    private ResponseEntity<?> buildError(HttpServletRequest request, HttpStatus status, ResultCode resultCode) {
        if (isV3(request)) {
            return ResponseEntity.status(status).body(
                    V3Response.error(String.valueOf(resultCode.getCode()), resultCode.getMsg())
            );
        }
        return ResponseEntity.status(status).body(Result.error(resultCode));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return buildError(request, HttpStatus.OK, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return buildError(request, HttpStatus.BAD_REQUEST, ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", errorMsg);
        return buildError(request, HttpStatus.BAD_REQUEST, ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", errorMsg);
        return buildError(request, HttpStatus.BAD_REQUEST, ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String errorMsg = String.format("参数'%s'类型错误", e.getName());
        log.warn("参数类型错误: {}", e.getMessage());
        return buildError(request, HttpStatus.BAD_REQUEST, ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("认证失败: {}", e.getMessage());
        return buildError(request, HttpStatus.UNAUTHORIZED, ResultCode.LOGIN_FAILED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足: {}", e.getMessage());
        return buildError(request, HttpStatus.FORBIDDEN, ResultCode.PERMISSION_DENIED);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.error("数据库唯一键冲突: {}", e.getMessage());
        return buildError(request, HttpStatus.CONFLICT, ResultCode.DB_DUPLICATE_KEY);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性约束违反: {}", e.getMessage());
        if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY")) {
            return buildError(request, HttpStatus.CONFLICT, ResultCode.DB_FOREIGN_KEY_VIOLATION);
        }
        return buildError(request, HttpStatus.CONFLICT, ResultCode.DB_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: ", e);
        return buildError(request, HttpStatus.INTERNAL_SERVER_ERROR,
                ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统繁忙，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: ", e);
        return buildError(request, HttpStatus.INTERNAL_SERVER_ERROR,
                ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
