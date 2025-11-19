package cersei.twitchservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Twitch Service API")
                        .version("1.0")
                        .description("API просмотра собранных данных о играх и их просмотрах с сайта twitch.tv"));
    }
}
