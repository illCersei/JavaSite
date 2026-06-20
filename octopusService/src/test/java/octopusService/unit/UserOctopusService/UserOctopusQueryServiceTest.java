package octopusService.unit.UserOctopusService;

import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.UserOctopusQueryService;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOctopusQueryServiceTest {

    @Mock
    private UserOctopusRepository userOctopusRepository;

    @Mock
    private UserOctopusDtoAssembler assembler;

    @InjectMocks
    private UserOctopusQueryService userOctopusQueryService;

    private UUID userId;
    private UserOctopus octopus;
    private UserOctopusDto octopusDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        octopus = new UserOctopus();
        octopus.setId(1);
        octopus.setUserId(userId);

        octopusDto = new UserOctopusDto(
                1, 10, "Test", 1, 1, 1, null,
                0, 10, 10, 10, 10, 10, 0,
                null, null, null
        );
    }

    @Test
    void when_GetAllUserOctopuses_ReturnsMappedDtos() {
        when(userOctopusRepository.findByUserIdOrderByIdAsc(userId))
                .thenReturn(List.of(octopus));
        when(assembler.toDto(octopus)).thenReturn(octopusDto);

        List<UserOctopusDto> result =
                userOctopusQueryService.getAllUserOctopuses(userId);

        assertEquals(1, result.size());
        assertEquals(octopusDto, result.get(0));
        verify(assembler).toDto(octopus);
    }

    @Test
    void when_GetAllUserOctopuses_AndListEmpty_ReturnsEmptyList() {
        when(userOctopusRepository.findByUserIdOrderByIdAsc(userId))
                .thenReturn(List.of());

        List<UserOctopusDto> result =
                userOctopusQueryService.getAllUserOctopuses(userId);

        assertEquals(0, result.size());
    }

    @Test
    void when_GetExistingOctopus_ReturnsDto() {
        when(userOctopusRepository.findByIdAndUserId(1, userId))
                .thenReturn(Optional.of(octopus));
        when(assembler.toDto(octopus)).thenReturn(octopusDto);

        UserOctopusDto result =
                userOctopusQueryService.getUserOctopusById(userId, 1);

        assertEquals(octopusDto, result);
        verify(assembler).toDto(octopus);
    }

    @Test
    void when_GetNotExistingOctopus_ExceptionThrows() {
        when(userOctopusRepository.findByIdAndUserId(99, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                OctopusNotFoundException.class,
                () -> userOctopusQueryService.getUserOctopusById(userId, 99)
        );
    }
}