package cersei.octopusservice.service;

import cersei.octopusservice.dto.*;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.*;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.repository.UserOctopusSkillSlotRepository;
import cersei.octopusservice.utils.StatsForUpgrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOctopusService {

    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusEquipmentRepository equipmentRepository;
    private final UserOctopusSkillSlotRepository skillSlotRepository;

    @Transactional(readOnly = true)
    public List<UserOctopusDto> getAllUserOctopuses(UUID userId) {
        return userOctopusRepository.findByUserIdOrderByIdAsc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserOctopusDto getUserOctopusById(UUID userId, Integer userOctopusId) {
        UserOctopus userOctopus = userOctopusRepository.findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User octopus not found"));

        return toDto(userOctopus);
    }

    @Transactional
    public UserOctopusAddedExpDto addExpToOctopus(
            UUID userId,
            Integer userOctopusId,
            int exp
    ) {
        if (exp <= 0) {
            throw new IllegalArgumentException("Количество опыта должно быть больше 0");
        }

        UserOctopus userOctopus = userOctopusRepository
                .findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));

        int startLevel = userOctopus.getLevel();
        int newLevel = startLevel;
        int newExp = userOctopus.getExp() + exp;
        int gainedLevels = 0;

        log.info("Octopus with id {} получил {} опыта", userOctopusId, exp);

        while (newExp >= expToNextLevel(newLevel)) {
            newExp -= expToNextLevel(newLevel);
            newLevel++;
            gainedLevels++;
        }

        userOctopus.setLevel(newLevel);
        userOctopus.setExp(newExp);

        if (gainedLevels > 0) {
            userOctopus.setCurrentArmorStat(
                    userOctopus.getCurrentArmorStat() + gainedLevels
            );

            userOctopus.setCurrentSpeedStat(
                    userOctopus.getCurrentSpeedStat() + gainedLevels
            );

            userOctopus.setCurrentAttackStat(
                    userOctopus.getCurrentAttackStat() + gainedLevels
            );

            userOctopus.setCurrentMagicPowerStat(
                    userOctopus.getCurrentMagicPowerStat() + gainedLevels
            );

            userOctopus.setCurrentMagicResistStat(
                    userOctopus.getCurrentMagicResistStat() + gainedLevels
            );

            userOctopus.setCurrentFreeSkillPoints(
                    userOctopus.getCurrentFreeSkillPoints() + gainedLevels * 2
            );
        }

        log.info(
                "Октопус {}: уровень {} -> {}, получено уровней {}, осталось опыта {}",
                userOctopusId,
                startLevel,
                newLevel,
                gainedLevels,
                newExp
        );

        return new UserOctopusAddedExpDto(
                toDto(userOctopus),
                startLevel,
                newLevel
        );
    }

    private int expToNextLevel(int level) {
        return 20 * (1 << (level - 1));
    }

    @Transactional
    public UserOctopusAddedStatsDto addStatsToOctopus(UUID userId, Integer userOctopusId, StatsForUpgrade stat) {
        UserOctopus userOctopus = userOctopusRepository.findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));

        Integer freeSkillPoints = userOctopus.getCurrentFreeSkillPoints();
        log.info("Игрок {} октопус {} сейчас скиллпоинтов {}", userId, userOctopusId, freeSkillPoints);

        //it cant be less than 0
        if (freeSkillPoints <= 0) {
            throw new IllegalArgumentException("Нет свободных скиллпоинтов");
        }

        int currentStat = switch (stat) {
            case ARMOR -> {
                int newValue = userOctopus.getCurrentArmorStat() + 1;
                userOctopus.setCurrentArmorStat(newValue);
                yield newValue;
            }
            case SPEED -> {
                int newValue = userOctopus.getCurrentSpeedStat() + 1;
                userOctopus.setCurrentSpeedStat(newValue);
                yield newValue;
            }
            case ATTACK -> {
                int newValue = userOctopus.getCurrentAttackStat() + 1;
                userOctopus.setCurrentAttackStat(newValue);
                yield newValue;
            }
            case MAGIC_POWER -> {
                int newValue = userOctopus.getCurrentMagicPowerStat() + 1;
                userOctopus.setCurrentMagicPowerStat(newValue);
                yield newValue;
            }
            case MAGIC_RESIST -> {
                int newValue = userOctopus.getCurrentMagicResistStat() + 1;
                userOctopus.setCurrentMagicResistStat(newValue);
                yield newValue;
            }
        };

        userOctopus.setCurrentFreeSkillPoints(
                userOctopus.getCurrentFreeSkillPoints() - 1
        );

        return new UserOctopusAddedStatsDto(
                stat,
                currentStat,
                userOctopus.getCurrentFreeSkillPoints()
        );
    }

    private UserOctopusDto toDto(UserOctopus userOctopus) {
        List<UserOctopusEquipment> equipment =
                equipmentRepository.findByUserOctopus_IdOrderByIdAsc(userOctopus.getId());

        List<UserOctopusSkillSlot> skillSlots =
                skillSlotRepository.findByUserOctopus_IdOrderBySlotIndexAsc(userOctopus.getId());

        return new UserOctopusDto(
                userOctopus.getId(),
                userOctopus.getOctopus().getId(),
                userOctopus.getNickname(),
                userOctopus.getLevel(),
                userOctopus.getCurrentTier(),
                userOctopus.getStars(),
                userOctopus.getRole(),
                userOctopus.getExp(),
                userOctopus.getCurrentAttackStat(),
                userOctopus.getCurrentMagicPowerStat(),
                userOctopus.getCurrentArmorStat(),
                userOctopus.getCurrentMagicResistStat(),
                userOctopus.getCurrentSpeedStat(),
                userOctopus.getCurrentFreeSkillPoints(),
                toSkillDtos(userOctopus.getOpenSkills()),
                skillSlots.stream().map(this::toSkillSlotDto).toList(),
                equipment.stream().map(this::toEquipmentDto).toList()
        );
    }

    private Set<SkillDto> toSkillDtos(Set<OctopusSkill> skills) {
        return skills.stream()
                .map(this::toSkillDto)
                .collect(Collectors.toSet());
    }

    private SkillSlotDto toSkillSlotDto(UserOctopusSkillSlot slot) {
        return new SkillSlotDto(
                slot.getId(),
                slot.getSlotIndex(),
                slot.getSkill() == null ? null : toSkillDto(slot.getSkill())
        );
    }

    private SkillDto toSkillDto(OctopusSkill skill) {
        return new SkillDto(
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skill.getElementType().name(),
                skill.getCooldownMs(),
                skill.getManaCost()
        );
    }

    private EquipmentDto toEquipmentDto(UserOctopusEquipment equipment) {
        return new EquipmentDto(
                equipment.getId(),
                equipment.getSlot(),
                toItemDto(equipment.getItem())
        );
    }

    private ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getSlot(),
                item.getTier(),
                item.getAttackStat(),
                item.getMagicPowerStat(),
                item.getArmorStat(),
                item.getMagicResistStat(),
                item.getSpeedStat()
        );
    }
}