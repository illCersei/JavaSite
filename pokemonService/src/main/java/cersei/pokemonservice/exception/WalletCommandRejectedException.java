package cersei.pokemonservice.exception;

import lombok.Getter;

@Getter
public class WalletCommandRejectedException extends RuntimeException {

    private final String errorCode;

    public WalletCommandRejectedException(String errorCode, String detail) {
        super(detail != null ? detail : errorCode);
        this.errorCode = errorCode;
    }
}
