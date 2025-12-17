package cersei.auth.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//TODO: Нужно перенести это в общие ресурсы
@Configuration
public class RabbitConfig {
    @Bean
    public Queue testQueue() {
        return new Queue("test", true);
    }
}
