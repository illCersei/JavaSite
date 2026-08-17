package octopusService.unit.LootService;

import cersei.octopusservice.service.loot.LootTierRoller;
import cersei.octopusservice.service.loot.LootTierWeightProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootTierRollerTest {

    @Test
    void when_RollBoostedTier_CapsAtHighestConfiguredTier() {
        LootTierWeightProperties properties = new LootTierWeightProperties(
                Map.of(1, 100),
                Map.of(1, 50L),
                null
        );
        LootTierRoller roller = new LootTierRoller(properties);

        for (int i = 0; i < 50; i++) {
            assertEquals(1, roller.rollBoostedTier(1));
            assertEquals(1, roller.rollBoostedTier(5));
        }
    }

    @Test
    void when_RollBoostedTier_BumpsWithinConfiguredRange() {
        LootTierWeightProperties properties = new LootTierWeightProperties(
                Map.of(1, 100, 2, 100, 3, 100),
                Map.of(1, 50L, 2, 100L, 3, 150L),
                null
        );
        LootTierRoller roller = new LootTierRoller(properties);

        for (int i = 0; i < 50; i++) {
            int rolled = roller.rollBoostedTier(1);
            assertTrue(rolled >= 2 && rolled <= 3, "boosted roll should never fall below the lowest possible bump");
        }
    }
}
