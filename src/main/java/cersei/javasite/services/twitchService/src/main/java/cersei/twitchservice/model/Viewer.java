package cersei.twitchservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class Viewer {
    long id;
    long gameId;
    LocalDateTime dateTime;
    long viewers;
}
