package octopusService.unit.UserOctopusStashService;

import cersei.octopusservice.dto.InventoryLineDto;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.UserOctopusStash;
import cersei.octopusservice.repository.OctopusCatalogRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.repository.UserOctopusStashRepository;
import cersei.octopusservice.service.IdempotencyService;
import cersei.octopusservice.service.OctopusCatalogService;
import cersei.octopusservice.service.UserOctopusStashService;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOctopusStashServiceTest {

    @Mock
    private UserOctopusStashRepository stashRepository;

    @Mock
    private OctopusCatalogService octopusCatalogService;

    @Mock
    private OctopusCatalogRepository octopusCatalogRepository;

    @Mock
    private UserOctopusRepository userOctopusRepository;

    @Mock
    private UserOctopusDtoAssembler userOctopusDtoAssembler;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private UserOctopusStashService inventoryService;

    private UUID userId;

    private UserOctopusStash stash;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        stash = new UserOctopusStash(userId, 1, 2, Instant.now());
    }

    @SuppressWarnings("unchecked")
    private void stubIdempotencyPassthrough() {
        when(idempotencyService.run(eq(userId), eq(UserOctopusStashService.ACTION_SUMMON), any(), eq(UserOctopusDto.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<UserOctopusDto> supplier = invocation.getArgument(4);
                    return supplier.get();
                });
    }

    @Test
    void when_Summon_DecrementsStashAndCreatesInstance() {
        stubIdempotencyPassthrough();
        when(stashRepository.findByUserIdAndOctopusId(userId, 1)).thenReturn(Optional.of(stash));

        Octopus template = new Octopus();
        template.setId(1);
        template.setAttackStat(6);
        template.setMagicPowerStat(4);
        template.setArmorStat(5);
        template.setMagicResistStat(3);
        template.setSpeedStat(8);
        template.setFreeSkillPoints(1);
        when(octopusCatalogRepository.findById(1)).thenReturn(Optional.of(template));

        when(userOctopusRepository.save(any(UserOctopus.class))).thenAnswer(invocation -> {
            UserOctopus saved = invocation.getArgument(0);
            saved.setId(42);
            return saved;
        });
        UserOctopusDto expectedDto = new UserOctopusDto(
                42, 1, null, 1, 1, 1, null, 0, 6, 4, 5, 3, 8, 1, Set.of(), List.of(), List.of());
        when(userOctopusDtoAssembler.toDto(any(UserOctopus.class))).thenReturn(expectedDto);

        UserOctopusDto result = inventoryService.summon(userId, 1, "key-1");

        assertEquals(1, stash.getQuantity());
        assertEquals(expectedDto, result);
        verify(stashRepository).save(stash);

        ArgumentCaptor<UserOctopus> captor = ArgumentCaptor.forClass(UserOctopus.class);
        verify(userOctopusRepository).save(captor.capture());
        UserOctopus created = captor.getValue();
        assertEquals(userId, created.getUserId());
        assertEquals(template, created.getOctopus());
        assertEquals(6, created.getCurrentAttackStat());
        assertEquals(1, created.getCurrentFreeSkillPoints());
    }

    @Test
    void when_Summon_NoStashRow_Throws() {
        stubIdempotencyPassthrough();
        when(stashRepository.findByUserIdAndOctopusId(userId, 1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.summon(userId, 1, "key-1"));
    }

    @Test
    void when_Summon_StashQuantityZero_Throws() {
        stubIdempotencyPassthrough();
        UserOctopusStash emptyStash = new UserOctopusStash(userId, 1, 0, Instant.now());
        when(stashRepository.findByUserIdAndOctopusId(userId, 1)).thenReturn(Optional.of(emptyStash));

        assertThrows(IllegalArgumentException.class, () -> inventoryService.summon(userId, 1, "key-1"));
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
