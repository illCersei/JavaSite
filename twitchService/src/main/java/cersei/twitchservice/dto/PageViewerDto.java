package cersei.twitchservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class PageViewerDto {

    @Schema(description = "Список элементов")
    private List<ViewerDto> content;

    @Schema(description = "Номер страницы")
    private int number;

    @Schema(description = "Размер страницы")
    private int size;

    @Schema(description = "Количество элементов на текущей странице")
    private int numberOfElements;

    @Schema(description = "Общее количество элементов")
    private long totalElements;

    @Schema(description = "Общее количество страниц")
    private int totalPages;

    @Schema(description = "Это последняя страница")
    private boolean last;

    @Schema(description = "Это первая страница")
    private boolean first;
}
