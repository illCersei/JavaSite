package cersei.octopusservice.service;

import cersei.octopusservice.dto.GachaSpinResponse;
import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.dto.WalletOperationRequest;
import cersei.octopusservice.dto.WalletOperationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

@Service
public class GachaService {

    public static final String ACTION_GACHA_SPIN = "OCTOPUS_GACHA_SPIN";

    private final WalletClient walletClient;
    private final OctopusCatalogService octopusCatalogService;
    private final OctopusInventoryService octopusInventoryService;
    private final IdempotencyService idempotencyService;
    private final long costMinor;
    private final int maxOctopusId;

    public GachaService(
            WalletClient walletClient,
            OctopusCatalogService octopusCatalogService,
            OctopusInventoryService octopusInventoryService,
            IdempotencyService idempotencyService,
            @Value("${octopus.gacha.cost-minor}") long costMinor,
            @Value("${octopus.gacha.max-template-id}") int maxTemplateId
    ) {
        this.walletClient = walletClient;
        this.octopusCatalogService = octopusCatalogService;
        this.octopusInventoryService = octopusInventoryService;
        this.idempotencyService = idempotencyService;
        this.costMinor = costMinor;
        this.maxOctopusId = maxTemplateId;
    }

    public GachaSpinResponse spin(String accessToken, UUID userId, String idempotencyKey) {
        return idempotencyService.run(
                userId,
                ACTION_GACHA_SPIN,
                idempotencyKey,
                GachaSpinResponse.class,
                () -> doSpin(accessToken, userId, idempotencyKey)
        );
    }

    private GachaSpinResponse doSpin(String accessToken, UUID userId, String spinId) {
        WalletOperationRequest debitRequest = new WalletOperationRequest(
                costMinor,
                "OCTOPUS_GACHA_SPIN",
                spinId,
                "OCTOPUS_GACHA",
                null,
                spinId
        );
        WalletOperationResponse walletResult = walletClient.debit(accessToken, debitRequest);

        int octopusId = ThreadLocalRandom.current().nextInt(1, maxOctopusId + 1);
        try {
            OctopusSummaryDto base = octopusCatalogService.getById(octopusId);
            int ownedQty = octopusInventoryService.addOne(userId, octopusId);
            OctopusSummaryDto octopus = new OctopusSummaryDto(
                    base.id(),
                    base.name(),
                    base.elementType(),
                    base.tier(),
                    base.imageUrl(),
                    base.attack(),
                    base.magicPower(),
                    base.armor(),
                    base.magicResist(),
                    base.speed(),
                    ownedQty
            );
            long balanceAfter = walletResult.balanceMinorAfter() != null ? walletResult.balanceMinorAfter() : 0L;
            return new GachaSpinResponse(spinId, octopus, balanceAfter, walletResult.idempotentReplay());
        } catch (RuntimeException ex) {
            WalletOperationRequest refundRequest = new WalletOperationRequest(
                    costMinor,
                    "OCTOPUS_GACHA_REFUND",
                    spinId,
                    "OCTOPUS_GACHA_REFUND",
                    "{\"reason\":\"OCTOPUS_GACHA_FAILURE\"}",
                    spinId
            );
            walletClient.creditQuietly(accessToken, refundRequest);
            throw ex;
        }
    }
}
