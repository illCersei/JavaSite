package cersei.octopusservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class FightClientConfig {

    @Bean
    public RestClient fightRestClient(@Value("${octopus.fight.base-url}") String baseUrl) {
        // See WalletClientConfig - java.net.http.HttpClient can't handle underscored
        // hostnames like docker-compose's "fight-service" ever contained "_".
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
}