package com.popovrnd.springbootobservabilitysetup.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.";

        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            message += " | Caused by: " + cause.getMessage();
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, message
        );
        problemDetail.setProperty("error", ex.getClass().getSimpleName());
        if (cause != null) {
            problemDetail.setProperty("cause", cause.getClass().getSimpleName());
        }

        // Full logging for developers
        log.error("Unhandled exception occurred", ex);

        return problemDetail;
    }

}
