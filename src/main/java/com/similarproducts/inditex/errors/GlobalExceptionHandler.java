package com.similarproducts.inditex.errors;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(
                        "PRODUCT_NOT_FOUND",
                        safeMessage(ex, "Product not found")
                ));
    }

    @ExceptionHandler(GatewayTimeoutException.class)
    public ResponseEntity<ErrorDto> timeout(GatewayTimeoutException ex) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorDto(
                        "UPSTREAM_TIMEOUT",
                        safeMessage(ex, "Timeout while calling upstream service")
                ));
    }

    @ExceptionHandler(UpstreamException.class)
    public ResponseEntity<ErrorDto> upstream(UpstreamException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorDto(
                        "UPSTREAM_BAD_GATEWAY",
                        safeMessage(ex, "Upstream service returned an error")
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> unhandled(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(
                        "INTERNAL_ERROR",
                        safeMessage(ex, "Unexpected error")
                ));
    }

    private String safeMessage(Throwable ex, String fallback) {
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank()) ? fallback : msg;
    }
}
