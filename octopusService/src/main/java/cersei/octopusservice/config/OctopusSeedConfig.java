package cersei.octopusservice.config;

import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.repository.OctopusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class OctopusSeedConfig {

    private final OctopusRepository octopusRepository;

    @Bean
    CommandLineRunner seedOctopusCatalog() {
        return args -> {
            if (octopusRepository.count() > 0) {
                return;
            }
            octopusRepository.saveAll(List.of(
                    build(1, "Toxic Inkling", "POISON", 1, 18, 10, 8, 7, 12),
                    build(2, "Frozen Pearl", "FROST", 1, 12, 18, 7, 10, 11),
                    build(3, "Ember Tentacle", "FLAME", 1, 20, 9, 7, 7, 13),
                    build(4, "Storm Current", "STORM", 1, 14, 16, 8, 8, 15),
                    build(5, "Abyss Watcher", "ABYSS", 1, 16, 14, 10, 11, 10),
                    build(6, "Tidal Guard", "TIDE", 1, 12, 12, 14, 13, 9)
            ));
        };
    }

    private Octopus build(
            int id,
            String name,
            String type,
            int tier,
            int atk,
            int magic,
            int armor,
            int mr,
            int speed
    ) {
        Octopus o = new Octopus();
        o.setId(id);
        o.setName(name);
        o.setElementType(type);
        o.setTier(tier);
        o.setAttackStat(atk);
        o.setMagicPowerStat(magic);
        o.setArmorStat(armor);
        o.setMagicResistStat(mr);
        o.setSpeedStat(speed);
        return o;
    }
}
