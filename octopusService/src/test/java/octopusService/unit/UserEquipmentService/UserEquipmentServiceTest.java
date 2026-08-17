package octopusService.unit.UserEquipmentService;

import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.UserOctopusEquipment;
import cersei.octopusservice.model.utils.CombatRole;
import cersei.octopusservice.model.utils.ItemSlot;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.ItemCatalogService;
import cersei.octopusservice.service.UserEquipmentService;
import cersei.octopusservice.service.UserItemService;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEquipmentServiceTest {

    @Mock
    private UserOctopusRepository userOctopusRepository;

    @Mock
    private UserOctopusEquipmentRepository equipmentRepository;

    @Mock
    private ItemCatalogService itemCatalogService;

    @Mock
    private UserItemService userItemService;

    @Mock
    private UserOctopusDtoAssembler assembler;

    private UserEquipmentService userEquipmentService;

    private final UUID userId = UUID.randomUUID();
    private UserOctopus userOctopus;
    private Item weapon;

    @BeforeEach
    void setUp() {
        userEquipmentService = new UserEquipmentService(
                userOctopusRepository,
                equipmentRepository,
                itemCatalogService,
                userItemService,
                assembler
        );

        Octopus base = new Octopus();
        base.setId(1);

        userOctopus = new UserOctopus();
        userOctopus.setId(10);
        userOctopus.setUserId(userId);
        userOctopus.setOctopus(base);
        userOctopus.setRole(CombatRole.BRUISER);
        userOctopus.setCurrentAttackStat(10);
        userOctopus.setCurrentMagicPowerStat(10);
        userOctopus.setCurrentArmorStat(10);
        userOctopus.setCurrentMagicResistStat(10);
        userOctopus.setCurrentSpeedStat(10);
        userOctopus.setCurrentFreeSkillPoints(0);

        weapon = new Item();
        weapon.setId(1);
        weapon.setSlot(ItemSlot.WEAPON);
    }

    @Test
    void when_Equip_ToEmptySlot_CreatesEquipment() {
        UserOctopusDto dto = mock(UserOctopusDto.class);

        when(userOctopusRepository.findByIdAndUserId(10, userId)).thenReturn(Optional.of(userOctopus));
        when(itemCatalogService.requireById(1)).thenReturn(weapon);
        when(equipmentRepository.findByUserOctopus_IdAndSlot(10, ItemSlot.WEAPON)).thenReturn(Optional.empty());
        when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assembler.toDto(userOctopus)).thenReturn(dto);

        UserOctopusDto result = userEquipmentService.equip(userId, 10, 1);

        assertSame(dto, result);
        verify(userItemService).consumeItems(userId, 1, 1);
        verify(equipmentRepository).save(any(UserOctopusEquipment.class));
        verify(userItemService, never()).addItems(any(), anyInt(), anyInt());
    }

    @Test
    void when_Equip_ReplacesExistingItem_ReturnsOldItemToStash() {
        UserOctopusEquipment existing = new UserOctopusEquipment();
        existing.setId(99);
        existing.setSlot(ItemSlot.WEAPON);
        Item oldItem = new Item();
        oldItem.setId(6);
        existing.setItem(oldItem);

        when(userOctopusRepository.findByIdAndUserId(10, userId)).thenReturn(Optional.of(userOctopus));
        when(itemCatalogService.requireById(1)).thenReturn(weapon);
        when(equipmentRepository.findByUserOctopus_IdAndSlot(10, ItemSlot.WEAPON)).thenReturn(Optional.of(existing));
        when(assembler.toDto(userOctopus)).thenReturn(mock(UserOctopusDto.class));

        userEquipmentService.equip(userId, 10, 1);

        verify(userItemService).addItems(userId, 6, 1);
        verify(equipmentRepository).save(existing);
        assertEquals(weapon, existing.getItem());
    }

    @Test
    void when_Unequip_ReturnsItemToStash() {
        UserOctopusEquipment equipment = new UserOctopusEquipment();
        equipment.setSlot(ItemSlot.WEAPON);
        Item equipped = new Item();
        equipped.setId(1);
        equipment.setItem(equipped);

        when(userOctopusRepository.findByIdAndUserId(10, userId)).thenReturn(Optional.of(userOctopus));
        when(equipmentRepository.findByUserOctopus_IdAndSlot(10, ItemSlot.WEAPON)).thenReturn(Optional.of(equipment));
        when(assembler.toDto(userOctopus)).thenReturn(mock(UserOctopusDto.class));

        userEquipmentService.unequip(userId, 10, ItemSlot.WEAPON);

        verify(userItemService).addItems(userId, 1, 1);
        verify(equipmentRepository).delete(equipment);
    }

    @Test
    void when_Equip_UnknownOctopus_Throws() {
        when(userOctopusRepository.findByIdAndUserId(10, userId)).thenReturn(Optional.empty());

        assertThrows(
                OctopusNotFoundException.class,
                () -> userEquipmentService.equip(userId, 10, 1)
        );
    }
}
