package cersei.octopusservice.service.useroctopus.utils;

import cersei.octopusservice.dto.*;
import cersei.octopusservice.model.*;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.repository.UserOctopusSkillSlotRepository;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserOctopusDtoAssembler {

    private final UserOctopusEquipmentRepository equipmentRepository;
    private final UserOctopusSkillSlotRepository skillSlotRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final SkillDtoMapper skillDtoMapper;

    public UserOctopusDto toDto(UserOctopus userOctopus) {
        List<UserOctopusEquipment> equipment =
                equipmentRepository.findByUserOctopus_IdOrderByIdAsc(
                        userOctopus.getId()
                );

        List<UserOctopusSkillSlot> skillSlots =
                skillSlotRepository.findByUserOctopus_IdOrderBySlotIndexAsc(
                        userOctopus.getId()
                );

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
                skillSlots.stream()
                        .map(this::toSkillSlotDto)
                        .toList(),
                equipment.stream()
                        .map(this::toEquipmentDto)
                        .toList()
        );
    }

    private Set<SkillDto> toSkillDtos(Set<OctopusSkill> skills) {
        return skills.stream()
                .map(skillDtoMapper::toDto)
                .collect(Collectors.toSet());
    }

    private SkillSlotDto toSkillSlotDto(UserOctopusSkillSlot slot) {
        return new SkillSlotDto(
                slot.getId(),
                slot.getSlotIndex(),
                skillDtoMapper.toDto(slot.getSkill())
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
        return itemDtoMapper.toDto(item);
    }
}
