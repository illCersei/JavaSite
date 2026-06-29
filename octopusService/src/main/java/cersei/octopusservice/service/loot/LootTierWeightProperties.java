package cersei.octopusservice.service.loot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "octopus.loot")
public record LootTierWeightProperties(
        Map<Integer, Integer> tierWeights,
        Map<Integer, Long> coinMinorByTier
) {
    public Map<Integer, Integer> weights() {
        return tierWeights != null ? tierWeights : Map.of();
    }

    public Map<Integer, Long> coinMinorByTier() {
        return coinMinorByTier != null ? coinMinorByTier : Map.of();
    }
}
