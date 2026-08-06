package octopusService.unit.LootService;

import cersei.octopusservice.service.loot.LootTierWeightProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LootTierWeightPropertiesTest {

    @Test
    void when_FailedRunRetentionBpsNotConfigured_DefaultsToFiftyPercent() {
        LootTierWeightProperties properties = new LootTierWeightProperties(
                Map.of(1, 70, 2, 30),
                Map.of(1, 50L, 2, 150L),
                null
        );

        assertEquals(5000, properties.resolvedFailedRunRetentionBps());
        assertEquals(75L, properties.applyFailedRunRetention(150L));
        assertEquals(2, properties.applyFailedRunRetention(5));
    }

    @Test
    void when_FailedRunRetentionBpsConfigured_UsesConfiguredValue() {
        LootTierWeightProperties properties = new LootTierWeightProperties(
                Map.of(1, 70, 2, 30),
                Map.of(1, 50L, 2, 150L),
                2500
        );

        assertEquals(2500, properties.resolvedFailedRunRetentionBps());
        assertEquals(25L, properties.applyFailedRunRetention(100L));
    }
}
