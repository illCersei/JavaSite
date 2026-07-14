package cersei.octopusservice.service;

import cersei.octopusservice.dto.BattleTeamDto;
import cersei.octopusservice.dto.BattleTeamSlotDto;
import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.CombatTeamSnapshotDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserBattleTeam;
import cersei.octopusservice.repository.UserBattleTeamRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.UserOctopusQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBattleTeamService {

    public static final int TEAM_SIZE = 3;

    private final UserBattleTeamRepository teamRepository;
    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusQueryService userOctopusQueryService;

    @Transactional(readOnly = true)
    public BattleTeamDto getTeam(UUID userId) {
        BattleTeamDto team = teamRepository.findById(userId)
                .map(this::toDto)
                .orElseGet(() -> new BattleTeamDto(List.of()));
        log.info("Игрок {} запросил боевую команду, заполнено слотов={}", userId, team.slots().size());
        return team;
    }

    @Transactional
    public BattleTeamDto saveTeam(UUID userId, List<Integer> userOctopusIds) {
        log.info("Игрок {} сохраняет боевую команду userOctopusIds={}", userId, userOctopusIds);

        for (Integer userOctopusId : userOctopusIds) {
            userOctopusRepository.findByIdAndUserId(userOctopusId, userId)
                    .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));
        }

        UserBattleTeam team = teamRepository.findById(userId).orElseGet(() -> {
            UserBattleTeam created = new UserBattleTeam();
            created.setUserId(userId);
            return created;
        });
        team.setUserOctopusIds(new ArrayList<>(userOctopusIds));
        team.setUpdatedAt(Instant.now());
        teamRepository.save(team);

        log.info(
                "Игрок {} сохранил боевую команду: slot0={} slot1={} slot2={}",
                userId,
                userOctopusIds.get(0),
                userOctopusIds.get(1),
                userOctopusIds.get(2)
        );

        return getTeam(userId);
    }

    @Transactional(readOnly = true)
    public CombatTeamSnapshotDto getTeamCombatSnapshots(UUID userId) {
        List<Integer> userOctopusIds = teamRepository.findById(userId)
                .map(UserBattleTeam::getUserOctopusIds)
                .orElse(List.of());

        if (userOctopusIds.size() != TEAM_SIZE) {
            throw new IllegalArgumentException(
                    "Боевая команда не собрана: нужно ровно " + TEAM_SIZE + " осьминога, сейчас " + userOctopusIds.size()
            );
        }

        log.info("Игрок {} запросил combat snapshots для команды из {} бойцов", userId, TEAM_SIZE);

        List<CombatSnapshotDto> fighters = userOctopusIds.stream()
                .map(userOctopusId -> userOctopusQueryService.getCombatSnapshot(userId, userOctopusId))
                .toList();

        return new CombatTeamSnapshotDto(fighters);
    }

    private BattleTeamDto toDto(UserBattleTeam team) {
        List<BattleTeamSlotDto> slots = new ArrayList<>(TEAM_SIZE);
        for (int slotIndex = 0; slotIndex < team.getUserOctopusIds().size(); slotIndex++) {
            slots.add(new BattleTeamSlotDto(slotIndex, team.getUserOctopusIds().get(slotIndex)));
        }
        return new BattleTeamDto(slots);
    }
}
