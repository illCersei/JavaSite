package cersei.testapi.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Cersei
 * @since 11.12.2025
 * <p>
 *     Листенер rabbit'a, при попытке логина - выводит в консоль статус логина + никнейм
 * </p>
 */
@Component
@Slf4j
public class LoginListener {

    @RabbitListener(queues = "test")
    public void receiveLogin(String message) {
        log.info(message);
    }
}
