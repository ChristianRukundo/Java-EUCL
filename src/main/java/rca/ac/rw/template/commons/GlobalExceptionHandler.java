package rca.ac.rw.template.commons; 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import rca.ac.rw.template.commons.exceptions.BadRequestException;
import rca.ac.rw.template.commons.response.ErrorResponse;
import rca.ac.rw.template.commons.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.commons.exceptions.UnauthenticatedException;
import rca.ac.rw.template.commons.exceptions.ValidationException;
import org.springframework.security.core.AuthenticationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildResponseEntity(HttpStatus status, String message, String path, Map<String, List<String>> fieldErrors) {
        ErrorResponse errorResponse;
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            errorResponse = new ErrorResponse(status, message, path, fieldErrors);
        } else {
            errorResponse = new ErrorResponse(status, message, path);
        }
        return new ResponseEntity<>(errorResponse, status);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(HttpStatus status, String message, String path) {
        return buildResponseEntity(status, message, path, null);
    }

    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("ResourceNotFoundException: {} on path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.warn("ValidationException: {} on path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticatedException(UnauthenticatedException ex, HttpServletRequest request) {
        log.warn("UnauthenticatedException: {} on path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("MethodArgumentNotValidException: Validation failed for request on path: {}", request.getRequestURI(), ex);
        Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        String message = "Validation failed. Check 'fieldErrors' for details.";
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("AuthenticationException: {} on path: {}", ex.getMessage(), request.getRequestURI());
        
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }



    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("ConstraintViolationException: Validation failed for request on path: {}", request.getRequestURI(), ex);
        Map<String, List<String>> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        violation -> getFieldNameFromPath(violation.getPropertyPath().toString()),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));
        String message = "Validation failed. Check 'fieldErrors' for details.";
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), fieldErrors);
    }

    
    private String getFieldNameFromPath(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return "unknownField";
        }
        
        String[] parts = propertyPath.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : propertyPath;
    }


    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("HttpMessageNotReadableException: Malformed request on path: {}. Details: {}", request.getRequestURI(), ex.getMessage());
        String message = "Malformed JSON request or invalid input format.";
        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ifx) {
            message = "Invalid value '" + ifx.getValue() + "' for field '" + ifx.getPath().get(ifx.getPath().size()-1).getFieldName() + "'. Expected type: " + ifx.getTargetType().getSimpleName();
        } else if (ex.getCause() instanceof com.fasterxml.jackson.core.JsonParseException jpe) {
            message = "Malformed JSON request: " + jpe.getOriginalMessage();
        }
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    /**
     * Handles custom {@link BadRequestException}.
     * This is for general "bad request" scenarios not covered by more specific validation exceptions.
     *
     * @param ex      The BadRequestException.
     * @param request The HttpServletRequest.
     * @return A ResponseEntity with HTTP 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class) 
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.warn("BadRequestException: {} on path: {}", ex.getMessage(), request.getRequestURI(), ex); 
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("MissingServletRequestParameterException: Required parameter '{}' is missing on path: {}", ex.getParameterName(), request.getRequestURI());
        String message = String.format("Required request parameter '%s' of type %s is not present.", ex.getParameterName(), ex.getParameterType());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("MethodArgumentTypeMismatchException: Parameter '{}' has invalid value '{}' on path: {}", ex.getName(), ex.getValue(), request.getRequestURI());
        String message = String.format("Parameter '%s' should be of type '%s' but value was '%s'.",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown", ex.getValue());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }


    
    
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) 
    public ResponseEntity<ErrorResponse> handleGenericRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Unhandled RuntimeException: {} on path: {}", ex.getMessage(), request.getRequestURI(), ex); 
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred. Please try again later.", request.getRequestURI());
    }

    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception: {} on path: {}", ex.getMessage(), request.getRequestURI(), ex);
        String message = "An unexpected error occurred.";
        if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            message = "Database integrity constraint violated. This could be due to duplicate data or invalid foreign key references.";
            return buildResponseEntity(HttpStatus.CONFLICT, message, request.getRequestURI()); 
        }
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI());
    }
}