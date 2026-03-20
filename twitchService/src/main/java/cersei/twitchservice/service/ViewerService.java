package cersei.twitchservice.service;

import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.model.Viewer;
import cersei.twitchservice.repository.ViewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerService {
    private final ViewerRepository viewerRepository;

    public List<ViewerDto> findMaxViewersByDay() {
        return viewerRepository.findMaxViewersGroupedByDay();
    }
}
