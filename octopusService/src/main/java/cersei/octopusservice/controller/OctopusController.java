package cersei.octopusservice.controller;

import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.service.OctopusCatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@Tag(name = "Octopus", description = "Каталог осьминогов")
public class OctopusController {

    private final OctopusCatalogService octopusCatalogService;

    @GetMapping("/{id}")
    public OctopusSummaryDto byId(@PathVariable int id) {
        return octopusCatalogService.getById(id);
    }
}
