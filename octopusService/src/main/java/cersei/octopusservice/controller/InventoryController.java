package cersei.octopusservice.controller;

import cersei.octopusservice.dto.InventoryLineDto;
import cersei.octopusservice.service.OctopusInventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Инвентарь осьминогов по userId из JWT")
public class InventoryController {

    private final OctopusInventoryService octopusInventoryService;

    @GetMapping
    public List<InventoryLineDto> myInventory(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return octopusInventoryService.listWithDetails(userId);
    }
}
