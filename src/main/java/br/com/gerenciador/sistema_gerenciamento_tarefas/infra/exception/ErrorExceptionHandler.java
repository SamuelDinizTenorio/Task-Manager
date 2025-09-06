package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class ErrorExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity threatNotFoundError(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity threatValidationError(MethodArgumentNotValidException ex) {
        var errors = ex.getFieldErrors();
        var validationErrors = errors.stream()
                .map(ValidationDto::new)
                .toList();
        log.warn("Validation errors occurred: {}", validationErrors);
        return ResponseEntity.badRequest()
                .body(validationErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity threatTypeMismatchError(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch for parameter '{}'. Required type: {}, Value: '{}'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "N/A", ex.getValue());
        return ResponseEntity.badRequest()
                .body("Invalid URL parameter: " + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity threatMissingBodyError(HttpMessageNotReadableException ex) {
        log.warn("Required request body is missing or malformed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body("Required request body is missing or malformed.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> threatNoResourceFound(NoResourceFoundException ex) {
        log.warn("Resource not found for request path: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Resource not found at path: " + ex.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity threatGeneralError(Exception ex) {
        log.error("An internal server error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An internal server error occurred.");
    }

    private record ValidationDto(String field, String message) {
        public ValidationDto(org.springframework.validation.FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }
}