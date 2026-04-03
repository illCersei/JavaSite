package cersei.twitchservice.service;

import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.repository.ViewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerService {
    private final ViewerRepository viewerRepository;

    @Cacheable(value = "ViewersCache", key = "'fixed'")
    public List<ViewerDto> getCachedViewers() {
        log.info("Сделан запрос на вьюверов (промах кэша, загрузка из БД)");
        return loadMaxViewersByDay();
    }

    @Scheduled(fixedRate = 1000L * 60 * 60 * 3)
    @CachePut(value = "ViewersCache", key = "'fixed'")
    public List<ViewerDto> refreshViewersCache() {
        log.info("Плановое обновление кэша зрителей");
        return loadMaxViewersByDay();
    }

    private List<ViewerDto> loadMaxViewersByDay() {
        return viewerRepository.findMaxViewersGroupedByDay();
    }
}
