package cersei.octopusservice.service;

import cersei.octopusservice.dto.InventoryLineDto;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.UserOctopusStash;
import cersei.octopusservice.repository.OctopusCatalogRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.repository.UserOctopusStashRepository;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOctopusStashService {

    public static final String ACTION_SUMMON = "OCTOPUS_SUMMON";

    private final UserOctopusStashRepository userOctopusStashRepository;
    private final OctopusCatalogService octopusCatalogService;
    private final OctopusCatalogRepository octopusCatalogRepository;
    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusDtoAssembler userOctopusDtoAssembler;
    private final IdempotencyService idempotencyService;

    @Transactional
    public int addOne(UUID userId, int octopusId) {
        UserOctopusStash row = userOctopusStashRepository
                .findByUserIdAndOctopusId(userId, octopusId)
                .orElseGet(() -> new UserOctopusStash(userId, octopusId, 0, Instant.now()));
        row.setQuantity(row.getQuantity() + 1);
        row.setUpdatedAt(Instant.now());
        userOctopusStashRepository.save(row);
        log.debug("OctopusInventory addOne userId={} octopusId={} quantity={}", userId, octopusId, row.getQuantity());
        return row.getQuantity();
    }

    // Gacha only ever adds to the stash (a raw duplicate counter) - it never creates a
    // playable UserOctopus instance by itself. This is the step that actually turns 1 stash
    // copy into an instance with its own level/tier/skills/equipment, so it can be equipped
    // and put on a battle team.
    public UserOctopusDto summon(UUID userId, int octopusId, String idempotencyKey) {
        return idempotencyService.run(
                userId,
                ACTION_SUMMON,
                idempotencyKey,
                UserOctopusDto.class,
                () -> doSummon(userId, octopusId)
        );
    }

    // No @Transactional here: this is only ever invoked as a self-call from summon()'s lambda,
    // which already runs inside the transaction IdempotencyService.run() opened - a proxy-based
    // @Transactional on this method would be silently ignored anyway (self-invocation bypasses
    // Spring AOP), so it just joins the already-open transaction implicitly.
    private UserOctopusDto doSummon(UUID userId, int octopusId) {
        UserOctopusStash stash = userOctopusStashRepository
                .findByUserIdAndOctopusId(userId, octopusId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "В стеше нет осьминога id=" + octopusId));
        if (stash.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Недостаточно копий в стеше для создания инстанса: id=" + octopusId);
        }
        Octopus template = octopusCatalogRepository.findById(octopusId)
                .orElseThrow(() -> new OctopusNotFoundException(octopusId));

        stash.setQuantity(stash.getQuantity() - 1);
        stash.setUpdatedAt(Instant.now());
        userOctopusStashRepository.save(stash);

        UserOctopus instance = new UserOctopus();
        instance.setUserId(userId);
        instance.setOctopus(template);
        instance.setCurrentAttackStat(template.getAttackStat());
        instance.setCurrentMagicPowerStat(template.getMagicPowerStat());
        instance.setCurrentArmorStat(template.getArmorStat());
        instance.setCurrentMagicResistStat(template.getMagicResistStat());
        instance.setCurrentSpeedStat(template.getSpeedStat());
        instance.setCurrentFreeSkillPoints(template.getFreeSkillPoints());
        userOctopusRepository.save(instance);

        log.info("Octopus summoned userId={} octopusId={} userOctopusId={} stashLeft={}",
                userId, octopusId, instance.getId(), stash.getQuantity());
        return userOctopusDtoAssembler.toDto(instance);
    }

    @Transactional(readOnly = true)
    public List<InventoryLineDto> listWithDetails(UUID userId) {
        return userOctopusStashRepository.findByUserIdOrderByOctopusIdAsc(userId).stream()
                .map(this::toLine)
                .toList();
    }

    private InventoryLineDto toLine(UserOctopusStash stash) {
        OctopusSummaryDto base = octopusCatalogService.getById(stash.getOctopusId());
        long instances = userOctopusRepository.countByUserIdAndOctopus_Id(stash.getUserId(), stash.getOctopusId());
        int totalQty = stash.getQuantity() + Math.toIntExact(instances);
        OctopusSummaryDto withQty = new OctopusSummaryDto(
                base.id(),
                base.name(),
                base.elementType(),
                base.tier(),
                base.imageUrl(),
                base.attack(),
                base.magicPower(),
                base.armor(),
                base.magicResist(),
                base.speed(),
                totalQty
        );
        return new InventoryLineDto(withQty);
    }
}
