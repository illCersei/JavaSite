package cersei.octopusservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The octopus catalog cache has a 7-day TTL and nothing else ever evicts it (see
 * RedisCacheConfig) - it's Flyway-seeded reference data, so it only actually changes when a
 * seed migration is edited and the service is redeployed. Without this, a redeploy with updated
 * catalog content (e.g. a text/translation fix) keeps serving the previous version's cached
 * responses out of Redis for up to a week, which looks exactly like the change didn't take.
 * Clearing on startup means every restart picks up whatever's currently in the DB.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OctopusCatalogCacheEvictor {

    private final CacheManager cacheManager;

    @EventListener(ApplicationReadyEvent.class)
    public void evictOnStartup() {
        evict(RedisCacheConfig.OCTOPUS_CACHE);
        evict(RedisCacheConfig.OCTOPUS_LIST_CACHE);
    }

    private void evict(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cleared cache={} on startup", cacheName);
        }
    }
}
