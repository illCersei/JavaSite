package cersei.twitchservice.controller;

import cersei.twitchservice.dto.GamesDto;
import cersei.twitchservice.service.GamesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GamesController {
    private final GamesService gamesService;

    @GetMapping
    public List<GamesDto> getAllGames() {
        return gamesService.findAll();
    }
}
