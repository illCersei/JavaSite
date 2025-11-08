package cersei.twitchservice.controller;

import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.model.Viewer;
import cersei.twitchservice.service.ViewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/viewers")
@RequiredArgsConstructor
public class ViewerController {
    private final ViewerService viewerService;

    @GetMapping
    public List<ViewerDto> getAllViewers() {
        return viewerService.findAll();
    }
}
