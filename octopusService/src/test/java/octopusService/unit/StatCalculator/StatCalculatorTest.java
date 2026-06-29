package octopusService.unit.StatCalculator;

import cersei.octopusservice.dto.CombatStatsDto;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.UserOctopusEquipment;
import cersei.octopusservice.model.utils.CombatRole;
import cersei.octopusservice.model.utils.ItemSlot;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.service.useroctopus.utils.StatCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatCalculatorTest {

    @Mock
    private UserOctopusEquipmentRepository equipmentRepository;

    private StatCalculator statCalculator;

    @BeforeEach
    void setUp() {
        statCalculator = new StatCalculator(equipmentRepository);
    }

    @Test
    void when_ComputeWithEquipment_AddsFlatItemStatsAndHp() {
        UserOctopus userOctopus = new UserOctopus();
        userOctopus.setId(1);
        userOctopus.setLevel(5);
        userOctopus.setOctopus(new Octopus());
        userOctopus.setRole(CombatRole.BRUISER);
        userOctopus.setCurrentAttackStat(10);
        userOctopus.setCurrentMagicPowerStat(5);
        userOctopus.setCurrentArmorStat(8);
        userOctopus.setCurrentMagicResistStat(4);
        userOctopus.setCurrentSpeedStat(6);
        userOctopus.setCurrentFreeSkillPoints(0);

        Item weapon = new Item();
        weapon.setAttackStat(6);
        weapon.setMagicPowerStat(0);
        weapon.setArmorStat(2);
        weapon.setMagicResistStat(1);
        weapon.setSpeedStat(1);

        UserOctopusEquipment equipment = new UserOctopusEquipment();
        equipment.setSlot(ItemSlot.WEAPON);
        equipment.setItem(weapon);

        when(equipmentRepository.findByUserOctopus_IdOrderByIdAsc(1)).thenReturn(List.of(equipment));

        CombatStatsDto stats = statCalculator.computeWithEquipment(userOctopus);

        assertEquals(16, stats.attack());
        assertEquals(5, stats.magicPower());
        assertEquals(10, stats.armor());
        assertEquals(5, stats.magicResist());
        assertEquals(7, stats.speed());
        assertEquals(500 + 5 * 50 + 10 * 5 + 5 * 3, stats.hp());
    }
}
