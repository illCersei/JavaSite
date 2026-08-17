package cersei.octopusservice.config;

import cersei.octopusservice.service.loot.LootTierWeightProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LootTierWeightProperties.class)
public class LootConfig {
}
