package cersei.wallet.dto;

import java.time.Instant;

/**
 * Ответ на безопасное периодическое начисление (без суммы в теле запроса).
 *
 * @param grantedThisCall true, если в этом ответе только что начислили монеты
 * @param alreadyClaimedThisPeriod true, если в текущем периоде награда уже была (идемпотентный повтор)
 */
public record PeriodicRewardClaimResponse(
        boolean grantedThisCall,
        boolean alreadyClaimedThisPeriod,
        long amountMinor,
        long balanceMinorAfter,
        String currency,
        Instant periodWindowStart,
        Instant nextClaimAvailableAt) {}
