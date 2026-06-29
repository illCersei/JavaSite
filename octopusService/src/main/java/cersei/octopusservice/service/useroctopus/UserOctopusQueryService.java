package cersei.octopusservice.service.useroctopus;

import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.utils.StatCalculator;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserOctopusQueryService {

    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusDtoAssembler assembler;
    private final StatCalculator statCalculator;

    public List<UserOctopusDto> getAllUserOctopuses(UUID userId) {
        return userOctopusRepository.findByUserIdOrderByIdAsc(userId)
                .stream()
                .map(assembler::toDto)
                .toList();
    }

    public UserOctopusDto getUserOctopusById(UUID userId, Integer userOctopusId) {
        UserOctopus userOctopus = userOctopusRepository
                .findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));

        return assembler.toDto(userOctopus);
    }

    public CombatSnapshotDto getCombatSnapshot(UUID userId, Integer userOctopusId) {
        log.info("Combat snapshot userId={} userOctopusId={}", userId, userOctopusId);

        UserOctopus userOctopus = userOctopusRepository
                .findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));

        UserOctopusDto dto = assembler.toDto(userOctopus);

        return new CombatSnapshotDto(
                dto.id(),
                dto.baseOctopusId(),
                dto.nickname(),
                dto.level(),
                dto.currentTier(),
                dto.stars(),
                dto.role(),
                statCalculator.computeWithEquipment(userOctopus),
                dto.openSkills(),
                dto.skillSlots(),
                dto.equipment()
        );
    }
}