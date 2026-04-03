package cersei.twitchservice.controller;

import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.service.ViewerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<ViewerDto> getAllViewers() {
        return viewerService.getCachedViewers();
    }
}
