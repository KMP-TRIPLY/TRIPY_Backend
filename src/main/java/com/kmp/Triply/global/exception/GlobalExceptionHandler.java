package com.kmp.Triply.global.exception;

import com.kmp.Triply.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
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

    /** 필수 쿼리 파라미터 누락. 캐치올로 새면 클라이언트 잘못이 500 으로 보고된다. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("필수 파라미터가 없습니다: " + e.getParameterName()));
    }

    /** @Validated 가 붙은 컨트롤러의 @Min/@Max 등 파라미터 제약 위반. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("잘못된 요청 값입니다.");
        log.warn("Constraint violation: {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }

    /** 파싱 불가능한 JSON 바디 (깨진 JSON, 타입 불일치). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("Unreadable request body: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("요청 본문을 읽을 수 없습니다."));
    }

    /** 매핑된 핸들러가 없는 경로. 캐치올로 새면 단순 오타 URL 이 500 으로 보고된다. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("요청한 경로를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(HttpServletRequest request, Exception e) {
        // 로그가 없으면 500 을 받아도 원인을 찾을 수 없다.
        log.error("처리되지 않은 예외: {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}