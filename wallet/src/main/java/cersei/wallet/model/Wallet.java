package cersei.wallet.model;

import cersei.wallet.model.utils.WalletStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
public class Wallet {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, length = 16)
    private String currency = "GAME";

    @Column(name = "balance_minor", nullable = false)
    private long balanceMinor;

    @Version
    @Column(nullable = false)
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public static Wallet newWallet(UUID userId) {
        Wallet w = new Wallet();
        w.id = UUID.randomUUID();
        w.userId = userId;
        w.balanceMinor = 0;
        return w;
    }
}
