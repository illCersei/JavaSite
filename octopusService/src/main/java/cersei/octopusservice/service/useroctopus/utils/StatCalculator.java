package cersei.octopusservice.service.useroctopus.utils;

import cersei.octopusservice.dto.CombatStatsDto;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.UserOctopusEquipment;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatCalculator {

    private static final int HP_BASE = 500;
    private static final int HP_PER_LEVEL = 50;
    private static final int HP_PER_ARMOR = 5;
    private static final int HP_PER_MAGIC_RESIST = 3;

    // Every octopus starts identical on these (docs/OCTOPUS_MINIGAME_PLAN.md §16.2) - items are
    // the only differentiator for v1.
    private static final int BASE_CRIT_CHANCE = 5;
    private static final int BASE_CRIT_DAMAGE = 150;
    private static final int BASE_ACCURACY = 100;
    private static final int BASE_EVASION = 0;
    private static final int BASE_TENACITY = 0;
    private static final int BASE_STATUS_POWER = 0;

    // Balance caps from docs/OCTOPUS_MINIGAME_PLAN.md §16.7.
    private static final int MAX_CRIT_CHANCE = 75;
    private static final int MAX_EVASION = 45;

    private final UserOctopusEquipmentRepository equipmentRepository;

    public CombatStatsDto computeWithEquipment(UserOctopus userOctopus) {
        int attack = userOctopus.getCurrentAttackStat();
        int magicPower = userOctopus.getCurrentMagicPowerStat();
        int armor = userOctopus.getCurrentArmorStat();
        int magicResist = userOctopus.getCurrentMagicResistStat();
        int speed = userOctopus.getCurrentSpeedStat();
        int critChance = BASE_CRIT_CHANCE;
        int critDamage = BASE_CRIT_DAMAGE;
        int accuracy = BASE_ACCURACY;
        int evasion = BASE_EVASION;
        int tenacity = BASE_TENACITY;
        int statusPower = BASE_STATUS_POWER;

        List<UserOctopusEquipment> equipment =
                equipmentRepository.findByUserOctopus_IdOrderByIdAsc(userOctopus.getId());

        for (UserOctopusEquipment row : equipment) {
            Item item = row.getItem();
            attack += item.getAttackStat();
            magicPower += item.getMagicPowerStat();
            armor += item.getArmorStat();
            magicResist += item.getMagicResistStat();
            speed += item.getSpeedStat();
            critChance += item.getCritChance();
            critDamage += item.getCritDamage();
            accuracy += item.getAccuracy();
            evasion += item.getEvasion();
            tenacity += item.getTenacity();
            statusPower += item.getStatusPower();
        }

        int hp = HP_BASE
                + userOctopus.getLevel() * HP_PER_LEVEL
                + armor * HP_PER_ARMOR
                + magicResist * HP_PER_MAGIC_RESIST;

        return new CombatStatsDto(
                hp, attack, magicPower, armor, magicResist, speed,
                Math.min(critChance, MAX_CRIT_CHANCE),
                critDamage,
                accuracy,
                Math.min(evasion, MAX_EVASION),
                tenacity,
                statusPower);
    }
}
