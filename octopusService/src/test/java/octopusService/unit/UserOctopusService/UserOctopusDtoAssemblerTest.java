package octopusService.unit.UserOctopusService;

import cersei.octopusservice.dto.EquipmentDto;
import cersei.octopusservice.dto.ItemDto;
import cersei.octopusservice.dto.SkillDto;
import cersei.octopusservice.dto.SkillSlotDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.model.*;
import cersei.octopusservice.model.utils.CombatRole;
import cersei.octopusservice.model.utils.ElementType;
import cersei.octopusservice.model.utils.ItemSlot;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.repository.UserOctopusSkillSlotRepository;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import cersei.octopusservice.service.useroctopus.utils.SkillDtoMapper;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOctopusDtoAssemblerTest {

    @Mock
    private UserOctopusEquipmentRepository equipmentRepository;

    @Mock
    private UserOctopusSkillSlotRepository skillSlotRepository;

    private final ItemDtoMapper itemDtoMapper = new ItemDtoMapper();

    private UserOctopusDtoAssembler assembler;

    private UserOctopus userOctopus;

    @BeforeEach
    void setUp() {
        assembler = new UserOctopusDtoAssembler(
                equipmentRepository,
                skillSlotRepository,
                itemDtoMapper,
                new SkillDtoMapper()
        );

        Octopus baseOctopus = new Octopus();
        baseOctopus.setId(10);

        userOctopus = new UserOctopus();
        userOctopus.setId(1);
        userOctopus.setOctopus(baseOctopus);
        userOctopus.setNickname("Ace");
        userOctopus.setLevel(5);
        userOctopus.setCurrentTier(2);
        userOctopus.setStars(3);
        userOctopus.setRole(CombatRole.BRUISER);
        userOctopus.setExp(42);
        userOctopus.setCurrentAttackStat(11);
        userOctopus.setCurrentMagicPowerStat(12);
        userOctopus.setCurrentArmorStat(13);
        userOctopus.setCurrentMagicResistStat(14);
        userOctopus.setCurrentSpeedStat(15);
        userOctopus.setCurrentFreeSkillPoints(4);
    }

    @Test
    void when_ToDto_MapsAllFieldsWithRelations() {
        OctopusSkill skill = createSkill(7, "Ink Blast");
        userOctopus.setOpenSkills(Set.of(skill));

        UserOctopusSkillSlot filledSlot = createSkillSlot(100L, 0, skill);
        UserOctopusSkillSlot emptySlot = createSkillSlot(101L, 1, null);
        UserOctopusEquipment equipment = createEquipment(200, ItemSlot.WEAPON, "Trident");

        when(equipmentRepository.findByUserOctopus_IdOrderByIdAsc(1))
                .thenReturn(List.of(equipment));
        when(skillSlotRepository.findByUserOctopus_IdOrderBySlotIndexAsc(1))
                .thenReturn(List.of(filledSlot, emptySlot));

        UserOctopusDto dto = assembler.toDto(userOctopus);

        assertEquals(1, dto.id());
        assertEquals(10, dto.baseOctopusId());
        assertEquals("Ace", dto.nickname());
        assertEquals(5, dto.level());
        assertEquals(2, dto.currentTier());
        assertEquals(3, dto.stars());
        assertEquals(CombatRole.BRUISER, dto.role());
        assertEquals(42, dto.exp());
        assertEquals(11, dto.currentAttackStat());
        assertEquals(12, dto.currentMagicPowerStat());
        assertEquals(13, dto.currentArmorStat());
        assertEquals(14, dto.currentMagicResistStat());
        assertEquals(15, dto.currentSpeedStat());
        assertEquals(4, dto.currentFreeSkillPoints());

        assertEquals(1, dto.openSkills().size());
        SkillDto openSkill = dto.openSkills().iterator().next();
        assertEquals(7, openSkill.id());
        assertEquals("Ink Blast", openSkill.name());

        assertEquals(2, dto.skillSlots().size());

        SkillSlotDto firstSlot = dto.skillSlots().get(0);
        assertEquals(100L, firstSlot.id());
        assertEquals(0, firstSlot.slotIndex());
        assertEquals(7, firstSlot.skill().id());

        SkillSlotDto secondSlot = dto.skillSlots().get(1);
        assertEquals(101L, secondSlot.id());
        assertEquals(1, secondSlot.slotIndex());
        assertNull(secondSlot.skill());

        assertEquals(1, dto.equipment().size());
        EquipmentDto equipmentDto = dto.equipment().get(0);
        assertEquals(200, equipmentDto.id());
        assertEquals(ItemSlot.WEAPON, equipmentDto.slot());

        ItemDto itemDto = equipmentDto.item();
        assertEquals(300, itemDto.id());
        assertEquals("Trident", itemDto.name());
        assertEquals(ItemSlot.WEAPON, itemDto.slot());
        assertEquals(2, itemDto.tier());
    }

    @Test
    void when_NoRelations_ReturnsEmptyCollections() {
        userOctopus.setOpenSkills(Set.of());

        when(equipmentRepository.findByUserOctopus_IdOrderByIdAsc(1))
                .thenReturn(List.of());
        when(skillSlotRepository.findByUserOctopus_IdOrderBySlotIndexAsc(1))
                .thenReturn(List.of());

        UserOctopusDto dto = assembler.toDto(userOctopus);

        assertTrue(dto.openSkills().isEmpty());
        assertTrue(dto.skillSlots().isEmpty());
        assertTrue(dto.equipment().isEmpty());
    }

    private OctopusSkill createSkill(int id, String name) {
        OctopusSkill skill = new OctopusSkill();
        skill.setId(id);
        skill.setName(name);
        skill.setDescription("desc");
        skill.setElementType(ElementType.PHYSICAL);
        skill.setCooldownMs(1000);
        skill.setManaCost(5);
        return skill;
    }

    private UserOctopusSkillSlot createSkillSlot(Long id, int slotIndex, OctopusSkill skill) {
        UserOctopusSkillSlot slot = new UserOctopusSkillSlot();
        slot.setId(id);
        slot.setSlotIndex(slotIndex);
        slot.setSkill(skill);
        return slot;
    }

    private UserOctopusEquipment createEquipment(int id, ItemSlot slot, String itemName) {
        Item item = new Item();
        item.setId(300);
        item.setName(itemName);
        item.setDescription("item desc");
        item.setSlot(slot);
        item.setTier(2);
        item.setAttackStat(1);
        item.setMagicPowerStat(2);
        item.setArmorStat(3);
        item.setMagicResistStat(4);
        item.setSpeedStat(5);

        UserOctopusEquipment equipment = new UserOctopusEquipment();
        equipment.setId(id);
        equipment.setSlot(slot);
        equipment.setItem(item);
        return equipment;
    }
}