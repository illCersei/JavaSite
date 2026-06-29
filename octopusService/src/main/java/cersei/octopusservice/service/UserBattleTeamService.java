package cersei.octopusservice.service;

import cersei.octopusservice.dto.BattleTeamDto;
import cersei.octopusservice.dto.BattleTeamSlotDto;
import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.CombatTeamSnapshotDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.UserBattleTeamSlot;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.repository.UserBattleTeamSlotRepository;
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

    private final UserBattleTeamSlotRepository teamSlotRepository;
    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusQueryService userOctopusQueryService;

    @Transactional(readOnly = true)
    public BattleTeamDto getTeam(UUID userId) {
        List<UserBattleTeamSlot> slots = teamSlotRepository.findByUserIdOrderBySlotIndexAsc(userId);
        log.info("Игрок {} запросил боевую команду, заполнено слотов={}", userId, slots.size());
        return toDto(slots);
    }

    @Transactional
    public BattleTeamDto saveTeam(UUID userId, List<Integer> userOctopusIds) {
        log.info("Игрок {} сохраняет боевую команду userOctopusIds={}", userId, userOctopusIds);

        List<UserOctopus> octopuses = new ArrayList<>(TEAM_SIZE);
        for (Integer userOctopusId : userOctopusIds) {
            UserOctopus octopus = userOctopusRepository.findByIdAndUserId(userOctopusId, userId)
                    .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));
            octopuses.add(octopus);
        }

        teamSlotRepository.deleteByUserId(userId);

        for (int slotIndex = 0; slotIndex < TEAM_SIZE; slotIndex++) {
            UserBattleTeamSlot slot = new UserBattleTeamSlot();
            slot.setUserId(userId);
            slot.setSlotIndex(slotIndex);
            slot.setUserOctopus(octopuses.get(slotIndex));
            slot.setUpdatedAt(Instant.now());
            teamSlotRepository.save(slot);
        }

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
        List<UserBattleTeamSlot> slots = teamSlotRepository.findByUserIdOrderBySlotIndexAsc(userId);
        if (slots.size() != TEAM_SIZE) {
            throw new IllegalArgumentException(
                    "Боевая команда не собрана: нужно ровно " + TEAM_SIZE + " осьминога, сейчас " + slots.size()
            );
        }

        log.info("Игрок {} запросил combat snapshots для команды из {} бойцов", userId, TEAM_SIZE);

        List<CombatSnapshotDto> fighters = slots.stream()
                .map(slot -> userOctopusQueryService.getCombatSnapshot(userId, slot.getUserOctopus().getId()))
                .toList();

        return new CombatTeamSnapshotDto(fighters);
    }

    private BattleTeamDto toDto(List<UserBattleTeamSlot> slots) {
        List<BattleTeamSlotDto> teamSlots = slots.stream()
                .map(slot -> new BattleTeamSlotDto(slot.getSlotIndex(), slot.getUserOctopus().getId()))
                .toList();
        return new BattleTeamDto(teamSlots);
    }
}
