package cersei.wallet.kafka;

import cersei.wallet.kafka.dto.WalletCommandMessage;
import cersei.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "wallet.kafka.listener-enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class WalletCommandConsumer {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${wallet.kafka.commands-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCommand(@Payload String payload) {
        try {
            WalletCommandMessage cmd = objectMapper.readValue(payload, WalletCommandMessage.class);
            UUID userId = UUID.fromString(cmd.userId());
            String type = cmd.command() != null ? cmd.command().trim().toUpperCase() : "";
            switch (type) {
                case "CREDIT" -> walletService.credit(
                        userId,
                        cmd.amountMinor(),
                        cmd.referenceType(),
                        cmd.referenceId(),
                        cmd.entryType(),
                        cmd.metadataJson(),
                        cmd.correlationId());
                case "DEBIT" -> walletService.debit(
                        userId,
                        cmd.amountMinor(),
                        cmd.referenceType(),
                        cmd.referenceId(),
                        cmd.entryType(),
                        cmd.metadataJson(),
                        cmd.correlationId());
                default -> log.warn("Unknown wallet command: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to process wallet command: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}