package cersei.pokemonservice.service;

import cersei.pokemonservice.config.RedisCacheConfig;
import cersei.pokemonservice.dto.PokemonSummaryDto;
import cersei.pokemonservice.exception.PokemonNotFoundException;
import cersei.pokemonservice.model.Pokemon;
import cersei.pokemonservice.repository.PokemonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PokemonCatalogService {

    private final PokemonRepository pokemonRepository;

    @Value("${pokemon.sprite-url-template:https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png}")
    private String spriteUrlTemplate;

    @Cacheable(cacheNames = RedisCacheConfig.POKEMON_CACHE, key = "#id")
    public PokemonSummaryDto getById(int id) {
        log.debug("PokemonCatalog lookup id={} (source=db)", id);
        Pokemon pokemon = pokemonRepository.findById(id).orElseThrow(() -> {
            log.warn("PokemonCatalog not found id={} (source=db)", id);
            return new PokemonNotFoundException(id);
        });
        log.debug("PokemonCatalog found id={} name={}", pokemon.getId(), pokemon.getName());
        return new PokemonSummaryDto(
                pokemon.getId(),
                pokemon.getName(),
                spriteUrlTemplate.formatted(pokemon.getId()),
                pokemon.getWeight(),
                1);
    }
}
