package cersei.octopusservice.service;

import cersei.octopusservice.client.WalletClient;
import cersei.octopusservice.dto.LootGrantResponse;
import cersei.octopusservice.dto.LootRollResponse;
import cersei.octopusservice.dto.WalletOperationRequest;
import cersei.octopusservice.dto.WalletOperationResponse;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.service.loot.LootCoinCalculator;
import cersei.octopusservice.service.loot.LootTierRoller;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class LootService {

    public static final String ACTION_LOOT_ROLL = "OCTOPUS_LOOT_ROLL";
    public static final String ACTION_LOOT_GRANT = "OCTOPUS_LOOT_GRANT";

    private final ItemCatalogService itemCatalogService;
    private final UserItemService userItemService;
    private final IdempotencyService idempotencyService;
    private final LootTierRoller lootTierRoller;
    private final LootCoinCalculator lootCoinCalculator;
    private final WalletClient walletClient;
    private final ItemDtoMapper itemDtoMapper;

    public LootService(
            ItemCatalogService itemCatalogService,
            UserItemService userItemService,
            IdempotencyService idempotencyService,
            LootTierRoller lootTierRoller,
            LootCoinCalculator lootCoinCalculator,
            WalletClient walletClient,
            ItemDtoMapper itemDtoMapper
    ) {
        this.itemCatalogService = itemCatalogService;
        this.userItemService = userItemService;
        this.idempotencyService = idempotencyService;
        this.lootTierRoller = lootTierRoller;
        this.lootCoinCalculator = lootCoinCalculator;
        this.walletClient = walletClient;
        this.itemDtoMapper = itemDtoMapper;
    }

    public LootRollResponse rollRandom(String accessToken, UUID userId, String idempotencyKey) {
        log.info("Игрок {} запросил ролл лута grantId={}", userId, idempotencyKey);
        return idempotencyService.run(
                userId,
                ACTION_LOOT_ROLL,
                idempotencyKey,
                LootRollResponse.class,
                () -> doRollRandom(accessToken, userId, idempotencyKey)
        );
    }

    public LootGrantResponse grantReward(
            String accessToken,
            UUID userId,
            String idempotencyKey,
            int itemId,
            int quantity,
            long coinsMinor
    ) {
        log.info(
                "Игрок {} запросил выдачу лута grantId={} itemId={} quantity={} coinsMinor={}",
                userId,
                idempotencyKey,
                itemId,
                quantity,
                coinsMinor
        );
        return idempotencyService.run(
                userId,
                ACTION_LOOT_GRANT,
                idempotencyKey,
                LootGrantResponse.class,
                () -> doGrantReward(accessToken, userId, idempotencyKey, itemId, quantity, coinsMinor)
        );
    }

    private LootRollResponse doRollRandom(String accessToken, UUID userId, String grantId) {
        int tier = lootTierRoller.rollTier();
        Item item = pickRandomItemByTier(tier);
        long coinsMinor = lootCoinCalculator.coinsForTier(tier);

        int quantityAfter = userItemService.addItems(userId, item.getId(), 1);
        WalletCreditResult walletResult = creditCoins(accessToken, grantId, coinsMinor);

        log.info(
                "Игрок {} получил ролл лута grantId={} tier={} itemId={} name={} quantityAfter={} coinsMinor={} balanceAfter={}",
                userId,
                grantId,
                tier,
                item.getId(),
                item.getName(),
                quantityAfter,
                coinsMinor,
                walletResult.balanceMinorAfter()
        );

        return new LootRollResponse(
                grantId,
                itemDtoMapper.toDto(item),
                quantityAfter,
                tier,
                coinsMinor,
                walletResult.balanceMinorAfter(),
                walletResult.idempotentReplay()
        );
    }

    private LootGrantResponse doGrantReward(
            String accessToken,
            UUID userId,
            String grantId,
            int itemId,
            int quantity,
            long coinsMinor
    ) {
        Item item = itemCatalogService.requireById(itemId);
        int quantityAfter = userItemService.addItems(userId, itemId, quantity);
        WalletCreditResult walletResult = creditCoins(accessToken, grantId, coinsMinor);

        log.info(
                "Игрок {} получил фиксированный лут grantId={} itemId={} name={} +{} -> quantityAfter={} coinsMinor={} balanceAfter={}",
                userId,
                grantId,
                itemId,
                item.getName(),
                quantity,
                quantityAfter,
                coinsMinor,
                walletResult.balanceMinorAfter()
        );

        return new LootGrantResponse(
                grantId,
                itemDtoMapper.toDto(item),
                quantityAfter,
                coinsMinor,
                walletResult.balanceMinorAfter(),
                walletResult.idempotentReplay()
        );
    }

    private WalletCreditResult creditCoins(String accessToken, String grantId, long coinsMinor) {
        if (coinsMinor <= 0) {
            return new WalletCreditResult(null, false);
        }
        WalletOperationRequest creditRequest = new WalletOperationRequest(
                coinsMinor,
                "OCTOPUS_LOOT_REWARD",
                grantId,
                "OCTOPUS_LOOT",
                null,
                grantId
        );
        WalletOperationResponse response = walletClient.credit(accessToken, creditRequest);
        return new WalletCreditResult(
                response.balanceMinorAfter(),
                response.idempotentReplay()
        );
    }

    private Item pickRandomItemByTier(int tier) {
        List<Item> pool = itemCatalogService.findByTier(tier);
        if (pool.isEmpty()) {
            log.warn("Loot pool пуст для tier={}", tier);
            throw new IllegalStateException("No items configured for tier " + tier);
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private record WalletCreditResult(Long balanceMinorAfter, boolean idempotentReplay) {
    }
}
