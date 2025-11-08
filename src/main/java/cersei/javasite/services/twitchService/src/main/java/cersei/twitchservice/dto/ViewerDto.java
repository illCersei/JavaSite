package cersei.twitchservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViewerDto {
    private Long id;
    private Long gameId;
    private LocalDateTime dateTime;
    private Long viewers;
}
