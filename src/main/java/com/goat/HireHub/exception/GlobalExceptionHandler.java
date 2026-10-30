package com.goat.HireHub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Spring MVC's own exceptions (404 URL, 405, 415, missing param...)
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {

        ResponseEntity<Object> response =
                super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail pd) {
            normalize(pd);
        }
        return response;
    }

    //custom exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND,"Not Found",e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return problem(HttpStatus.CONFLICT,
                "Conflict","The request conflicts with existing data");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST,"Bad Request", ex.getMessage());
    }


    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException e) {
        return problem(HttpStatus.BAD_REQUEST,"Bad Request",e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflictException(ConflictException e){
        return problem(HttpStatus.CONFLICT,"Conflict",e.getMessage());
    }

    //security exceptions

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {

        return problem(HttpStatus.UNAUTHORIZED,"Authentication Failed","Invalid email or password. Please try again");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED,"Authentication Failed","Authentication failed or required");
    }


    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
       return problem(HttpStatus.FORBIDDEN,"Access denied","You do not have permission to perform this action");
    }

    //validation exceptions
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(validationProblem(errors));
    }


    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {

        Map<String, String> errors = new java.util.LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String paramName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.putIfAbsent(paramName, violation.getMessage());
        });

        return ResponseEntity.badRequest().body(validationProblem(errors));
    }


    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error -> {
                errors.putIfAbsent(paramName, error.getDefaultMessage());
            });
        });
        return ResponseEntity.badRequest().body(validationProblem(errors));
    }


    //fall back
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","Something went wrong. Please try again later");
    }

    //helpers
    private static ProblemDetail problem(HttpStatus status,String title,String detail){
         ProblemDetail pd=ProblemDetail.forStatusAndDetail(status,detail);
         pd.setTitle(title);
         normalize(pd);
         return pd;
    }

    private static void normalize(ProblemDetail pd){
        if(pd.getTitle()==null){
            HttpStatus s=HttpStatus.resolve(pd.getStatus());
            if (s!=null) pd.setTitle(s.getReasonPhrase());
        }

        if (pd.getProperties()==null || !pd.getProperties().containsKey("errors")){
             pd.setProperty("errors",Map.of());
        }

        if(!pd.getProperties().containsKey("timestamp")){
            pd.setProperty("timestamp", Instant.now().toString());
        }
    }

    private static ProblemDetail validationProblem(Map<String,String> errors){
         ProblemDetail pd=problem(HttpStatus.BAD_REQUEST,"Validation Failed","One or more fields are invalid");
         pd.setProperty("errors",errors);
         return pd;
    }
}