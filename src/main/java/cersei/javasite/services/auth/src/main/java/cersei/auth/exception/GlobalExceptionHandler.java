package cersei.auth.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handlerException(AuthException authException){
        return ResponseEntity
                .status(authException.getHttpStatus())
                .body(Map.of("message", authException.getMessage()));
    }
}
