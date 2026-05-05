package cersei.octopusservice.service;

import cersei.octopusservice.config.RedisCacheConfig;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.repository.OctopusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OctopusCatalogService {

    private final OctopusRepository octopusRepository;

    @Value("${octopus.sprite-url-template}")
    private String spriteUrlTemplate;

    @Cacheable(cacheNames = RedisCacheConfig.OCTOPUS_CACHE, key = "#id")
    public OctopusSummaryDto getById(int id) {
        log.debug("OctopusCatalog lookup id={} (source=db)", id);
        Octopus octopus = octopusRepository.findById(id).orElseThrow(() -> new OctopusNotFoundException(id));
        return toSummary(octopus, 1);
    }

    public OctopusSummaryDto toSummary(Octopus octopus, int quantity) {
        return new OctopusSummaryDto(
                octopus.getId(),
                octopus.getName(),
                octopus.getElementType(),
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
