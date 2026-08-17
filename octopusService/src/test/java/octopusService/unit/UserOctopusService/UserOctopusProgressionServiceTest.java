package octopusService.unit.UserOctopusService;

import cersei.octopusservice.dto.UserOctopusAddedExpDto;
import cersei.octopusservice.dto.UserOctopusAddedStatsDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.UserOctopusProgressionService;
import cersei.octopusservice.service.useroctopus.utils.OctopusLevelCalculator;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import cersei.octopusservice.utils.StatsForUpgrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOctopusProgressionServiceTest {

    @Mock
    private UserOctopusRepository userOctopusRepository;

    @Mock
    private UserOctopusDtoAssembler assembler;

    private final OctopusLevelCalculator levelCalculator = new OctopusLevelCalculator();

    private UserOctopusProgressionService userOctopusProgressionService;

    private UUID userId;
    private UserOctopus octopus;
    private UserOctopusDto octopusDto;

    @BeforeEach
    void setUp() {
        userOctopusProgressionService = new UserOctopusProgressionService(
                userOctopusRepository,
                assembler,
                levelCalculator
        );

        userId = UUID.randomUUID();
        octopus = createOctopus(1, userId);
        octopusDto = new UserOctopusDto(
                1, 10, "Test", 1, 1, 1, null,
                0, 10, 10, 10, 10, 10, 0,
                null, null, null
        );
    }

    @Test
    void when_AddStatsToExistingOctopus_ReturnsUpgradedStat() {
        octopus.setCurrentFreeSkillPoints(2);
        octopus.setCurrentAttackStat(10);

        when(userOctopusRepository.findByIdAndUserId(1, userId))
                .thenReturn(Optional.of(octopus));

        UserOctopusAddedStatsDto result =
                userOctopusProgressionService.addStatsToOctopus(
                        userId, 1, StatsForUpgrade.ATTACK
                );

        assertEquals(StatsForUpgrade.ATTACK, result.stat());
        assertEquals(11, result.currentStat());
        assertEquals(1, result.leftFreePoints());
        assertEquals(11, octopus.getCurrentAttackStat());
        assertEquals(1, octopus.getCurrentFreeSkillPoints());
    }

    @Test
    void when_OctopusNotFound_ExceptionThrows() {
        when(userOctopusRepository.findByIdAndUserId(99, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                OctopusNotFoundException.class,
                () -> userOctopusProgressionService.addExpToOctopus(userId, 99, 10)
        );

        assertThrows(
                OctopusNotFoundException.class,
                () -> userOctopusProgressionService.addStatsToOctopus(
                        userId, 99, StatsForUpgrade.ARMOR
                )
        );
    }

    @Test
    void when_AddMoreThanZeroExpToOctopus_ExpAddedWithoutLevelUp() {
        octopus.setExp(5);

        when(userOctopusRepository.findByIdAndUserId(1, userId))
                .thenReturn(Optional.of(octopus));
        when(assembler.toDto(octopus)).thenReturn(octopusDto);

        UserOctopusAddedExpDto result =
                userOctopusProgressionService.addExpToOctopus(userId, 1, 10);

        assertEquals(1, octopus.getLevel());
        assertEquals(15, octopus.getExp());
        assertEquals(1, result.startLevel());
        assertEquals(1, result.newLevel());
        assertEquals(octopusDto, result.userOctopusDto());
        verify(assembler).toDto(octopus);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void when_AddLessOrEqualThanZeroExpToOctopus_ExceptionThrows(int exp) {
        assertThrows(
                IllegalArgumentException.class,
                () -> userOctopusProgressionService.addExpToOctopus(userId, 1, exp)
        );
    }

    @Test
    void when_AddExpCausesLevelUp_StatsAndSkillPointsIncreased() {
        octopus.setExp(15);
        octopus.setCurrentAttackStat(10);
        octopus.setCurrentMagicPowerStat(10);
        octopus.setCurrentArmorStat(10);
        octopus.setCurrentMagicResistStat(10);
        octopus.setCurrentSpeedStat(10);
        octopus.setCurrentFreeSkillPoints(0);

        when(userOctopusRepository.findByIdAndUserId(1, userId))
                .thenReturn(Optional.of(octopus));
        when(assembler.toDto(octopus)).thenReturn(octopusDto);

        UserOctopusAddedExpDto result =
                userOctopusProgressionService.addExpToOctopus(userId, 1, 10);

        assertEquals(2, octopus.getLevel());
        assertEquals(5, octopus.getExp());
        assertEquals(1, result.startLevel());
        assertEquals(2, result.newLevel());
        assertEquals(11, octopus.getCurrentAttackStat());
        assertEquals(11, octopus.getCurrentArmorStat());
        assertEquals(2, octopus.getCurrentFreeSkillPoints());
    }

    @Test
    void when_AddStatsWithNoFreePoints_ExceptionThrows() {
        octopus.setCurrentFreeSkillPoints(0);

        when(userOctopusRepository.findByIdAndUserId(1, userId))
                .thenReturn(Optional.of(octopus));

        assertThrows(
                IllegalArgumentException.class,
                () -> userOctopusProgressionService.addStatsToOctopus(
                        userId, 1, StatsForUpgrade.SPEED
                )
        );
    }

    private UserOctopus createOctopus(int id, UUID userId) {
        UserOctopus entity = new UserOctopus();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setLevel(1);
        entity.setExp(0);
        entity.setCurrentAttackStat(10);
        entity.setCurrentMagicPowerStat(10);
        entity.setCurrentArmorStat(10);
        entity.setCurrentMagicResistStat(10);
        entity.setCurrentSpeedStat(10);
        entity.setCurrentFreeSkillPoints(0);
        return entity;
    }
}