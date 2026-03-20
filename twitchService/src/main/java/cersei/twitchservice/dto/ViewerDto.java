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
    private Long maxViewers;

    public ViewerDto(LocalDate dateTime, Long maxViewers) {
        this.dateTime = dateTime.atStartOfDay();
        this.maxViewers = maxViewers;
    }
}