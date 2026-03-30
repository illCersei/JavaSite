package cersei.wallet.repository;

import cersei.wallet.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    Optional<LedgerEntry> findByWallet_IdAndReferenceTypeAndReferenceId(
            UUID walletId, String referenceType, String referenceId);
}
