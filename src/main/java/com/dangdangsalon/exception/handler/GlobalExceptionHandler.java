package com.dangdangsalon.exception.handler;

import com.dangdangsalon.exception.TokenExpiredException;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        log.error(ex.getMessage(), ex);

        ApiError<String> error = ApiUtil.error(statusCode.value(), "알 수 없는 오류가 발생했습니다. 문의 바랍니다.");
        return super.handleExceptionInternal(ex, error, headers, statusCode, request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class
    })
    protected ResponseEntity<?> handleIllegalArgumentException(Exception e) {
        ApiError<String> error = ApiUtil.error(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(error);
    }

    @ExceptionHandler({
            IllegalStateException.class
    })
    protected ResponseEntity<?> handleIllegalStateException(Exception e) {
        ApiError<String> error = ApiUtil.error(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).body(error);
    }

    @ExceptionHandler(TokenExpiredException.class)
    protected ResponseEntity<?> handleExpiredJwtException(TokenExpiredException e) {
        ApiError<String> error = ApiUtil.error(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(error);
    }
}
