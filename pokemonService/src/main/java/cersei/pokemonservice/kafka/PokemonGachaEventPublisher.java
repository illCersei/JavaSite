package cersei.pokemonservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokemonGachaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pokemon.kafka.gacha-events-topic}")
    private String topic;

    public void publish(PokemonGachaEvent event) {
        try {
            kafkaTemplate.send(topic, event.userId(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize pokemon gacha event spinId={}", event.spinId(), e);
        }
    }
}
