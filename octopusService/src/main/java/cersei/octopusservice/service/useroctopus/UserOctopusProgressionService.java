package cersei.octopusservice.service.useroctopus;

import cersei.octopusservice.dto.UserOctopusAddedExpDto;
import cersei.octopusservice.dto.UserOctopusAddedStatsDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.utils.LevelProgress;
import cersei.octopusservice.service.useroctopus.utils.OctopusLevelCalculator;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import cersei.octopusservice.utils.StatsForUpgrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserOctopusProgressionService {

    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusDtoAssembler assembler;
    private final OctopusLevelCalculator levelCalculator;

    public UserOctopusAddedExpDto addExpToOctopus(
            UUID userId,
            Integer userOctopusId,
            int exp
    ) {
        if (exp <= 0) {
            throw new IllegalArgumentException(
                    "Количество опыта должно быть больше 0"
            );
        }

        UserOctopus userOctopus = getUserOctopus(userId, userOctopusId);

        int startLevel = userOctopus.getLevel();

        log.info("Octopus with id {} получил {} опыта", userOctopusId, exp);

        LevelProgress progress = levelCalculator.calculate(
                userOctopus.getLevel(),
                userOctopus.getExp(),
                exp
        );

        userOctopus.setLevel(progress.level());
        userOctopus.setExp(progress.exp());

        applyLevelBonuses(userOctopus, progress.gainedLevels());

        log.info(
                "Октопус {}: уровень {} -> {}, получено уровней {}, осталось опыта {}",
                userOctopusId,
                startLevel,
                progress.level(),
                progress.gainedLevels(),
                progress.exp()
        );

        return new UserOctopusAddedExpDto(
                assembler.toDto(userOctopus),
                startLevel,
                progress.level()
        );
    }

    public UserOctopusAddedStatsDto addStatsToOctopus(
            UUID userId,
            Integer userOctopusId,
            StatsForUpgrade stat
    ) {
        UserOctopus userOctopus = getUserOctopus(userId, userOctopusId);

        Integer freeSkillPoints = userOctopus.getCurrentFreeSkillPoints();
        log.info(
                "Игрок {} октопус {} сейчас скиллпоинтов {}",
                userId,
                userOctopusId,
                freeSkillPoints
        );

        int currentStat = userOctopus.upgradeStat(stat);

        return new UserOctopusAddedStatsDto(
                stat,
                currentStat,
                userOctopus.getCurrentFreeSkillPoints()
        );
    }

    private UserOctopus getUserOctopus(
            UUID userId,
            Integer userOctopusId
    ) {
        return userOctopusRepository
                .findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));
    }

    private void applyLevelBonuses(
            UserOctopus octopus,
            int gainedLevels
    ) {
        if (gainedLevels <= 0) {
            return;
        }

        octopus.setCurrentArmorStat(
                octopus.getCurrentArmorStat() + gainedLevels
        );
        octopus.setCurrentSpeedStat(
                octopus.getCurrentSpeedStat() + gainedLevels
        );
        octopus.setCurrentAttackStat(
                octopus.getCurrentAttackStat() + gainedLevels
        );
        octopus.setCurrentMagicPowerStat(
                octopus.getCurrentMagicPowerStat() + gainedLevels
        );
        octopus.setCurrentMagicResistStat(
                octopus.getCurrentMagicResistStat() + gainedLevels
        );
        octopus.setCurrentFreeSkillPoints(
                octopus.getCurrentFreeSkillPoints() + gainedLevels * 2
        );
    }
}