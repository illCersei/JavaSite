package cersei.testapi.messaging;

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
public class LoginListener {

    /**
     * Функция Листенера логина
     * @param message String - статус логина
     */
    @RabbitListener(queues = "test")
    public void receiveLogin(String message) {
        System.out.println(message);
    }
}
