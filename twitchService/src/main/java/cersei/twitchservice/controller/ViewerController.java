package cersei.twitchservice.controller;

import cersei.twitchservice.dto.PageViewerDto;
import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.service.ViewerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/viewers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Viewers", description = "Работа со зрителями Twitch")
public class ViewerController {

    private final ViewerService viewerService;

    @GetMapping("/all")
    @Cacheable(value = "ViewersCache", key = "'fixed'")
    @Scheduled(fixedRate = 1000 * 60 * 60 * 3) // 3 h
    public List<ViewerDto> getAllViewers() {
        log.info("Сделан запрос на вьюверов");
        return viewerService.findMaxViewersByDay();
    }
}
