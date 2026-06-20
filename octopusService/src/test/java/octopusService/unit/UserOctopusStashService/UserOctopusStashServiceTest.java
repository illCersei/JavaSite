package octopusService.unit.UserOctopusStashService;

import cersei.octopusservice.dto.InventoryLineDto;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.model.UserOctopusStash;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.repository.UserOctopusStashRepository;
import cersei.octopusservice.service.OctopusCatalogService;
import cersei.octopusservice.service.UserOctopusStashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOctopusStashServiceTest {

    @Mock
    private UserOctopusStashRepository stashRepository;

    @Mock
    private OctopusCatalogService octopusCatalogService;

    @Mock
    private UserOctopusRepository userOctopusRepository;

    @InjectMocks
    private UserOctopusStashService inventoryService;

    private UUID userId;

    private UserOctopusStash stash;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        stash = new UserOctopusStash(userId, 1, 2, Instant.now());
    }

    @Test
    void when_AddOneExistingStash_IncrementsQuantity() {

        when(stashRepository.findByUserIdAndOctopusId(userId, 1))
                .thenReturn(Optional.of(stash));

        int result = inventoryService.addOne(userId, 1);

        assertEquals(3, result);
        assertEquals(3, stash.getQuantity());
        verify(stashRepository).save(stash);
    }

    @Test
    void when_AddOneNewStash_CreatesWithQuantityOne() {
        when(stashRepository.findByUserIdAndOctopusId(userId, 1))
                .thenReturn(Optional.empty());

        int result = inventoryService.addOne(userId, 1);

        assertEquals(1, result);

        ArgumentCaptor<UserOctopusStash> captor =
                ArgumentCaptor.forClass(UserOctopusStash.class);

        verify(stashRepository).save(captor.capture());

        UserOctopusStash saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(1, saved.getOctopusId());
        assertEquals(1, saved.getQuantity());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void when_ListWithDetails_ReturnInventoryLinesWithTotalQuantity() {
        when(stashRepository.findByUserIdOrderByOctopusIdAsc(userId))
                .thenReturn(List.of(stash));

        OctopusSummaryDto base = new OctopusSummaryDto(
                1, "Blue Octopus", "STORM", 1, "image-1.png",
                10, 20, 30, 40, 50, 1
        );
        when(octopusCatalogService.getById(1)).thenReturn(base);
        when(userOctopusRepository.countByUserIdAndOctopus_Id(userId, 1))
                .thenReturn(3L);

        List<InventoryLineDto> result = inventoryService.listWithDetails(userId);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).octopus().quantity());
        assertEquals("Blue Octopus", result.get(0).octopus().name());
    }

    @Test
    void when_ListWithDetails_AndStashEmpty_ReturnsEmptyList() {
        when(stashRepository.findByUserIdOrderByOctopusIdAsc(userId))
                .thenReturn(List.of());

        List<InventoryLineDto> result = inventoryService.listWithDetails(userId);

        assertEquals(0, result.size());
    }
}
