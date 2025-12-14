package cersei.twitchservice.controller;

import cersei.twitchservice.dto.PageViewerDto;
import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.service.ViewerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/viewers")
@RequiredArgsConstructor
@Tag(name = "Viewers", description = "Работа со зрителями Twitch")
public class ViewerController {

    private final ViewerService viewerService;

    @Operation(
            summary = "Получить список зрителей",
            description = "Возвращает страницу зрителей.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Номер страницы (нумерация с 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Количество элементов на странице",
                            example = "20"
                    ),
                    @Parameter(
                            name = "sort",
                            description = "Параметры сортировки. Формат: field,asc|desc. Пример: dateTime,desc",
                            example = "dateTime,desc"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Страница зрителей успешно получена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageViewerDto.class)
                            )
                    )
            }
    )
    @GetMapping("/paginated")
    @Cacheable(value = "ViewersPaginated", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public List<ViewerDto> getPaginatedViewers(
        @PageableDefault(page = 0, size = 10000)
        @Parameter(hidden = true) Pageable pageable) {

        return viewerService.findAllPageable(pageable).getContent();
    }


    @Operation(
            summary = "Получить список всех зрителей",
            description = "Возвращает список зрителей.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Номер страницы (нумерация с 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Количество элементов на странице",
                            example = "20"
                    ),
                    @Parameter(
                            name = "sort",
                            description = "Параметры сортировки. Формат: field,asc|desc. Пример: dateTime,desc",
                            example = "dateTime,desc"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Страница зрителей успешно получена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageViewerDto.class)
                            )
                    )
            }
    )
    @Cacheable(value = "ViewersCache", key = "'fixed'")
    @GetMapping("/all")
    public List<ViewerDto> getAllViewers() {
        return viewerService.findAll();
    }
}
