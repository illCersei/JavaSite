package cersei.twitchservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageGamesDto {
    private List<GamesDto> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
