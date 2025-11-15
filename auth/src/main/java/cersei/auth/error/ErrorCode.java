package cersei.auth.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR(1000),
    INVALID_CREDENTIALS(1001),
    INVALID_TOKEN(1002),
    BAD_REQUEST_BODY(1003);

    private final int code;

    ErrorCode(int code){
        this.code = code;
    }
}
