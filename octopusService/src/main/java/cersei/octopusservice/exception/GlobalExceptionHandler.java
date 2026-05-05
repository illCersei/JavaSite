package cersei.octopusservice.exception;

import cersei.common.error.errors.ApiError;
import cersei.common.error.errors.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OctopusNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(OctopusNotFoundException ex) {
        ApiError error = new ApiError("OCTOPUS_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(WalletOperationException.class)
    public ResponseEntity<ApiErrorResponse> walletError(WalletOperationException ex) {
        ApiError error = new ApiError("WALLET_ERROR", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException ex) {
        ApiError error = new ApiError("BAD_REQUEST", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(List.of(error)));
    }
}
