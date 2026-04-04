package cersei.pokemonservice.exception;

import cersei.common.error.errors.ApiError;
import cersei.common.error.errors.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(PokemonNotFoundException ex) {
        ApiError error = new ApiError("POKEMON_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(WalletCommandRejectedException.class)
    public ResponseEntity<ApiErrorResponse> walletCommand(WalletCommandRejectedException ex) {
        String code = ex.getErrorCode() != null ? ex.getErrorCode() : "WALLET_ERROR";
        HttpStatus status = switch (code) {
            case "INSUFFICIENT_FUNDS" -> HttpStatus.PAYMENT_REQUIRED;
            case "TIMEOUT" -> HttpStatus.GATEWAY_TIMEOUT;
            case "BAD_REQUEST" -> HttpStatus.BAD_REQUEST;
            case "INVALID_TOKEN" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_GATEWAY;
        };
        ApiError error = new ApiError(code, ex.getMessage(), null);
        return ResponseEntity.status(status).body(new ApiErrorResponse(List.of(error)));
    }
}
