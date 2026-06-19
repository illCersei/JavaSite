package cersei.octopusservice.service;

import cersei.octopusservice.dto.InventoryLineDto;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.model.UserOctopusStash;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.repository.UserOctopusStashRepository;
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

    private final UserOctopusStashRepository userOctopusStashRepository;
    private final OctopusCatalogService octopusCatalogService;
    private final UserOctopusRepository userOctopusRepository;

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
