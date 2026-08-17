package octopusService.unit.LootService;

import cersei.octopusservice.model.DungeonRun;
import cersei.octopusservice.model.DungeonRunRoom;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.repository.ItemRepository;
import cersei.octopusservice.service.loot.DungeonLootResolver;
import cersei.octopusservice.service.loot.LootCoinCalculator;
import cersei.octopusservice.service.loot.LootTierRoller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DungeonLootResolverTest {

    @Mock
    private LootTierRoller lootTierRoller;

    @Mock
    private LootCoinCalculator lootCoinCalculator;

    @Mock
    private ItemRepository itemRepository;

    private DungeonLootResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DungeonLootResolver(lootTierRoller, lootCoinCalculator, itemRepository);
    }

    @Test
    void when_RollForRoom_SetsCoinsAndItem() {
        DungeonRunRoom room = newRoom();
        Item item = new Item();
        item.setId(7);

        when(lootCoinCalculator.coinsForTier(1)).thenReturn(50L);
        when(lootTierRoller.rollBoostedTier(0)).thenReturn(1);
        when(itemRepository.findByTierOrderByIdAsc(1)).thenReturn(List.of(item));

        resolver.rollForRoom(room, 1);

        assertEquals(50L, room.getLootCoinsMinor());
        assertNotNull(room.getLootItem());
        assertEquals(1, room.getLootQuantity());
    }

    @Test
    void when_RollForRoom_CalledTwice_OnlyRollsOnce() {
        DungeonRunRoom room = newRoom();
        when(lootCoinCalculator.coinsForTier(anyInt())).thenReturn(50L);
        when(lootTierRoller.rollBoostedTier(0)).thenReturn(1);
        when(itemRepository.findByTierOrderByIdAsc(1)).thenReturn(List.of());

        resolver.rollForRoom(room, 1);
        resolver.rollForRoom(room, 1);

        verify(lootCoinCalculator, times(1)).coinsForTier(anyInt());
    }

    @Test
    void when_RollForEliteRoom_BoostsItemTierByOne() {
        DungeonRunRoom room = newRoom();
        when(lootCoinCalculator.coinsForTier(1)).thenReturn(50L);
        when(lootTierRoller.rollBoostedTier(1)).thenReturn(2);
        when(itemRepository.findByTierOrderByIdAsc(2)).thenReturn(List.of());

        resolver.rollForEliteRoom(room, 1);

        verify(lootTierRoller).rollBoostedTier(1);
        verify(lootTierRoller, never()).rollBoostedTier(0);
    }

    @Test
    void when_RollCoinsOnly_AppliesFractionAndSkipsItemRoll() {
        DungeonRunRoom room = newRoom();
        when(lootCoinCalculator.coinsForTier(1)).thenReturn(100L);

        resolver.rollCoinsOnly(room, 1, 0.5);

        assertEquals(50L, room.getLootCoinsMinor());
        verify(itemRepository, never()).findByTierOrderByIdAsc(anyInt());
    }

    private DungeonRunRoom newRoom() {
        DungeonRun run = new DungeonRun();
        run.setId(UUID.randomUUID());
        run.setRngSeed(42L);

        DungeonRunRoom room = new DungeonRunRoom();
        room.setDungeonRun(run);
        room.setLayerIndex(0);
        room.setSlotIndex(0);
        return room;
    }
}
