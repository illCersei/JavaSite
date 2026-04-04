package cersei.pokemonservice.client;

import cersei.pokemonservice.dto.PokemonSummaryDto;
import cersei.pokemonservice.exception.PokemonNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokeApiClient {

    private final RestClient pokeApiRestClient;
    private final ObjectMapper objectMapper;

    @Value("${pokeapi.base-url}")
    private String pokeApiBaseUrl;

    @Value("${pokeapi.sprite-url-template}")
    private String spriteUrlTemplate;

    public PokemonSummaryDto fetchSummary(int id) {
        String body = pokeApiRestClient
                .get()
                .uri("/api/v2/pokemon/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new PokemonNotFoundException(id);
                })
                .body(String.class);
        if (body == null || body.isBlank()) {
            log.warn(
                    "PokeAPI empty body for id={} (baseUrl={}). В Docker POKEAPI_BASE_URL=http://pokeapi, с хоста — http://localhost:8088",
                    id,
                    pokeApiBaseUrl);
            throw new PokemonNotFoundException(id);
        }
        try {
            return mapToSummary(objectMapper.readTree(body), id);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid PokeAPI response for id=" + id, e);
        }
    }

    private PokemonSummaryDto mapToSummary(JsonNode root, int id) {
        String name = root.path("name").asText("");
        int weight = root.path("weight").asInt(0);
        String imageUrl = spriteUrlTemplate.formatted(id);
        return new PokemonSummaryDto(id, name, imageUrl, weight, 1);
    }
}
