package cersei.pokemonservice.kafka.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletKafkaReplyListener {

    private final WalletKafkaRpcGateway walletKafkaRpcGateway;

    @KafkaListener(topics = "${wallet.kafka.replies-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onWalletReply(@Payload String payload) {
        walletKafkaRpcGateway.handleReplyPayload(payload);
    }
}
