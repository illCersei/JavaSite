package cersei.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FeatureDisabledException extends RuntimeException {

    public FeatureDisabledException(String message) {
        super(message);
    }
}
