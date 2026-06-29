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
        int status = ex.getHttpStatus() != null ? ex.getHttpStatus() : 502;
        HttpStatus httpStatus = switch (status) {
            case 402 -> HttpStatus.PAYMENT_REQUIRED; // insufficient funds
            case 403 -> HttpStatus.FORBIDDEN;
            case 408 -> HttpStatus.GATEWAY_TIMEOUT;
            case 504 -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.BAD_GATEWAY;
        };
        ApiError error = new ApiError("WALLET_ERROR", ex.getMessage(), null);
        return ResponseEntity.status(httpStatus).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ApiErrorResponse> idemConflict(IdempotencyConflictException ex) {
        ApiError error = new ApiError("IDEMPOTENCY_IN_PROGRESS", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> itemNotFound(ItemNotFoundException ex) {
        ApiError error = new ApiError("ITEM_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(InsufficientItemQuantityException.class)
    public ResponseEntity<ApiErrorResponse> insufficientItems(InsufficientItemQuantityException ex) {
        ApiError error = new ApiError("INSUFFICIENT_ITEM_QUANTITY", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException ex) {
        ApiError error = new ApiError("BAD_REQUEST", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(List.of(error)));
    }
}
