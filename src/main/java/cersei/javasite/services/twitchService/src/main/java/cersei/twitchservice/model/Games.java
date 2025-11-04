package cersei.twitchservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Games {
    long gameId;
    String gameName;
}
