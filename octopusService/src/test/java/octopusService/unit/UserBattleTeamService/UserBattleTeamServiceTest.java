package octopusService.unit.UserBattleTeamService;

import cersei.octopusservice.dto.BattleTeamDto;
import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.CombatStatsDto;
import cersei.octopusservice.dto.CombatTeamSnapshotDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.model.UserBattleTeam;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.utils.CombatRole;
import cersei.octopusservice.repository.UserBattleTeamRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.UserBattleTeamService;
import cersei.octopusservice.service.useroctopus.UserOctopusQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBattleTeamServiceTest {

    @Mock
    private UserBattleTeamRepository teamRepository;

    @Mock
    private UserOctopusRepository userOctopusRepository;

    @Mock
    private UserOctopusQueryService userOctopusQueryService;

    private UserBattleTeamService userBattleTeamService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userBattleTeamService = new UserBattleTeamService(
                teamRepository,
                userOctopusRepository,
                userOctopusQueryService
        );
    }

    @Test
    void when_SaveTeam_PersistsTeamArray() {
        List<Integer> ids = List.of(1, 2, 3);
        when(userOctopusRepository.findByIdAndUserId(1, userId)).thenReturn(Optional.of(createOctopus(1)));
        when(userOctopusRepository.findByIdAndUserId(2, userId)).thenReturn(Optional.of(createOctopus(2)));
        when(userOctopusRepository.findByIdAndUserId(3, userId)).thenReturn(Optional.of(createOctopus(3)));
        when(teamRepository.findById(userId)).thenReturn(Optional.empty(), Optional.of(createTeam(ids)));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BattleTeamDto team = userBattleTeamService.saveTeam(userId, ids);

        assertEquals(3, team.slots().size());
        verify(teamRepository).save(any(UserBattleTeam.class));
    }

    @Test
    void when_SaveTeam_UnknownOctopus_Throws() {
        when(userOctopusRepository.findByIdAndUserId(1, userId)).thenReturn(Optional.empty());

        assertThrows(
                OctopusNotFoundException.class,
                () -> userBattleTeamService.saveTeam(userId, List.of(1, 2, 3))
        );
    }

    @Test
    void when_GetTeamCombatSnapshots_ReturnsThreeFighters() {
        when(teamRepository.findById(userId)).thenReturn(Optional.of(createTeam(List.of(1, 2, 3))));

        CombatSnapshotDto snapshot = new CombatSnapshotDto(
                1, 10, "Ace", 5, 1, 1, CombatRole.BRUISER,
                new CombatStatsDto(1000, 10, 10, 10, 10, 10, 5, 150, 100, 0, 0, 0),
                Set.of(), List.of(), List.of()
        );
        when(userOctopusQueryService.getCombatSnapshot(eq(userId), anyInt())).thenReturn(snapshot);

        CombatTeamSnapshotDto result = userBattleTeamService.getTeamCombatSnapshots(userId);

        assertEquals(3, result.fighters().size());
        verify(userOctopusQueryService, times(3)).getCombatSnapshot(eq(userId), anyInt());
    }

    @Test
    void when_GetTeamCombatSnapshots_IncompleteTeam_Throws() {
        when(teamRepository.findById(userId)).thenReturn(Optional.of(createTeam(List.of(1))));

        assertThrows(
                IllegalArgumentException.class,
                () -> userBattleTeamService.getTeamCombatSnapshots(userId)
        );
    }

    private UserOctopus createOctopus(int id) {
        Octopus base = new Octopus();
        base.setId(10);

        UserOctopus octopus = new UserOctopus();
        octopus.setId(id);
        octopus.setUserId(userId);
        octopus.setOctopus(base);
        octopus.setRole(CombatRole.BRUISER);
        octopus.setCurrentAttackStat(10);
        octopus.setCurrentMagicPowerStat(10);
        octopus.setCurrentArmorStat(10);
        octopus.setCurrentMagicResistStat(10);
        octopus.setCurrentSpeedStat(10);
        octopus.setCurrentFreeSkillPoints(0);
        return octopus;
    }

    private UserBattleTeam createTeam(List<Integer> ids) {
        UserBattleTeam team = new UserBattleTeam();
        team.setUserId(userId);
        team.setUserOctopusIds(ids);
        return team;
    }
}
