package cersei.pokemonservice.service;

import cersei.pokemonservice.client.PokeApiClient;
import cersei.pokemonservice.config.RedisCacheConfig;
import cersei.pokemonservice.dto.PokemonSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PokemonCatalogService {

    private final PokeApiClient pokeApiClient;

    @Cacheable(cacheNames = RedisCacheConfig.POKE_API_POKEMON_CACHE, key = "#id")
    public PokemonSummaryDto getById(int id) {
        return pokeApiClient.fetchSummary(id);
    }
}
