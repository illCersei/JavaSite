package cersei.octopusservice.exception;

public class FightServiceException extends RuntimeException {

    private final Integer httpStatus;

    public FightServiceException(String message) {
        super(message);
        this.httpStatus = null;
    }

    public FightServiceException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}