package cersei.auth.messaging;

import cersei.auth.dto.UserLoginDto;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

/**
 * @author Cersei
 * @since 11.12.2025
 * <p>
 * Класс брокера сообщений
 * Особого смысла пока нет, он просто отправляет сообщений об успешном/неуспешном логина + никнейм
 * </p>
 */
@Service
@AllArgsConstructor
public class RabbitAuthMessagingService implements TestMessaging{
    private final RabbitTemplate rabbitTemplate;

    /**
     * Отпраляет в очередь сообщение об успешном логине
     * @param successMessage String, успешного логина
     */
    @Override
    public void successLogin(String successMessage) {
        MessageConverter converter = new Jackson2JsonMessageConverter();
        MessageProperties messageProperties = new MessageProperties();
        Message message = converter.toMessage(successMessage, messageProperties);
        rabbitTemplate.send("test", message);
    }

    /**
     * Отправляет в очередь сообщений о неуспешном логине
     * @param failureMessage String, неуспешного логина
     */
    @Override
    public void failureLogin(String failureMessage) {
        rabbitTemplate.convertAndSend("test", failureMessage);
    }
}
