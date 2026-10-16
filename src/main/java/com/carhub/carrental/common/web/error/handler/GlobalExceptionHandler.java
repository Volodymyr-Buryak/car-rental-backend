package com.carhub.carrental.common.web.error.handler;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.carhub.carrental.common.exception.NotFoundException;
import org.springframework.web.context.request.WebRequest;
import com.carhub.carrental.common.web.error.model.ApiProblem;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.carhub.carrental.common.web.error.model.ApiProblemFactory;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApiProblemFactory problemFactory;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception, request={}", request.getDescription(false), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemFactory.internalServerError(request));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiProblem> handleNotFoundException(NotFoundException ex, WebRequest request) {
        log.debug("Resource not found: {}, request={}", ex.getMessage(), request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problemFactory.notFound(ex, request));
    }
}
