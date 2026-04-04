package cersei.pokemonservice.controller;

import cersei.pokemonservice.dto.GachaSpinResponse;
import cersei.pokemonservice.service.GachaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/gacha")
@RequiredArgsConstructor
@Tag(name = "Gacha", description = "Гача за монеты кошелька")
public class GachaController {

    private final GachaService gachaService;

    @PostMapping("/spin")
    public GachaSpinResponse spin(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return gachaService.spin(jwt.getTokenValue(), userId);
    }
}
