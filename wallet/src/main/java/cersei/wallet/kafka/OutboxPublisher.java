package cersei.wallet.kafka;

import cersei.wallet.model.OutboxEvent;
import cersei.wallet.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "wallet.outbox.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${wallet.outbox.publish-interval-ms}")
    public void publishPending() {
        List<OutboxEvent> batch = outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEvent ev : batch) {
            try {
                kafkaTemplate
                        .send(ev.getTopic(), ev.getUserId().toString(), ev.getPayloadJson())
                        .get(10, TimeUnit.SECONDS);
                ev.setPublishedAt(Instant.now());
                outboxEventRepository.save(ev);
            } catch (Exception e) {
                log.warn("Outbox publish failed for {}: {}", ev.getId(), e.getMessage());
            }
        }
    }
}