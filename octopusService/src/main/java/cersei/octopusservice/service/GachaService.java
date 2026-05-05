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

    private final WalletClient walletClient;
    private final OctopusCatalogService octopusCatalogService;
    private final OctopusInventoryService octopusInventoryService;
    private final long costMinor;
    private final int maxOctopusId;

    public GachaService(
            WalletClient walletClient,
            OctopusCatalogService octopusCatalogService,
            OctopusInventoryService octopusInventoryService,
            @Value("${octopus.gacha.cost-minor}") long costMinor,
            @Value("${octopus.gacha.max-template-id}") int maxTemplateId
    ) {
        this.walletClient = walletClient;
        this.octopusCatalogService = octopusCatalogService;
        this.octopusInventoryService = octopusInventoryService;
        this.costMinor = costMinor;
        this.maxOctopusId = maxTemplateId;
    }

    public GachaSpinResponse spin(String accessToken, UUID userId) {
        String spinId = UUID.randomUUID().toString();
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
