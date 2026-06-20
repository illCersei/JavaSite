package octopusService.unit.UserOctopusService;

import cersei.octopusservice.service.useroctopus.utils.LevelProgress;
import cersei.octopusservice.service.useroctopus.utils.OctopusLevelCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OctopusLevelCalculatorTest {

    private OctopusLevelCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OctopusLevelCalculator();
    }

    @Test
    void when_AddExpWithoutLevelUp_KeepsSameLevel() {
        LevelProgress progress = calculator.calculate(1, 5, 10);

        assertEquals(1, progress.level());
        assertEquals(15, progress.exp());
        assertEquals(0, progress.gainedLevels());
    }

    @Test
    void when_AddExpExactlyToNextLevel_LevelsUpOnce() {
        LevelProgress progress = calculator.calculate(1, 0, 20);

        assertEquals(2, progress.level());
        assertEquals(0, progress.exp());
        assertEquals(1, progress.gainedLevels());
    }

    @Test
    void when_AddExpWithRemainder_LevelsUpAndKeepsRemainder() {
        LevelProgress progress = calculator.calculate(1, 15, 10);

        assertEquals(2, progress.level());
        assertEquals(5, progress.exp());
        assertEquals(1, progress.gainedLevels());
    }

    @Test
    void when_AddExpForMultipleLevels_LevelsUpMultipleTimes() {
        LevelProgress progress = calculator.calculate(1, 0, 60);

        assertEquals(3, progress.level());
        assertEquals(0, progress.exp());
        assertEquals(2, progress.gainedLevels());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 20",
            "2, 40",
            "3, 80",
            "4, 160"
    })
    void expToNextLevel_ReturnsExpectedValue(int level, long expectedExp) {
        assertEquals(expectedExp, calculator.expToNextLevel(level));
    }

    @Test
    void when_LevelIsZero_ExceptionThrows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.expToNextLevel(0)
        );
    }
}