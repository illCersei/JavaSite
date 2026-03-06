package cersei.auth.exception;

import cersei.common.error.errors.ApiError;
import cersei.common.error.errors.ApiErrorResponse;
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
}