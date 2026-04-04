package cersei.pokemonservice.controller;

import cersei.pokemonservice.dto.PokemonSummaryDto;
import cersei.pokemonservice.service.PokemonCatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@Tag(name = "Pokemon", description = "Каталог покемонов (PokeAPI + Redis)")
public class PokemonController {

    private final PokemonCatalogService pokemonCatalogService;

    @GetMapping("/{id}")
    public PokemonSummaryDto byId(@PathVariable int id) {
        return pokemonCatalogService.getById(id);
    }
}
