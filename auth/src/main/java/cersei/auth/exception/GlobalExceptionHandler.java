package cersei.auth.exception;

import cersei.auth.error.ApiLoginError;
import cersei.auth.error.ApiLoginErrorResponse;
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
    public ResponseEntity<ApiLoginErrorResponse> handlerAuthException(AuthException ex) {
        ApiLoginError error = new ApiLoginError(
                ErrorCode.INVALID_CREDENTIALS.getCode(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.
                status(ex.getHttpStatus()).
                body(new ApiLoginErrorResponse(List.of(error)));
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<ApiLoginErrorResponse> handleUnknownFieldException() {
        ApiLoginError error = new ApiLoginError(
                ErrorCode.BAD_REQUEST_BODY.getCode(),
                "Несоответствие ожидаемой структуре запроса",
                null
        );
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(new ApiLoginErrorResponse(List.of(error)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ApiLoginError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiLoginError(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        fieldError.getDefaultMessage(),
                        fieldError.getField()
                )).toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}
