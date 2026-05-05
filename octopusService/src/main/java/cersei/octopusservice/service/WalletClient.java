package cersei.octopusservice.service;

import cersei.octopusservice.dto.WalletOperationRequest;
import cersei.octopusservice.dto.WalletOperationResponse;
import cersei.octopusservice.exception.WalletOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class WalletClient {

    private final RestClient walletRestClient;

    public WalletOperationResponse debit(String accessToken, WalletOperationRequest request) {
        try {
            WalletOperationResponse response = walletRestClient.post()
                    .uri("/private/me/game/debits")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(WalletOperationResponse.class);
            if (response == null) {
                throw new WalletOperationException("Wallet debit returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new WalletOperationException("Wallet debit failed: HTTP " + ex.getStatusCode().value());
        }
    }

    public void creditQuietly(String accessToken, WalletOperationRequest request) {
        try {
            walletRestClient.post()
                    .uri("/private/me/game/credits")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {

        }
    }
}
