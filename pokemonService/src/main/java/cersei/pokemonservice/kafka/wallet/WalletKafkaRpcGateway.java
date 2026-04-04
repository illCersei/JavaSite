package cersei.pokemonservice.kafka.wallet;

import cersei.pokemonservice.exception.WalletCommandRejectedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletKafkaRpcGateway {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wallet.kafka.commands-topic}")
    private String commandsTopic;

    @Value("${pokemon.instance-id:}")
    private String configuredInstanceId;

    @Value("${wallet.kafka.rpc-timeout-ms:15000}")
    private long rpcTimeoutMs;

    private String instanceId;

    private final ConcurrentHashMap<String, CompletableFuture<WalletKafkaCommandResultDto>> pending =
            new ConcurrentHashMap<>();

    @PostConstruct
    void initInstanceId() {
        instanceId =
                (configuredInstanceId != null && !configuredInstanceId.isBlank())
                        ? configuredInstanceId
                        : UUID.randomUUID().toString();
        log.info("Pokemon service Kafka RPC instanceId={}", instanceId);
    }

    public void handleReplyPayload(String payload) {
        WalletKafkaCommandResultDto result;
        try {
            result = objectMapper.readValue(payload, WalletKafkaCommandResultDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Ignoring invalid wallet reply JSON: {}", e.getMessage());
            return;
        }
        if (result.replyInstanceId() == null || !result.replyInstanceId().equals(instanceId)) {
            return;
        }
        String corr = result.correlationId();
        if (corr == null) {
            return;
        }
        CompletableFuture<WalletKafkaCommandResultDto> f = pending.remove(corr);
        if (f != null) {
            f.complete(result);
        }
    }

    public WalletKafkaCommandResultDto debitAndAwait(
            UUID userId,
            String accessToken,
            long amountMinor,
            String referenceType,
            String referenceId,
            String entryType,
            String metadataJson,
            String correlationId) {
        return sendAndAwait(
                new WalletKafkaCommandDto(
                        "DEBIT",
                        userId.toString(),
                        amountMinor,
                        referenceType,
                        referenceId,
                        entryType,
                        metadataJson,
                        correlationId,
                        instanceId,
                        accessToken),
                correlationId);
    }

    public WalletKafkaCommandResultDto creditAndAwait(
            UUID userId,
            String accessToken,
            long amountMinor,
            String referenceType,
            String referenceId,
            String entryType,
            String metadataJson,
            String correlationId) {
        return sendAndAwait(
                new WalletKafkaCommandDto(
                        "CREDIT",
                        userId.toString(),
                        amountMinor,
                        referenceType,
                        referenceId,
                        entryType,
                        metadataJson,
                        correlationId,
                        instanceId,
                        accessToken),
                correlationId);
    }

    private WalletKafkaCommandResultDto sendAndAwait(WalletKafkaCommandDto cmd, String correlationId) {
        CompletableFuture<WalletKafkaCommandResultDto> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        try {
            String json = objectMapper.writeValueAsString(cmd);
            kafkaTemplate.send(commandsTopic, correlationId, json);
            return future.orTimeout(rpcTimeoutMs, TimeUnit.MILLISECONDS).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                log.warn("Wallet Kafka RPC timeout correlationId={}", correlationId);
                throw new WalletCommandRejectedException("TIMEOUT", "Wallet did not respond in time");
            }
            throw new IllegalStateException("Wallet Kafka RPC failed", e);
        } catch (Exception e) {
            throw new IllegalStateException("Wallet Kafka RPC failed", e);
        } finally {
            pending.remove(correlationId, future);
        }
    }
}
