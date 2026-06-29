package octopusService.unit.LootService;

import cersei.octopusservice.client.WalletClient;
import cersei.octopusservice.dto.LootRollResponse;
import cersei.octopusservice.dto.WalletOperationRequest;
import cersei.octopusservice.dto.WalletOperationResponse;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.utils.ItemSlot;
import cersei.octopusservice.service.IdempotencyService;
import cersei.octopusservice.service.ItemCatalogService;
import cersei.octopusservice.service.LootService;
import cersei.octopusservice.service.UserItemService;
import cersei.octopusservice.service.loot.LootCoinCalculator;
import cersei.octopusservice.service.loot.LootTierRoller;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LootServiceTest {

    @Mock
    private ItemCatalogService itemCatalogService;

    @Mock
    private UserItemService userItemService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private LootTierRoller lootTierRoller;

    @Mock
    private LootCoinCalculator lootCoinCalculator;

    @Mock
    private WalletClient walletClient;

    private LootService lootService;

    private final UUID userId = UUID.randomUUID();
    private final String token = "Bearer token";

    @BeforeEach
    void setUp() {
        lootService = new LootService(
                itemCatalogService,
                userItemService,
                idempotencyService,
                lootTierRoller,
                lootCoinCalculator,
                walletClient,
                new ItemDtoMapper()
        );
    }

    @Test
    void when_RollRandom_GrantsItemAndCoins() {
        String grantId = "loot-1";
        Item item = createItem(1, "Coral Spear", 1);

        when(idempotencyService.run(
                eq(userId),
                eq(LootService.ACTION_LOOT_ROLL),
                eq(grantId),
                eq(LootRollResponse.class),
                any()
        )).thenAnswer(invocation -> {
            Supplier<LootRollResponse> supplier = invocation.getArgument(4);
            return supplier.get();
        });

        when(lootTierRoller.rollTier()).thenReturn(1);
        when(lootCoinCalculator.coinsForTier(1)).thenReturn(50L);
        when(itemCatalogService.findByTier(1)).thenReturn(List.of(item));
        when(userItemService.addItems(userId, 1, 1)).thenReturn(5);
        when(walletClient.credit(eq(token), any(WalletOperationRequest.class)))
                .thenReturn(new WalletOperationResponse(UUID.randomUUID(), 150L, false));

        LootRollResponse response = lootService.rollRandom(token, userId, grantId);

        assertEquals(grantId, response.grantId());
        assertEquals(1, response.item().id());
        assertEquals(5, response.quantityAfter());
        assertEquals(1, response.rolledTier());
        assertEquals(50L, response.coinsMinor());
        assertEquals(150L, response.balanceMinorAfter());
        assertFalse(response.walletIdempotentReplay());

        verify(walletClient).credit(eq(token), any(WalletOperationRequest.class));
    }

    private Item createItem(int id, String name, int tier) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setSlot(ItemSlot.WEAPON);
        item.setTier(tier);
        item.setAttackStat(1);
        item.setMagicPowerStat(0);
        item.setArmorStat(0);
        item.setMagicResistStat(0);
        item.setSpeedStat(0);
        return item;
    }
}
