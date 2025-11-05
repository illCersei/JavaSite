package cersei.twitchservice.service;

import cersei.twitchservice.model.Viewer;
import cersei.twitchservice.repository.ViewerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewerService {
    private final ViewerRepository viewerRepository;

    public List<Viewer> findAll(){
        return viewerRepository.findAll().stream().limit(100).collect(Collectors.toList());
    }
}
