package cersei.octopusservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WalletClientConfig {

    @Bean
    public RestClient walletRestClient(@Value("${wallet.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl + "/api/v1/wallet").build();
    }
}
