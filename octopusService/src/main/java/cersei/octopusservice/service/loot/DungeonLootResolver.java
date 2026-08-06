package cersei.octopusservice.service.loot;

import cersei.octopusservice.model.DungeonRunRoom;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Rolls a room's loot lazily, at the moment it's actually resolved (room cleared, fight won),
 * instead of at map-generation time - so a room's reward is never visible before the player
 * has earned it. {@link cersei.octopusservice.service.dungeon.DungeonMapGenerator} only decides
 * room type + enemy; this decides what that room pays out, once.
 */
@Component
@RequiredArgsConstructor
public class DungeonLootResolver {

    private final LootTierRoller lootTierRoller;
    private final LootCoinCalculator lootCoinCalculator;
    private final ItemRepository itemRepository;

    public void rollForRoom(DungeonRunRoom room, int dungeonTier) {
        if (alreadyRolled(room)) {
            return;
        }
        room.setLootCoinsMinor(lootCoinCalculator.coinsForTier(dungeonTier));
        rollItemLoot(room, 0);
    }

    // Elite fights roll their item drop one tier better than usual, on top of the normal
    // dungeon-tier coin reward.
    public void rollForEliteRoom(DungeonRunRoom room, int dungeonTier) {
        if (alreadyRolled(room)) {
            return;
        }
        room.setLootCoinsMinor(lootCoinCalculator.coinsForTier(dungeonTier));
        rollItemLoot(room, 1);
    }

    // Instant, no-fight rooms (EVENT) - coins only, a fraction of a normal room's reward.
    public void rollCoinsOnly(DungeonRunRoom room, int dungeonTier, double fractionOfNormal) {
        if (alreadyRolled(room)) {
            return;
        }
        long full = lootCoinCalculator.coinsForTier(dungeonTier);
        room.setLootCoinsMinor(Math.round(full * fractionOfNormal));
    }

    private boolean alreadyRolled(DungeonRunRoom room) {
        return room.getLootItem() != null || room.getLootCoinsMinor() > 0;
    }

    private void rollItemLoot(DungeonRunRoom room, int bonusTiers) {
        int rolledTier = lootTierRoller.rollBoostedTier(bonusTiers);
        List<Item> pool = itemRepository.findByTierOrderByIdAsc(rolledTier);
        if (pool.isEmpty()) {
            return;
        }
        Random random = new Random(room.getDungeonRun().getRngSeed() + room.getLayerIndex());
        Item item = pool.get(random.nextInt(pool.size()));
        room.setLootItem(item);
        room.setLootQuantity(1);
    }
}
