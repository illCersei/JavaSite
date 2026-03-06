package cersei.common.error.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private List<ApiError> errors;
    private Instant timestamp;

    public ApiErrorResponse(List<ApiError> errors) {
        this.errors = errors;
        this.timestamp = Instant.now();
    }
}
