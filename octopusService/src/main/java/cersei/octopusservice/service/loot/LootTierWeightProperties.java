package cersei.octopusservice.service.loot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "octopus.loot")
public record LootTierWeightProperties(
        Map<Integer, Integer> tierWeights,
        Map<Integer, Long> coinMinorByTier,
        Integer failedRunRetentionBps
) {
    private static final int DEFAULT_FAILED_RUN_RETENTION_BPS = 5000;

    public Map<Integer, Integer> weights() {
        return tierWeights != null ? tierWeights : Map.of();
    }

    public Map<Integer, Long> coinMinorByTier() {
        return coinMinorByTier != null ? coinMinorByTier : Map.of();
    }

    // Fraction (basis points, 10000 = 100%) of pending loot paid out when a run ends FAILED
    // instead of being voluntarily extracted.
    public int resolvedFailedRunRetentionBps() {
        return failedRunRetentionBps != null ? failedRunRetentionBps : DEFAULT_FAILED_RUN_RETENTION_BPS;
    }

    public int applyFailedRunRetention(int amount) {
        return (int) applyFailedRunRetention((long) amount);
    }

    public long applyFailedRunRetention(long amount) {
        return amount * resolvedFailedRunRetentionBps() / 10_000;
    }
}
