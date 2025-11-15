package cersei.auth.exception;

import cersei.auth.error.ApiError;
import cersei.auth.error.ApiErrorResponse;
import cersei.auth.error.ErrorCode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handlerAuthException(AuthException ex) {
        ApiError error = new ApiError(
                ErrorCode.INVALID_CREDENTIALS.getCode(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.
                status(ex.getHttpStatus()).
                body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownFieldException() {
        ApiError error = new ApiError(
                ErrorCode.BAD_REQUEST_BODY.getCode(),
                "Несоответствие ожидаемой структуре запроса",
                null
        );
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiError(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        fieldError.getDefaultMessage(),
                        fieldError.getField()
                )).toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}
