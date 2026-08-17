package octopusService.unit.UserOctopusService;

import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.utils.StatsForUpgrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserOctopusTest {

    private UserOctopus octopus;

    @BeforeEach
    void setUp() {
        octopus = new UserOctopus();
        octopus.setCurrentAttackStat(10);
        octopus.setCurrentMagicPowerStat(10);
        octopus.setCurrentArmorStat(10);
        octopus.setCurrentMagicResistStat(10);
        octopus.setCurrentSpeedStat(10);
        octopus.setCurrentFreeSkillPoints(1);
    }

    @ParameterizedTest
    @EnumSource(StatsForUpgrade.class)
    void when_UpgradeStat_IncrementsSelectedStatAndSpendsPoint(StatsForUpgrade stat) {
        int newValue = octopus.upgradeStat(stat);

        assertEquals(11, newValue);
        assertEquals(0, octopus.getCurrentFreeSkillPoints());
        assertEquals(
                stat == StatsForUpgrade.ATTACK ? 11 : 10,
                octopus.getCurrentAttackStat()
        );
        assertEquals(
                stat == StatsForUpgrade.MAGIC_POWER ? 11 : 10,
                octopus.getCurrentMagicPowerStat()
        );
        assertEquals(
                stat == StatsForUpgrade.ARMOR ? 11 : 10,
                octopus.getCurrentArmorStat()
        );
        assertEquals(
                stat == StatsForUpgrade.MAGIC_RESIST ? 11 : 10,
                octopus.getCurrentMagicResistStat()
        );
        assertEquals(
                stat == StatsForUpgrade.SPEED ? 11 : 10,
                octopus.getCurrentSpeedStat()
        );
    }

    @Test
    void when_NoFreeSkillPoints_ExceptionThrows() {
        octopus.setCurrentFreeSkillPoints(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> octopus.upgradeStat(StatsForUpgrade.ATTACK)
        );
    }
}