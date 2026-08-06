package cersei.octopusservice.client;

import cersei.octopusservice.dto.WalletOperationRequest;
import cersei.octopusservice.dto.WalletOperationResponse;
import cersei.octopusservice.exception.WalletOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class WalletClient {

    private static final String SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";

    private final RestClient walletRestClient;

    @Value("${wallet.service-token}")
    private String serviceToken;

    public WalletOperationResponse debit(String accessToken, WalletOperationRequest request) {
        try {
            WalletOperationResponse response = walletRestClient.post()
                    .uri("/private/me/game/debits")
                    .header("Authorization", "Bearer " + accessToken)
                    .header(SERVICE_TOKEN_HEADER, serviceToken)
                    .body(request)
                    .retrieve()
                    .body(WalletOperationResponse.class);
            if (response == null) {
                throw new WalletOperationException("Wallet debit returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            throw new WalletOperationException(status, "Wallet debit failed: HTTP " + status);
        }
    }

    public WalletOperationResponse credit(String accessToken, WalletOperationRequest request) {
        try {
            WalletOperationResponse response = walletRestClient.post()
                    .uri("/private/me/game/credits")
                    .header("Authorization", "Bearer " + accessToken)
                    .header(SERVICE_TOKEN_HEADER, serviceToken)
                    .body(request)
                    .retrieve()
                    .body(WalletOperationResponse.class);
            if (response == null) {
                throw new WalletOperationException("Wallet credit returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            throw new WalletOperationException(status, "Wallet credit failed: HTTP " + status);
        }
    }

    public void creditQuietly(String accessToken, WalletOperationRequest request) {
        try {
            walletRestClient.post()
                    .uri("/private/me/game/credits")
                    .header("Authorization", "Bearer " + accessToken)
                    .header(SERVICE_TOKEN_HEADER, serviceToken)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {

        }
    }
}
