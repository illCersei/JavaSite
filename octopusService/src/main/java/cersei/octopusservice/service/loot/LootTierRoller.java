package cersei.octopusservice.service.loot;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LootTierRoller {

    private final NavigableMap<Integer, Integer> tierByRoll;
    private final int totalWeight;
    private final int maxTier;

    public LootTierRoller(LootTierWeightProperties properties) {
        NavigableMap<Integer, Integer> map = new TreeMap<>();
        int cumulative = 0;
        int highestTier = Integer.MIN_VALUE;
        for (Map.Entry<Integer, Integer> entry : properties.weights().entrySet()) {
            int weight = entry.getValue();
            if (weight <= 0) {
                continue;
            }
            cumulative += weight;
            map.put(cumulative, entry.getKey());
            highestTier = Math.max(highestTier, entry.getKey());
        }
        if (map.isEmpty()) {
            throw new IllegalStateException("octopus.loot.tier-weights must define at least one positive weight");
        }
        this.tierByRoll = map;
        this.totalWeight = cumulative;
        this.maxTier = highestTier;
    }

    public int rollTier() {
        int roll = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);
        return tierByRoll.ceilingEntry(roll).getValue();
    }

    // Rolls normally, then bumps the result up by `bonusTiers` (capped at the highest
    // configured tier) - used to give elite-room loot better odds without a separate table.
    public int rollBoostedTier(int bonusTiers) {
        return Math.min(rollTier() + bonusTiers, maxTier);
    }
}
