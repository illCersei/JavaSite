package cersei.twitchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ViewerDto {
    private LocalDateTime dateTime;
    private Integer maxViewers;

    public ViewerDto(LocalDate dateTime, Integer maxViewers) {
        this.dateTime = dateTime.atStartOfDay();
        this.maxViewers = maxViewers;
    }
}