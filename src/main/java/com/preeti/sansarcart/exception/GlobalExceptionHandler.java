package com.preeti.sansarcart.exception;

import com.preeti.sansarcart.exception.custom.ListValidationException;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.TokenException;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static com.preeti.sansarcart.common.I18n.i18n;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @Value("${app.debug.stacktrace:false}")
    private boolean showStackTrace;

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleValidationException(ValidationException ex) {
        return ApiResponse.error(i18n("exception.validation.error"), ex.getMessage());
    }

    @ExceptionHandler(TokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleTokenException(TokenException ex) {
        return ApiResponse.error(i18n("exception.token.error"), ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleAuthenticationException(AuthenticationException ex) {
        return ApiResponse.error(i18n("exception.authentication.error"), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleResourceNotFoundException(ResourceNotFound ex) {
        return ApiResponse.error(i18n("exception.resource.not.found"), ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<String> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "N/A";

        String message = i18n("exception.method.not.allowed.detail") + ": " + supportedMethods;

        return ApiResponse.error(i18n("exception.method.not.allowed"),message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ApiResponse.error(i18n("exception.json.error"),null);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<List<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).toList();

        return ApiResponse.error(i18n("exception.validation.error"),errors);
    }

    @ExceptionHandler(ListValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<List<String>> handleListValidationException(ListValidationException ex) {
        return ApiResponse.error(i18n("exception.validation.error"), ex.getErrors());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String value = ex.getValue().toString();
        String message = i18n("validation.param.type.invalid", name, value, ex.getRequiredType().getSimpleName());

        return ResponseEntity.badRequest().body(ApiResponse.error(message,null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleBadCredentialsException(BadCredentialsException ex) {
        return ApiResponse.error(i18n("exception.bad.credentials"), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleDataIntegrityException(DataIntegrityViolationException ex) {
        return ApiResponse.error(i18n("exception.data.integrity.violation"), ex.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleAccountStatusException(AccountStatusException ex) {
        return ApiResponse.error(i18n("exception.account.invalid"),  ex.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleJwtException(JwtException ex) {
        return ApiResponse.error(i18n("exception.jwt.invalid"),  ex.getMessage());
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(i18n("exception.resource.not.found"), null));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDenied(AuthorizationDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(i18n("exception.access.denied"), null));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleGlobalException(Exception ex) {
        if (showStackTrace) {
            ex.printStackTrace();
        }else {
            log.error("Unhandled exception:{}", ex.getMessage());
        }
        return ApiResponse.error(i18n("exception.internal.server.error"), ex.getMessage());
    }
}
