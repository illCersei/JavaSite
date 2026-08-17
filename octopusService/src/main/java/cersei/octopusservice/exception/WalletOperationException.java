package cersei.octopusservice.exception;

public class WalletOperationException extends RuntimeException {

    private final Integer httpStatus;

    public WalletOperationException(String message) {
        super(message);
        this.httpStatus = null;
    }

    public WalletOperationException(Integer httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
