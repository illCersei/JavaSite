package cersei.pokemonservice.dto;

/**
 * @param pokemon карточка из кэша PokeAPI; поле {@code quantity} — сколько штук у пользователя в инвентаре.
 */
public record InventoryLineDto(PokemonSummaryDto pokemon) {}
