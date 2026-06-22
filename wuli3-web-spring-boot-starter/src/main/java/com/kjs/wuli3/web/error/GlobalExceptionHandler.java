package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.CommonErrorCode;
import com.kjs.wuli3.core.error.ApplicationException;
import com.kjs.wuli3.web.ApiResponse;
import com.kjs.wuli3.web.RequestId;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException ex) {
        ApiResponse<Void> response = ApiResponse.failure(ex.errorCode(), safeMessage(ex), requestId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.failure(
                CommonErrorCode.VALIDATION_FAILED,
                CommonErrorCode.VALIDATION_FAILED.message(),
                requestId()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.failure(
                CommonErrorCode.INTERNAL_ERROR,
                CommonErrorCode.INTERNAL_ERROR.message(),
                requestId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private static String requestId() {
        String requestId = MDC.get(RequestId.MDC_KEY);
        return requestId == null ? "" : requestId;
    }

    private static String safeMessage(ApplicationException ex) {
        String message = ex.getMessage();
        return message == null ? ex.errorCode().message() : message;
    }
}
