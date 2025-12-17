package cersei.newyearservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClaimResponse {
    private boolean success;
    private String message;
    private String claimedBy;
}
