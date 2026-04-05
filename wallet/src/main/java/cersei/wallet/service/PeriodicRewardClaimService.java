package cersei.wallet.service;

import cersei.wallet.config.WalletPeriodicRewardProperties;
import cersei.wallet.dto.PeriodicRewardClaimResponse;
import cersei.wallet.exception.FeatureDisabledException;
import cersei.wallet.service.utils.WalletBalanceView;
import cersei.wallet.service.utils.WalletOperationResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PeriodicRewardClaimService {

    private final WalletPeriodicRewardProperties properties;
    private final WalletService walletService;

    @PostConstruct
    void validateConfig() {
        if (!properties.isEnabled()) {
            return;
        }
        if (properties.getAmountMinor() <= 0) {
            throw new IllegalStateException("wallet.periodic-reward.amount-minor must be positive");
        }
        if (properties.getStrategy() == WalletPeriodicRewardProperties.Strategy.CALENDAR_DAY
                && properties.getPeriodHours() != 24) {
            throw new IllegalStateException(
                    "wallet.periodic-reward.period-hours must be 24 when strategy is CALENDAR_DAY");
        }
        if (properties.getStrategy() == WalletPeriodicRewardProperties.Strategy.FIXED_WINDOW) {
            long sec = periodSeconds();
            if (sec <= 0) {
                throw new IllegalStateException(
                        "For FIXED_WINDOW set wallet.periodic-reward.period-minutes > 0 or period-hours > 0");
            }
        }
        ZoneId.of(properties.getZoneId());
    }

    @Transactional
    public PeriodicRewardClaimResponse claim(UUID userId) {
        if (!properties.isEnabled()) {
            throw new FeatureDisabledException("Periodic reward is disabled");
        }

        ZoneId zone = ZoneId.of(properties.getZoneId());
        Instant windowStart;
        Instant nextClaimAt;
        String referenceId;

        if (properties.getStrategy() == WalletPeriodicRewardProperties.Strategy.CALENDAR_DAY) {
            LocalDate today = LocalDate.now(zone);
            referenceId = userId + ":" + today;
            ZonedDateTime startZ = today.atStartOfDay(zone);
            windowStart = startZ.toInstant();
            nextClaimAt = today.plusDays(1).atStartOfDay(zone).toInstant();
        } else {
            long periodSec = periodSeconds();
            long nowSec = Instant.now().getEpochSecond();
            long windowEpoch = (nowSec / periodSec) * periodSec;
            windowStart = Instant.ofEpochSecond(windowEpoch);
            nextClaimAt = Instant.ofEpochSecond(windowEpoch + periodSec);
            referenceId = userId + ":" + windowEpoch;
        }

        WalletOperationResult result =
                walletService.credit(
                        userId,
                        properties.getAmountMinor(),
                        properties.getReferenceType(),
                        referenceId,
                        properties.getEntryType(),
                        null,
                        referenceId);

        WalletBalanceView balance = walletService.getBalance(userId);

        boolean replay = result.idempotentReplay();
        return new PeriodicRewardClaimResponse(
                !replay,
                replay,
                properties.getAmountMinor(),
                result.balanceMinorAfter(),
                balance.currency(),
                windowStart,
                nextClaimAt);
    }

    private long periodSeconds() {
        if (properties.getPeriodMinutes() > 0) {
            return properties.getPeriodMinutes() * 60L;
        }
        return properties.getPeriodHours() * 3600L;
    }
}
