package cersei.pokemonservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AppClientsConfig {

    /**
     * {@link java.net.http.HttpClient} (JDK) отклоняет хосты с подчёркиванием в имени (например {@code wallet_service}),
     * а {@link SimpleClientHttpRequestFactory} с этим справляется. Для PokeAPI то же самое — меньше сюрпризов с URI.
     */
    @Bean
    public RestClient pokeApiRestClient(
            @Value("${pokeapi.base-url}") String baseUrl,
            @Value("${pokeapi.http.timeout-seconds:20}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int ms = (int) Math.min(Integer.MAX_VALUE, Duration.ofSeconds(timeoutSeconds).toMillis());
        factory.setConnectTimeout(ms);
        factory.setReadTimeout(ms);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
