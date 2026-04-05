package cersei.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class WalletAccessDeniedException extends RuntimeException {

    public WalletAccessDeniedException(String message) {
        super(message);
    }
}
