package cersei.octopusservice.service.dungeon;

import cersei.octopusservice.client.FightServiceClient;
import cersei.octopusservice.client.WalletClient;
import cersei.octopusservice.dto.CombatTeamSnapshotDto;
import cersei.octopusservice.dto.WalletOperationRequest;
import cersei.octopusservice.dto.WalletOperationResponse;
import cersei.octopusservice.dto.dungeon.DungeonPendingLootDto;
import cersei.octopusservice.dto.dungeon.DungeonRoomNodeDto;
import cersei.octopusservice.dto.dungeon.DungeonRunStateDto;
import cersei.octopusservice.dto.dungeon.DungeonStartFightDto;
import cersei.octopusservice.dto.dungeon.DungeonTemplateDto;
import cersei.octopusservice.dto.fight.*;
import cersei.octopusservice.exception.DungeonNotFoundException;
import cersei.octopusservice.model.*;
import cersei.octopusservice.model.utils.*;
import cersei.octopusservice.repository.*;
import cersei.octopusservice.service.IdempotencyService;
import cersei.octopusservice.service.UserBattleTeamService;
import cersei.octopusservice.service.UserItemService;
import cersei.octopusservice.service.enemy.EnemyCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DungeonService {

    public static final String ACTION_DUNGEON_EXTRACT = "OCTOPUS_DUNGEON_EXTRACT";
    private static final int MAX_TURNS = 50;

    private final DungeonTemplateRepository templateRepository;
    private final DungeonRunRepository runRepository;
    private final DungeonRunRoomRepository roomRepository;
    private final DungeonRunRoomLinkRepository linkRepository;
    private final DungeonRunLootRepository lootRepository;
    private final DungeonMapGenerator mapGenerator;
    private final EnemyCatalogService enemyCatalogService;
    private final UserBattleTeamService userBattleTeamService;
    private final FightCombatantMapper fightCombatantMapper;
    private final FightServiceClient fightServiceClient;
    private final WalletClient walletClient;
    private final UserItemService userItemService;
    private final IdempotencyService idempotencyService;

    @Value("${octopus.fight.base-url}")
    private String fightServiceUrl;

    @Transactional(readOnly = true)
    public List<DungeonTemplateDto> listTemplates() {
        return templateRepository.findAll().stream()
                .map(this::toTemplateDto)
                .toList();
    }

    @Transactional
    public DungeonRunStateDto startRun(String accessToken, UUID userId, int templateId) {
        if (!runRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, DungeonRunStatus.ACTIVE).isEmpty()) {
            throw new IllegalArgumentException("У игрока уже есть активный забег в данже");
        }

        DungeonTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Dungeon template not found: " + templateId));

        UUID runId = UUID.randomUUID();
        debitEntryCost(accessToken, userId, runId, template.getEntryCostMinor());

        DungeonRun run = new DungeonRun();
        run.setId(runId);
        run.setUserId(userId);
        run.setDungeonTemplate(template);
        run.setStatus(DungeonRunStatus.ACTIVE);
        run.setRngSeed(ThreadLocalRandom.current().nextLong());
        run.setCreatedAt(Instant.now());
        run.setUpdatedAt(Instant.now());

        DungeonMapGenerator.GeneratedDungeonMap map = mapGenerator.generate(run, template);
        runRepository.save(run);
        roomRepository.saveAll(map.rooms());
        linkRepository.saveAll(map.links());

        DungeonRunRoom startRoom = map.rooms().stream()
                .filter(r -> r.getLayerIndex() == 0 && r.getSlotIndex() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Start room not generated"));
        run.setCurrentRoomId(startRoom.getId());
        runRepository.save(run);

        log.info("Dungeon run started userId={} runId={} templateId={}", userId, runId, templateId);
        return buildState(run);
    }

    @Transactional(readOnly = true)
    public DungeonRunStateDto getRun(UUID userId, UUID runId) {
        return buildState(requireRun(userId, runId));
    }

    @Transactional
    public DungeonRunStateDto enterRoom(UUID userId, UUID runId, long roomId) {
        DungeonRun run = requireRun(userId, runId);
        ensureActive(run);

        DungeonRunRoom room = requireRoom(runId, roomId);
        if (room.getRoomStatus() != DungeonRoomStatus.AVAILABLE) {
            throw new IllegalArgumentException("Комната недоступна: status=" + room.getRoomStatus());
        }
        if (!isReachable(run, run.getCurrentRoomId(), roomId)) {
            throw new IllegalArgumentException("Комната не связана с текущей позицией");
        }

        run.setCurrentRoomId(roomId);
        run.setUpdatedAt(Instant.now());
        runRepository.save(run);

        if (room.getRoomType() == DungeonRoomType.CHEST) {
            collectRoomLoot(run, room);
            room.setRoomStatus(DungeonRoomStatus.CLEARED);
            roomRepository.save(room);
            unlockLinkedRooms(runId, room);
            log.info("Dungeon chest cleared runId={} roomId={}", runId, roomId);
        } else {
            log.info("Dungeon room entered runId={} roomId={} type={}", runId, roomId, room.getRoomType());
        }

        return buildState(run);
    }

    @Transactional
    public DungeonStartFightDto startFight(String accessToken, UUID userId, UUID runId, long roomId) {
        DungeonRun run = requireRun(userId, runId);
        ensureActive(run);

        DungeonRunRoom room = requireRoom(runId, roomId);
        if (room.getRoomType() != DungeonRoomType.BATTLE && room.getRoomType() != DungeonRoomType.BOSS) {
            throw new IllegalArgumentException("В этой комнате нельзя начать бой: type=" + room.getRoomType());
        }
        if (!Objects.equals(run.getCurrentRoomId(), roomId)) {
            throw new IllegalArgumentException("Сначала нужно войти в комнату");
        }

        if (room.getRoomStatus() == DungeonRoomStatus.IN_FIGHT
                && run.getCurrentFightId() != null
                && !run.getCurrentFightId().isBlank()) {
            return new DungeonStartFightDto(
                    run.getCurrentFightId(),
                    fightServiceUrl,
                    roomId,
                    room.getRoomType().name()
            );
        }
        if (room.getRoomStatus() != DungeonRoomStatus.AVAILABLE) {
            throw new IllegalArgumentException("Комната не готова к бою: status=" + room.getRoomStatus());
        }

        CombatTeamSnapshotDto team = userBattleTeamService.getTeamCombatSnapshots(userId);
        EnemyTemplateDto enemy = enemyCatalogService.requireById(room.getEnemyTemplateId());

        String battleId = UUID.randomUUID().toString();
        FightStartRequest request = new FightStartRequest(
                battleId,
                new FightContextDto(
                        FightSource.DUNGEON,
                        userId,
                        runId,
                        roomId
                ),
                run.getRngSeed(),
                new FightSquadDto(team.fighters().stream()
                        .map(fightCombatantMapper::fromPlayerSnapshot)
                        .toList()),
                new FightSquadDto(List.of(fightCombatantMapper.fromEnemy(enemy))),
                new FightRulesDto(MAX_TURNS, true)
        );

        fightServiceClient.startFight(accessToken, request);

        run.setCurrentFightId(battleId);
        run.setUpdatedAt(Instant.now());
        runRepository.save(run);

        room.setRoomStatus(DungeonRoomStatus.IN_FIGHT);
        roomRepository.save(room);

        log.info("Dungeon fight started runId={} roomId={} battleId={}", runId, roomId, battleId);
        return new DungeonStartFightDto(battleId, fightServiceUrl, roomId, room.getRoomType().name());
    }

    @Transactional
    public DungeonRunStateDto completeFight(String accessToken, UUID userId, UUID runId, String battleId) {
        DungeonRun run = requireRun(userId, runId);
        ensureActive(run);

        if (!Objects.equals(battleId, run.getCurrentFightId())) {
            throw new IllegalArgumentException("battleId не совпадает с текущим боем");
        }

        FightResultResponse result = fightServiceClient.getFightResult(accessToken, battleId);
        if (!result.finished()) {
            log.info("Dungeon fight still in progress runId={} battleId={}", runId, battleId);
            return buildState(run);
        }

        DungeonRunRoom room = requireRoom(runId, run.getCurrentRoomId());
        if (result.result() == BattleResult.WIN) {
            collectRoomLoot(run, room);
            room.setRoomStatus(DungeonRoomStatus.CLEARED);
            roomRepository.save(room);
            unlockLinkedRooms(runId, room);
            run.setCurrentFightId(null);
            run.setUpdatedAt(Instant.now());
            runRepository.save(run);
            log.info("Dungeon fight won runId={} battleId={} roomId={}", runId, battleId, room.getId());
        } else {
            run.setStatus(DungeonRunStatus.FAILED);
            run.setCurrentFightId(null);
            run.setUpdatedAt(Instant.now());
            runRepository.save(run);
            log.info("Dungeon fight lost runId={} battleId={}", runId, battleId);
        }

        return buildState(run);
    }

    @Transactional
    public DungeonRunStateDto extract(String accessToken, UUID userId, UUID runId, String idempotencyKey) {
        return idempotencyService.run(
                userId,
                ACTION_DUNGEON_EXTRACT,
                idempotencyKey,
                DungeonRunStateDto.class,
                () -> doExtract(accessToken, userId, runId)
        );
    }

    private DungeonRunStateDto doExtract(String accessToken, UUID userId, UUID runId) {
        DungeonRun run = requireRun(userId, runId);
        if (run.getStatus() == DungeonRunStatus.EXTRACTED) {
            return buildState(run);
        }

        List<DungeonRunLoot> pending = lootRepository.findByDungeonRun_Id(runId);
        for (DungeonRunLoot entry : pending) {
            grantPendingLoot(accessToken, userId, runId, entry);
        }
        lootRepository.deleteAll(pending);

        run.setStatus(DungeonRunStatus.EXTRACTED);
        run.setCurrentFightId(null);
        run.setUpdatedAt(Instant.now());
        runRepository.save(run);

        log.info("Dungeon extracted runId={} userId={} lootEntries={}", runId, userId, pending.size());
        return buildState(run);
    }

    private void grantPendingLoot(String accessToken, UUID userId, UUID runId, DungeonRunLoot entry) {
        String grantId = runId + ":" + entry.getId();
        if (entry.getItem() != null && entry.getQuantity() > 0) {
            userItemService.addItems(userId, entry.getItem().getId(), entry.getQuantity());
        }
        if (entry.getCoinsMinor() > 0) {
            WalletOperationRequest creditRequest = new WalletOperationRequest(
                    entry.getCoinsMinor(),
                    "OCTOPUS_DUNGEON_LOOT",
                    grantId,
                    "OCTOPUS_DUNGEON",
                    null,
                    grantId
            );
            walletClient.credit(accessToken, creditRequest);
        }
    }

    private void debitEntryCost(String accessToken, UUID userId, UUID runId, long entryCostMinor) {
        if (entryCostMinor <= 0) {
            return;
        }
        WalletOperationRequest debitRequest = new WalletOperationRequest(
                entryCostMinor,
                "OCTOPUS_DUNGEON_ENTRY",
                runId.toString(),
                "OCTOPUS_DUNGEON",
                null,
                runId.toString()
        );
        WalletOperationResponse response = walletClient.debit(accessToken, debitRequest);
        log.info(
                "Dungeon entry paid userId={} runId={} costMinor={} balanceAfter={}",
                userId,
                runId,
                entryCostMinor,
                response.balanceMinorAfter()
        );
    }

    private void collectRoomLoot(DungeonRun run, DungeonRunRoom room) {
        if (room.getLootItem() == null && room.getLootQuantity() <= 0 && room.getLootCoinsMinor() <= 0) {
            return;
        }
        DungeonRunLoot loot = new DungeonRunLoot();
        loot.setDungeonRun(run);
        loot.setItem(room.getLootItem());
        loot.setQuantity(room.getLootQuantity() != null ? room.getLootQuantity() : 0);
        loot.setCoinsMinor(room.getLootCoinsMinor() != null ? room.getLootCoinsMinor() : 0L);
        lootRepository.save(loot);
    }

    private void unlockLinkedRooms(UUID runId, DungeonRunRoom clearedRoom) {
        for (DungeonRunRoomLink link : linkRepository.findByDungeonRunId(runId)) {
            if (!link.getFromRoom().getId().equals(clearedRoom.getId())) {
                continue;
            }
            DungeonRunRoom toRoom = link.getToRoom();
            if (toRoom.getRoomStatus() == DungeonRoomStatus.LOCKED) {
                toRoom.setRoomStatus(DungeonRoomStatus.AVAILABLE);
                roomRepository.save(toRoom);
            }
        }
    }

    private boolean isReachable(DungeonRun run, Long fromRoomId, long toRoomId) {
        if (fromRoomId == null) {
            return toRoomId == findStartRoomId(run.getId());
        }
        if (fromRoomId == toRoomId) {
            return true;
        }
        return linkRepository.findByDungeonRunId(run.getId()).stream()
                .anyMatch(link -> link.getFromRoom().getId().equals(fromRoomId)
                        && link.getToRoom().getId().equals(toRoomId));
    }

    private long findStartRoomId(UUID runId) {
        return roomRepository.findByDungeonRun_IdOrderByLayerIndexAscSlotIndexAsc(runId).stream()
                .filter(r -> r.getLayerIndex() == 0 && r.getSlotIndex() == 0)
                .map(DungeonRunRoom::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Start room not found for run " + runId));
    }

    private DungeonRun requireRun(UUID userId, UUID runId) {
        return runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new DungeonNotFoundException(runId));
    }

    private DungeonRunRoom requireRoom(UUID runId, long roomId) {
        return roomRepository.findByIdAndDungeonRun_Id(roomId, runId)
                .orElseThrow(() -> new IllegalArgumentException("Dungeon room not found: " + roomId));
    }

    private void ensureActive(DungeonRun run) {
        if (run.getStatus() != DungeonRunStatus.ACTIVE) {
            throw new IllegalArgumentException("Забег не активен: status=" + run.getStatus());
        }
    }

    private DungeonRunStateDto buildState(DungeonRun run) {
        UUID runId = run.getId();
        List<DungeonRunRoom> rooms = roomRepository.findByDungeonRun_IdOrderByLayerIndexAscSlotIndexAsc(runId);
        Map<Long, List<Long>> linksByFrom = linkRepository.findByDungeonRunId(runId).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getFromRoom().getId(),
                        Collectors.mapping(link -> link.getToRoom().getId(), Collectors.toList())
                ));

        List<DungeonRoomNodeDto> map = rooms.stream()
                .map(room -> new DungeonRoomNodeDto(
                        room.getId(),
                        room.getLayerIndex(),
                        room.getSlotIndex(),
                        room.getRoomType(),
                        room.getRoomStatus(),
                        room.getEnemyTemplateId(),
                        room.getLootItem() != null ? room.getLootItem().getId() : null,
                        room.getLootQuantity(),
                        room.getLootCoinsMinor(),
                        linksByFrom.getOrDefault(room.getId(), List.of())
                ))
                .toList();

        List<DungeonPendingLootDto> pendingLoot = lootRepository.findByDungeonRun_Id(runId).stream()
                .map(loot -> new DungeonPendingLootDto(
                        loot.getId(),
                        loot.getItem() != null ? loot.getItem().getId() : null,
                        loot.getQuantity(),
                        loot.getCoinsMinor()
                ))
                .toList();

        return new DungeonRunStateDto(
                runId,
                run.getDungeonTemplate().getId(),
                run.getDungeonTemplate().getName(),
                run.getStatus().name(),
                run.getCurrentRoomId(),
                run.getCurrentFightId(),
                map,
                pendingLoot
        );
    }

    private DungeonTemplateDto toTemplateDto(DungeonTemplate template) {
        return new DungeonTemplateDto(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getTier(),
                template.getEntryCostMinor(),
                template.getDepthLayers()
        );
    }
}