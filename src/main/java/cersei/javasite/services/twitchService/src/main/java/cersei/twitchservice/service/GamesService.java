package cersei.twitchservice.service;

import cersei.twitchservice.dto.GamesDto;
import cersei.twitchservice.model.Games;
import cersei.twitchservice.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamesService {
    private final GamesRepository gamesRepository;

    public List<GamesDto> findAll(){
        return gamesRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private GamesDto toDto(Games games){
        GamesDto dto = new GamesDto();
        dto.setId(games.getGameId());
        dto.setName(games.getGameName());

        return dto;
    }
}
