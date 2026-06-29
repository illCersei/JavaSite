package cersei.octopusservice.service.loot;

import org.springframework.stereotype.Component;

@Component
public class LootCoinCalculator {

    private final LootTierWeightProperties properties;

    public LootCoinCalculator(LootTierWeightProperties properties) {
        this.properties = properties;
    }

    public long coinsForTier(int tier) {
        return properties.coinMinorByTier().getOrDefault(tier, 0L);
    }
}
