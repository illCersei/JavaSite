package cersei.auth.messaging;

import cersei.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "user-registration";

    public void sendUserRegistrationEvent(User user) {
        String message = user.getUserId().toString();
        kafkaTemplate.send(TOPIC, message);
    }
}