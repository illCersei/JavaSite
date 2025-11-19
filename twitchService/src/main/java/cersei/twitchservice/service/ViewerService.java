package cersei.twitchservice.service;

import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.model.Viewer;
import cersei.twitchservice.repository.ViewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerService {
    private final ViewerRepository viewerRepository;

    public Page<ViewerDto> findAll(Pageable pageable) {
        return viewerRepository.findAll(pageable)
                .map(this::toDto);
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
