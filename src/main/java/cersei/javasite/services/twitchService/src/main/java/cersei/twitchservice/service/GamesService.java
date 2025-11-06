package cersei.twitchservice.service;

import cersei.twitchservice.model.Games;
import cersei.twitchservice.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamesService {
    private final GamesRepository gamesRepository;

    public List<Games> findAll(){
        return gamesRepository.findAll();
    }
}
