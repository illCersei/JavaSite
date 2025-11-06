package cersei.twitchservice.service;

import cersei.twitchservice.model.Viewer;
import cersei.twitchservice.repository.ViewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerService {
    private final ViewerRepository viewerRepository;

    public List<Viewer> findAll(){
        return viewerRepository.findAll().stream().limit(100).collect(Collectors.toList());
    }
}
