package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.exception.ErrorResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.exception.ValidationErrorDetailDTO;
import com.auth0.jwt.exceptions.JWTCreationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException; // Importar
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler de exceções global para toda a aplicação.
 * Captura exceções específicas e as transforma em respostas HTTP padronizadas
 * usando o formato {@link ErrorResponseDTO}.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Constrói o DTO de resposta de erro padrão.
     *
     * @param status O status HTTP da resposta.
     * @param message A mensagem de erro principal.
     * @param path A URL da requisição que causou o erro.
     * @param details Detalhes adicionais do erro (usado para validação).
     * @return Um objeto ErrorResponseDTO preenchido.
     */
    private ErrorResponseDTO buildErrorResponse(HttpStatus status, String message, String path, List<?> details) {
        return new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
    }

    /**
     * Sobrecarga de buildErrorResponse para erros sem detalhes adicionais.
     */
    private ErrorResponseDTO buildErrorResponse(HttpStatus status, String message, String path) {
        return buildErrorResponse(status, message, path, null);
    }

    /**
     * Handler para exceções de autenticação do Spring Security. Retorna 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> threatAuthenticationError(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handler para TaskNotFoundException. Retorna 404 Not Found.
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> threatTaskNotFound(TaskNotFoundException ex, HttpServletRequest request) {
        log.warn(ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handler para UserNotFoundException. Retorna 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> threatUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn(ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handler para UsernameNotFoundException do Spring Security. Retorna 404 Not Found.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> threatUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        log.warn("Username not found: {}", ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handler para UserAlreadyExistsException. Retorna 409 Conflict.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> threatUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn(ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handler para operações proibidas de usuário (ex: auto-deleção). Retorna 403 Forbidden.
     */
    @ExceptionHandler({SecurityException.class, SelfDeletionNotAllowedException.class, LastAdminDeletionNotAllowedException.class,
            SelfRoleChangeNotAllowedException.class, LastAdminDemotionNotAllowedException.class})
    public ResponseEntity<ErrorResponseDTO> threatForbiddenOperations(RuntimeException ex, HttpServletRequest request) {
        log.warn(ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handler para erros na criação de token JWT. Retorna 500 Internal Server Error.
     */
    @ExceptionHandler(JWTCreationException.class)
    public ResponseEntity<ErrorResponseDTO> threatJwtCreationError(JWTCreationException ex, HttpServletRequest request) {
        log.error("Error while creating JWT token: {}", ex.getMessage(), ex);
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred while generating the access token.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handler para erros de validação de DTOs (@Valid). Retorna 400 Bad Request com detalhes dos campos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> threatValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorDetailDTO> validationErrors = ex.getFieldErrors().stream()
                .map(ValidationErrorDetailDTO::new)
                .collect(Collectors.toList());
        log.warn("Validation errors occurred: {}", validationErrors);
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), validationErrors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handler para erros de tipo de parâmetro na URL. Retorna 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> threatTypeMismatchError(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid URL parameter: " + ex.getName();
        log.warn("Method argument type mismatch for parameter '{}'. Required type: {}, Value: '{}'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "N/A", ex.getValue());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handler para corpo de requisição ausente ou malformado. Retorna 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> threatMissingBodyError(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Required request body is missing or malformed.";
        log.warn(message + ": " + ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handler para método HTTP não suportado. Retorna 405 Method Not Allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> threatMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn(ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handler para tipo de mídia não suportado. Retorna 415 Unsupported Media Type.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> threatMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn(ex.getMessage());
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    /**
     * Handler para recursos não encontrados (endpoints inexistentes). Retorna 404 Not Found.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> threatNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String message = "Resource not found at path: " + ex.getResourcePath();
        log.warn(message);
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handler genérico para qualquer outra exceção não tratada. Retorna 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> threatGeneralError(Exception ex, HttpServletRequest request) {
        log.error("An internal server error occurred: {}", ex.getMessage(), ex);
        ErrorResponseDTO errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
