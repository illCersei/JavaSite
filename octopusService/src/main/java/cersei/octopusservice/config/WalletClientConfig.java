package cersei.octopusservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class WalletClientConfig {

    @Bean
    public RestClient walletRestClient(@Value("${wallet.base-url}") String baseUrl) {
        // java.net.http.HttpClient (RestClient's default request factory) rejects
        // hostnames containing "_" (java.net.URI.getHost() returns null for them -
        // this repo's docker-compose service names are hyphenated for exactly this
        // reason), throwing "unsupported URI". The older HttpURLConnection-backed
        // factory doesn't have this restriction; kept as defense in depth.
        return RestClient.builder()
                .baseUrl(baseUrl + "/api/v1/wallet")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
}
