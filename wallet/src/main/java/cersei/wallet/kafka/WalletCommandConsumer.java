package cersei.wallet.kafka;

import cersei.wallet.exception.InsufficientFundsException;
import cersei.wallet.kafka.dto.WalletCommandMessage;
import cersei.wallet.kafka.dto.WalletCommandResult;
import cersei.wallet.service.WalletService;
import cersei.wallet.service.utils.WalletOperationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "wallet.kafka.listener-enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class WalletCommandConsumer {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JwtDecoder jwtDecoder;

    @Value("${wallet.kafka.replies-topic}")
    private String repliesTopic;

    @KafkaListener(topics = "${wallet.kafka.commands-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCommand(@Payload String payload) {
        WalletCommandMessage cmd;
        try {
            cmd = objectMapper.readValue(payload, WalletCommandMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid wallet command JSON", e);
            return;
        }

        String correlationId = cmd.correlationId() != null ? cmd.correlationId() : "";
        String replyInstanceId = cmd.replyInstanceId();

        try {
            validateCaller(cmd);
            String type = cmd.command() != null ? cmd.command().trim().toUpperCase() : "";
            switch (type) {
                case "CREDIT" -> {
                    UUID userId = UUID.fromString(cmd.userId());
                    WalletOperationResult r = walletService.credit(
                            userId,
                            cmd.amountMinor(),
                            cmd.referenceType(),
                            cmd.referenceId(),
                            cmd.entryType(),
                            cmd.metadataJson(),
                            cmd.correlationId());
                    publishSuccess(correlationId, replyInstanceId, r);
                }
                case "DEBIT" -> {
                    UUID userId = UUID.fromString(cmd.userId());
                    WalletOperationResult r = walletService.debit(
                            userId,
                            cmd.amountMinor(),
                            cmd.referenceType(),
                            cmd.referenceId(),
                            cmd.entryType(),
                            cmd.metadataJson(),
                            cmd.correlationId());
                    publishSuccess(correlationId, replyInstanceId, r);
                }
                default -> {
                    log.warn("Unknown wallet command: {}", type);
                    publishFailure(correlationId, replyInstanceId, "UNKNOWN_COMMAND");
                }
            }
        } catch (InsufficientFundsException e) {
            publishFailure(correlationId, replyInstanceId, "INSUFFICIENT_FUNDS");
        } catch (JwtException e) {
            log.warn("Invalid JWT in wallet command correlationId={}: {}", correlationId, e.getMessage());
            publishFailure(correlationId, replyInstanceId, "INVALID_TOKEN");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Wallet command rejected correlationId={}: {}", correlationId, e.getMessage());
            publishFailure(correlationId, replyInstanceId, "BAD_REQUEST");
        } catch (Exception e) {
            log.error("Wallet command failed correlationId={}", correlationId, e);
            throw e;
        }
    }

    private void validateCaller(WalletCommandMessage cmd) {
        if (cmd.accessToken() == null || cmd.accessToken().isBlank()) {
            throw new IllegalArgumentException("accessToken is required for Kafka wallet commands");
        }
        Jwt jwt = jwtDecoder.decode(cmd.accessToken());
        String tokenUser = jwt.getClaimAsString("uuid");
        if (tokenUser == null || !tokenUser.equalsIgnoreCase(cmd.userId())) {
            throw new IllegalArgumentException("userId does not match access token");
        }
    }

    private void publishSuccess(String correlationId, String replyInstanceId, WalletOperationResult r) {
        try {
            WalletCommandResult result = new WalletCommandResult(
                    correlationId,
                    replyInstanceId,
                    true,
                    r.balanceMinorAfter(),
                    null,
                    r.ledgerEntryId().toString(),
                    r.idempotentReplay());
            String json = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(repliesTopic, correlationId, json);
        } catch (Exception e) {
            log.error("Failed to publish wallet command success reply correlationId={}", correlationId, e);
            throw new IllegalStateException("Cannot publish wallet command reply", e);
        }
    }

    private void publishFailure(String correlationId, String replyInstanceId, String errorCode) {
        try {
            WalletCommandResult result = new WalletCommandResult(
                    correlationId, replyInstanceId, false, null, errorCode, null, false);
            String json = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(repliesTopic, correlationId, json);
        } catch (Exception e) {
            log.error("Failed to publish wallet command failure reply correlationId={}", correlationId, e);
        }
    }
}
