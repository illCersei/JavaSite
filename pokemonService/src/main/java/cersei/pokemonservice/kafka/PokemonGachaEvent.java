package cersei.pokemonservice.kafka;

public record PokemonGachaEvent(
        String userId,
        String spinId,
        int pokemonId,
        String pokemonName,
        long costMinor,
        String occurredAt) {}
