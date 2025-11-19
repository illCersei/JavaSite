package cersei.twitchservice.controller;

import cersei.twitchservice.dto.GamesDto;
import cersei.twitchservice.dto.PageGamesDto;
import cersei.twitchservice.service.GamesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Получение информации об играх Twitch")
public class GamesController {

    private final GamesService gamesService;

    @Operation(
            summary = "Получить список игр",
            description = "Возвращает страницу игр с поддержкой пагинации и сортировки.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Номер страницы (0-based)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Количество элементов на странице",
                            example = "20"
                    ),
                    @Parameter(
                            name = "sort",
                            description = "Параметры сортировки. Формат: field,asc|desc. Пример: gameName,asc",
                            example = "gameName,asc"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Страница игр успешно получена",
                            content = @Content(
                                    schema = @Schema(implementation = PageGamesDto.class)
                            )
                    )
            }
    )
    @GetMapping
    public Page<GamesDto> getAllGames(@PageableDefault(page = 0, size = 10000)
                                          @Parameter(hidden = true) Pageable pageable) {
        return gamesService.findAll(pageable);
    }
}
