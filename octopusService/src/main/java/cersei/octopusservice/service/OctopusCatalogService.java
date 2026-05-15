package cersei.octopusservice.service;

import cersei.octopusservice.config.RedisCacheConfig;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.repository.OctopusCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OctopusCatalogService {

    private final OctopusCatalogRepository octopusRepository;

    @Value("${octopus.sprite-url-template}")
    private String spriteUrlTemplate;

    @Cacheable(cacheNames = RedisCacheConfig.OCTOPUS_CACHE, key = "#id")
    public OctopusSummaryDto getById(int id) {
        log.info("OctopusCatalog lookup id={} (source=db)", id);
        Octopus octopus = octopusRepository.findById(id).orElseThrow(() -> new OctopusNotFoundException(id));
        return toSummary(octopus, 1);
    }

    @Cacheable(cacheNames = RedisCacheConfig.OCTOPUS_LIST_CACHE, key = "'all'")
    public List<OctopusSummaryDto> getAll(){
        log.info("Getting all octopuses, (source=db)");
        return octopusRepository.findAll().stream()
                .map(it -> toSummary(it, 1))
                .toList();
    }

    public OctopusSummaryDto toSummary(Octopus octopus, int quantity) {
        return new OctopusSummaryDto(
                octopus.getId(),
                octopus.getName(),
                octopus.getElementType() != null ? octopus.getElementType().name() : null,
                octopus.getTier(),
                spriteUrlTemplate.formatted(octopus.getId()),
                octopus.getAttackStat(),
                octopus.getMagicPowerStat(),
                octopus.getArmorStat(),
                octopus.getMagicResistStat(),
                octopus.getSpeedStat(),
                quantity
        );
    }
}
