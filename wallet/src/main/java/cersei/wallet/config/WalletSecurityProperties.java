package cersei.wallet.config;

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
@ConfigurationProperties(prefix = "wallet.security")
public class WalletSecurityProperties {

    /**
     * Если false — {@code POST /private/me/credits} и {@code /debits} доступны только ролям из {@link #privilegedRoles}.
     * Начисление «по кнопке» для игроков — через {@code /rewards/claim}.
     */
    private boolean allowArbitraryCreditDebit = false;

    /** Роли из JWT claim {@code role} (как в auth), без учёта регистра. */
    private String privilegedRoles = "ADMIN";

    public Set<String> privilegedRolesSet() {
        return Arrays.stream(privilegedRoles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
