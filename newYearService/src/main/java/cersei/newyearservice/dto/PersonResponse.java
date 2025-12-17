package cersei.newyearservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class PersonResponse {
    private Long id;
    private String name;
    private List<GiftResponse> gifts;
}

