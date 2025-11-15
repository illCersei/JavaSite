package cersei.auth.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private int code;
    private String message;
    private String field;
}
