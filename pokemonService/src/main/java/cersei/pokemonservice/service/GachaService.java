package cersei.pokemonservice.service;

import cersei.pokemonservice.dto.GachaSpinResponse;
import cersei.pokemonservice.dto.PokemonSummaryDto;
import cersei.pokemonservice.exception.WalletCommandRejectedException;
import cersei.pokemonservice.kafka.PokemonGachaEvent;
import cersei.pokemonservice.kafka.PokemonGachaEventPublisher;
import cersei.pokemonservice.kafka.wallet.WalletKafkaCommandResultDto;
import cersei.pokemonservice.kafka.wallet.WalletKafkaRpcGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class GachaService {

    private final WalletKafkaRpcGateway walletKafkaRpcGateway;
    private final PokemonCatalogService pokemonCatalogService;
    private final PokemonInventoryService pokemonInventoryService;
    private final PokemonGachaEventPublisher pokemonGachaEventPublisher;

    @Value("${pokemon.gacha.cost-minor}")
    private long costMinor;

    @Value("${pokemon.gacha.max-pokemon-id}")
    private int maxPokemonId;

    public GachaSpinResponse spin(String accessToken, UUID userId) {
        String spinId = UUID.randomUUID().toString();
        WalletKafkaCommandResultDto walletResult = walletKafkaRpcGateway.debitAndAwait(
                userId,
                accessToken,
                costMinor,
                "GACHA_SPIN",
                spinId,
                "GACHA",
                null,
                spinId);

        if (!walletResult.success()) {
            String err = walletResult.errorCode() != null ? walletResult.errorCode() : "WALLET_ERROR";
            throw new WalletCommandRejectedException(err, "Wallet command failed: " + err);
        }

        long balanceAfter =
                walletResult.balanceMinorAfter() != null ? walletResult.balanceMinorAfter() : 0L;

        int pokemonId = ThreadLocalRandom.current().nextInt(1, maxPokemonId + 1);
        try {
            PokemonSummaryDto base = pokemonCatalogService.getById(pokemonId);
            int ownedQty = pokemonInventoryService.addOne(userId, pokemonId);
            PokemonSummaryDto pokemon = new PokemonSummaryDto(
                    base.id(), base.name(), base.imageUrl(), base.weight(), ownedQty);
            pokemonGachaEventPublisher.publish(new PokemonGachaEvent(
                    userId.toString(),
                    spinId,
                    pokemon.id(),
                    pokemon.name(),
                    costMinor,
                    Instant.now().toString()));
            return new GachaSpinResponse(
                    spinId, pokemon, balanceAfter, walletResult.idempotentReplay());
        } catch (RuntimeException e) {
            refundQuietly(accessToken, userId, spinId);
            throw e;
        }
    }

    private void refundQuietly(String accessToken, UUID userId, String spinId) {
        try {
            walletKafkaRpcGateway.creditAndAwait(
                    userId,
                    accessToken,
                    costMinor,
                    "GACHA_REFUND",
                    spinId,
                    "GACHA_REFUND",
                    "{\"reason\":\"POKEAPI_OR_CACHE_FAILURE\"}",
                    spinId);
        } catch (Exception ex) {
            log.error("Gacha refund failed, manual reconciliation may be needed spinId={}", spinId, ex);
        }
    }
}
