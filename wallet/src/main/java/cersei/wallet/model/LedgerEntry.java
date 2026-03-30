package cersei.wallet.model;

import cersei.wallet.model.utils.LedgerDirection;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;


/**
 * Журнал проводок: каждое начисление/списание - отдельная строка с типом операции,
 * направлением (CREDIT / DEBIT), суммой, балансом после операции,
 * внешней ссылкой reference_type + reference_id (идемпотентность).
 */
@Entity
@Table(name = "ledger_entries")
@Data
@NoArgsConstructor
public class LedgerEntry {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "entry_type", nullable = false, length = 64)
    private String entryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LedgerDirection direction;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "balance_after_minor", nullable = false)
    private long balanceAfterMinor;

    @Column(name = "reference_type", nullable = false, length = 64)
    private String referenceType;

    @Column(name = "reference_id", nullable = false, length = 256)
    private String referenceId;

    @Column(name = "metadata_json")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
