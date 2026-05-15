package cersei.octopusservice.config;

import cersei.octopusservice.dto.OctopusSummaryDto;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

@Configuration
public class RedisCacheConfig {

    public static final String OCTOPUS_CACHE = "octopusCatalogV1";
    public static final String OCTOPUS_LIST_CACHE = "octopusListCatalogV1";


    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<OctopusSummaryDto> octopusSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, OctopusSummaryDto.class);

        JavaType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, OctopusSummaryDto.class);

        Jackson2JsonRedisSerializer<List<OctopusSummaryDto>> octopusListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, listType);

        RedisCacheConfiguration octopusConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(octopusSerializer)
                )
                .entryTtl(Duration.ofDays(7));

        RedisCacheConfiguration octopusListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(octopusListSerializer)
                )
                .entryTtl(Duration.ofDays(7));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(octopusConfig)
                .withCacheConfiguration(OCTOPUS_CACHE, octopusConfig)
                .withCacheConfiguration(OCTOPUS_LIST_CACHE, octopusListConfig)
                .build();
    }
}
