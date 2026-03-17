package cersei.auth.exception;

import cersei.common.error.errors.ApiError;
import cersei.common.error.errors.ApiErrorResponse;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuth(AuthException ex) {
        ApiError error = new ApiError(
                "AUTH_ERROR",
                ex.getMessage(),
                null
        );
        ApiErrorResponse body = new ApiErrorResponse(List.of(error));
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiError(
                        "VALIDATION_ERROR",
                        fe.getDefaultMessage(),
                        fe.getField()
                ))
                .toList();

        return ResponseEntity.badRequest().body(new ApiErrorResponse(errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson() {
        ApiError error = new ApiError("BAD_REQUEST_BODY", "Некорректное тело запроса", null);
        return ResponseEntity.badRequest().body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
        ApiError error = new ApiError("INTERNAL_ERROR", "Внутренняя ошибка сервера", null);
        return ResponseEntity.status(500).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof PSQLException psql && psql.getServerErrorMessage() != null) {
            String constraint = psql.getServerErrorMessage().getConstraint();

            if ("uk_user_email".equals(constraint)) {
                ApiError error = new ApiError(
                        "EMAIL_ALREADY_EXISTS",
                        "Email уже зарегистрирован",
                        null
                );

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiErrorResponse(List.of(error)));
            }

            if ("uk_user_username".equals(constraint)) {
                ApiError error = new ApiError(
                        "USERNAME_ALREADY_EXISTS",
                        "Username уже занят",
                        null
                );

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiErrorResponse(List.of(error)));
            }
        }

        ApiError error = new ApiError(
                "DATA_INTEGRITY_VIOLATION",
                "Нарушение ограничения целостности данных",
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(List.of(error)));
    }
}