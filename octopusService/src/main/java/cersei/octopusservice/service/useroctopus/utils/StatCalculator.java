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

    private final UserOctopusEquipmentRepository equipmentRepository;

    public CombatStatsDto computeWithEquipment(UserOctopus userOctopus) {
        int attack = userOctopus.getCurrentAttackStat();
        int magicPower = userOctopus.getCurrentMagicPowerStat();
        int armor = userOctopus.getCurrentArmorStat();
        int magicResist = userOctopus.getCurrentMagicResistStat();
        int speed = userOctopus.getCurrentSpeedStat();

        List<UserOctopusEquipment> equipment =
                equipmentRepository.findByUserOctopus_IdOrderByIdAsc(userOctopus.getId());

        for (UserOctopusEquipment row : equipment) {
            Item item = row.getItem();
            attack += item.getAttackStat();
            magicPower += item.getMagicPowerStat();
            armor += item.getArmorStat();
            magicResist += item.getMagicResistStat();
            speed += item.getSpeedStat();
        }

        int hp = HP_BASE
                + userOctopus.getLevel() * HP_PER_LEVEL
                + armor * HP_PER_ARMOR
                + magicResist * HP_PER_MAGIC_RESIST;

        return new CombatStatsDto(hp, attack, magicPower, armor, magicResist, speed);
    }
}
