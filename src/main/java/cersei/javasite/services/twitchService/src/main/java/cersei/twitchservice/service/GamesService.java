package cersei.twitchservice.service;

import cersei.twitchservice.model.Games;
import cersei.twitchservice.repository.GamesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GamesService {
    private final GamesRepository gamesRepository;

    public List<Games> findAll(){
        return gamesRepository.findAll();
    }
}
