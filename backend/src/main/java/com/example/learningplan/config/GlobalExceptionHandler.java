package com.example.learningplan.config;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));
        log.error("[VALIDATION] {} {} - {}", request.getMethod(), request.getRequestURI(), message, ex);
        return ResponseEntity.badRequest().body(message.isEmpty() ? "参数校验失败" : message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.joining("; "));
        log.error("[VALIDATION] {} {} - {}", request.getMethod(), request.getRequestURI(), message, ex);
        return ResponseEntity.badRequest().body(message.isEmpty() ? "参数校验失败" : message);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.error("[ILLEGAL_STATE] {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.error("[AUTH] {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(401).body("账号或密码错误");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        log.error("[AUTH] {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(401).body("登录失败，请检查账号和密码");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex, HttpServletRequest request) {
        log.error("[ERROR] {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(500).body("服务器异常，请稍后重试");
    }

    private String formatFieldError(FieldError error) {
        if (error == null) return "";
        String field = error.getField();
        String msg = error.getDefaultMessage();
        if (field == null) field = "";
        if (msg == null) msg = "";
        return (field + " " + msg).trim();
    }
}
