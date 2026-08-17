package cersei.octopusservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "octopus.security")
public class OctopusSecurityProperties {

    /** Роли из JWT claim {@code role}, без учёта регистра, которым разрешены GM-эндпоинты. */
    private String privilegedRoles = "ADMIN";

    public Set<String> privilegedRolesSet() {
        return Arrays.stream(privilegedRoles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
