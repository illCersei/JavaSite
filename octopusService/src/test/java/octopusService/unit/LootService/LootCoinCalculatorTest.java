package octopusService.unit.LootService;

import cersei.octopusservice.service.loot.LootCoinCalculator;
import cersei.octopusservice.service.loot.LootTierWeightProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LootCoinCalculatorTest {

    @Test
    void when_CoinsForTier_ReturnsConfiguredAmount() {
        LootTierWeightProperties properties = new LootTierWeightProperties(
                Map.of(1, 70, 2, 30),
                Map.of(1, 50L, 2, 150L),
                null
        );
        LootCoinCalculator calculator = new LootCoinCalculator(properties);

        assertEquals(50L, calculator.coinsForTier(1));
        assertEquals(150L, calculator.coinsForTier(2));
        assertEquals(0L, calculator.coinsForTier(99));
    }
}
