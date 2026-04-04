package cersei.pokemonservice.controller;

import cersei.pokemonservice.dto.InventoryLineDto;
import cersei.pokemonservice.service.PokemonInventoryService;
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
@Tag(name = "Inventory", description = "Пойманные покемоны по userId из JWT")
public class InventoryController {

    private final PokemonInventoryService pokemonInventoryService;

    @GetMapping
    public List<InventoryLineDto> myInventory(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return pokemonInventoryService.listWithDetails(userId);
    }
}
