package cersei.newyearservice.dto;

import lombok.Data;

@Data
public class GiftResponse {
    private Long id;
    private String description;
    private Boolean isClaimed;
    private String claimedBy;
    private String link;
}

