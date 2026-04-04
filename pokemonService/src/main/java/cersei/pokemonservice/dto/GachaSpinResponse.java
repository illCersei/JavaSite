package cersei.pokemonservice.dto;

public record GachaSpinResponse(
        String spinId,
        PokemonSummaryDto pokemon,
        long balanceMinorAfter,
        boolean walletIdempotentReplay) {}
