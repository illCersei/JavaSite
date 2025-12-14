package cersei.twitchservice.service;

import cersei.twitchservice.dto.GamesDto;
import cersei.twitchservice.model.Games;
import cersei.twitchservice.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamesService {
    private final GamesRepository gamesRepository;

    public Page<GamesDto> findAllPaginated(Pageable pageable) {
        return gamesRepository.findAll(pageable)
                .map(this::toDto);
    }

    public List<GamesDto> findAll() {
        return gamesRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private GamesDto toDto(Games games){
        GamesDto dto = new GamesDto();
        dto.setGameId(games.getGameId());
        dto.setGameName(games.getGameName());

        return dto;
    }
}
