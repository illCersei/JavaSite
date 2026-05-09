package cersei.octopusservice.service;

import cersei.octopusservice.dto.*;
import cersei.octopusservice.model.*;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.repository.UserOctopusSkillSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOctopusService {

    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusEquipmentRepository equipmentRepository;
    private final UserOctopusSkillSlotRepository skillSlotRepository;

    @Transactional(readOnly = true)
    public List<UserOctopusDto> getUserOctopuses(UUID userId) {
        return userOctopusRepository.findByUserIdOrderByIdAsc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserOctopusDto getUserOctopus(UUID userId, Integer userOctopusId) {
        UserOctopus userOctopus = userOctopusRepository.findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User octopus not found"));

        return toDto(userOctopus);
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