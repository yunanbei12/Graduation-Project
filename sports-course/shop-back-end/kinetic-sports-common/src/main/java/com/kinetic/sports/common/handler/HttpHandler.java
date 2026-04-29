package com.kinetic.sports.common.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.kinetic.sports.common.exception.KineticSportsBindException;
import com.kinetic.sports.common.response.ServerResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class HttpHandler {

    @ExceptionHandler(KineticSportsBindException.class)
    public ResponseEntity<ServerResponseEntity<Void>> handleKineticSportsBindException(KineticSportsBindException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServerResponseEntity.fail(e.getCode(), e.getMessage()));
    }

    /**
     * Sa-Token 未登录异常处理
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ServerResponseEntity<Void>> handleNotLoginException(NotLoginException e) {
        log.warn("未登录或token无效: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ServerResponseEntity.fail(401, "请重新登录"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ServerResponseEntity<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数或业务校验异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServerResponseEntity.fail(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServerResponseEntity<Void>> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ServerResponseEntity.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器出了点小差"));
    }
}
