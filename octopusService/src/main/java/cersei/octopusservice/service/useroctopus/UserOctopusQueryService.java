package cersei.octopusservice.service.useroctopus;

import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserOctopusQueryService {

    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusDtoAssembler assembler;

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
}