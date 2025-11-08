package cersei.twitchservice.service;

import cersei.twitchservice.dto.ViewerDto;
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

    public List<ViewerDto> findAll(){
        return viewerRepository.findAll().stream()
                .map(this::toDto)
                .limit(100)
                .collect(Collectors.toList());
    }

    private ViewerDto toDto(Viewer viewer){
        ViewerDto dto = new ViewerDto();
        dto.setId(viewer.getId());
        dto.setViewers(viewer.getViewers());
        dto.setDateTime(viewer.getDateTime());
        dto.setId(viewer.getId());

        return dto;
    }
}
