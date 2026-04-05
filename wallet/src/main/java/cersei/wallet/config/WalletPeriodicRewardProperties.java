package cersei.wallet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "wallet.periodic-reward")
public class WalletPeriodicRewardProperties {

    private boolean enabled = true;

    /**
     * Сколько минорных единиц начислять за одно успешное получение.
     * Фактическое значение задаётся в {@code application.yml} / env {@code WALLET_PERIODIC_REWARD_AMOUNT}.
     */
    private long amountMinor = 1000L;

    /**
     * Для {@link Strategy#FIXED_WINDOW} — длина окна в часах, если {@link #periodMinutes} = 0.
     * Для {@link Strategy#CALENDAR_DAY} должно быть 24 (проверяется при старте).
     */
    private int periodHours = 0;

    /**
     * Для {@link Strategy#FIXED_WINDOW}: если {@code > 0}, период в минутах (приоритет над {@link #periodHours}).
     * Пример: {@code 15} — награда не чаще чем раз в 15 минут (окна от эпохи).
     */
    private int periodMinutes = 10;

    private Strategy strategy = Strategy.FIXED_WINDOW;

    /** Часовой пояс для {@link Strategy#CALENDAR_DAY} (например Europe/Moscow, UTC). */
    private String zoneId = "UTC";

    private String referenceType = "PERIODIC_REWARD";

    private String entryType = "PERIODIC_REWARD";

    public enum Strategy {
        /** Один раз за локальные сутки по {@link #zoneId}. */
        CALENDAR_DAY,
        /** Окна фиксированной длины от Unix-эпохи: floor(epoch / period) * period. */
        FIXED_WINDOW
    }
}
