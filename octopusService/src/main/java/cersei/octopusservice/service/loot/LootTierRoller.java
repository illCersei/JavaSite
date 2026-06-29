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

    public LootTierRoller(LootTierWeightProperties properties) {
        NavigableMap<Integer, Integer> map = new TreeMap<>();
        int cumulative = 0;
        for (Map.Entry<Integer, Integer> entry : properties.weights().entrySet()) {
            int weight = entry.getValue();
            if (weight <= 0) {
                continue;
            }
            cumulative += weight;
            map.put(cumulative, entry.getKey());
        }
        if (map.isEmpty()) {
            throw new IllegalStateException("octopus.loot.tier-weights must define at least one positive weight");
        }
        this.tierByRoll = map;
        this.totalWeight = cumulative;
    }

    public int rollTier() {
        int roll = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);
        return tierByRoll.ceilingEntry(roll).getValue();
    }
}
