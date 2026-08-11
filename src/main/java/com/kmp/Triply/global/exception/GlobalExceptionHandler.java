package com.kmp.Triply.global.exception;

import com.kmp.Triply.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e, HttpServletRequest request) {
        // 의도된 예외라 스택트레이스는 남기지 않는다
        log.warn("{} {} -> {}", request.getMethod(), request.getRequestURI(), e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(message));
    }

    /** 경로 변수/쿼리 파라미터 타입이 안 맞는 건 클라이언트 잘못이라 400 이다. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {}='{}'", e.getName(), e.getValue());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("잘못된 요청 값입니다: " + e.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        // 이게 없으면 500 이 떠도 로그에 아무 흔적이 없어 원인을 못 찾는다
        log.error("Unhandled exception: {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
