package cersei.octopusservice.service.useroctopus.utils;

import org.springframework.stereotype.Component;

@Component
public class OctopusLevelCalculator {

    public LevelProgress calculate(
            int currentLevel,
            int currentExp,
            int addedExp
    ) {
        int level = currentLevel;
        long exp = (long) currentExp + addedExp;
        int gainedLevels = 0;

        while (exp >= expToNextLevel(level)) {
            exp -= expToNextLevel(level);
            level++;
            gainedLevels++;
        }

        return new LevelProgress(
                level,
                Math.toIntExact(exp),
                gainedLevels
        );
    }

    public long expToNextLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException(
                    "Уровень должен быть больше 0"
            );
        }

        return 20L * (1L << (level - 1));
    }
}