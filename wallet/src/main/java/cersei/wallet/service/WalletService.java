package cersei.wallet.service;

import cersei.wallet.exception.InsufficientFundsException;
import cersei.wallet.kafka.dto.WalletBalanceChangedEvent;
import cersei.wallet.model.LedgerEntry;
import cersei.wallet.model.OutboxEvent;
import cersei.wallet.model.Wallet;
import cersei.wallet.model.utils.LedgerDirection;
import cersei.wallet.model.utils.WalletStatus;
import cersei.wallet.repository.LedgerEntryRepository;
import cersei.wallet.repository.OutboxEventRepository;
import cersei.wallet.repository.WalletRepository;
import cersei.wallet.service.utils.WalletBalanceView;
import cersei.wallet.service.utils.WalletOperationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${wallet.kafka.events-topic}")
    private String eventsTopic;

    /**
     * Получить баланс кошелька пользователя. Если кошелька нет, возвращается баланс 0 с несуществующим walletId.
     * @param userId - идентификатор пользователя
     * @return - представление баланса кошелька. Если кошелька нет, walletId будет null, а balanceMinor 0.
     */
    @Transactional(readOnly = true)
    public WalletBalanceView getBalance(UUID userId) {
        return walletRepository
                .findByUserId(userId)
                .map(w -> new WalletBalanceView(w.getId(), w.getUserId(), w.getBalanceMinor(), w.getCurrency()))
                .orElseGet(() -> new WalletBalanceView(null, userId, 0L, "GAME"));
    }

    /**
     * Начисление средств на кошелек. Если кошелька нет, он будет создан.
     * Идентичные запросы (одинаковый userId + referenceType + referenceId) должны быть идемпотентными - повторные запросы не должны создавать новые записи в журнал
     * @param userId - идентификатор пользователя
     * @param amountMinor - сумма в единицах
     * @param referenceType - тип операции для идемпотентности (например, "PURCHASE", "REWARD", "REFUND" и т.п.)
     * @param referenceId - идентификатор операции для идемпотентности (например, идентификатор покупки, награды, возврата и т.п.)
     * @param entryType - произвольная строка для классификации записи в журнале
     * @param metadataJson - произвольный JSON для сохранения в записи журнала.
     * @param correlationId - произвольная строка для корреляции событий.
     * @return - результат операции с информацией о новом балансе и идентификаторе записи в журнале.
     * Если запрос был повтором, возвращается флаг idempotentReplay=true.
     */
    @Transactional
    public WalletOperationResult credit(
            UUID userId,
            long amountMinor,
            String referenceType,
            String referenceId,
            String entryType,
            String metadataJson,
            String correlationId) {
        validateAmount(amountMinor);
        Wallet wallet =
                walletRepository.findByUserIdForUpdate(userId).orElseGet(() -> createWalletFirstCredit(userId));
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }

        Optional<LedgerEntry> existing = ledgerEntryRepository.findByWallet_IdAndReferenceTypeAndReferenceId(
                wallet.getId(), referenceType, referenceId);
        if (existing.isPresent()) {
            LedgerEntry le = existing.get();
            return WalletOperationResult.builder()
                    .ledgerEntryId(le.getId())
                    .balanceMinorAfter(wallet.getBalanceMinor())
                    .idempotentReplay(true)
                    .build();
        }

        try {
            return applyCredit(wallet, amountMinor, referenceType, referenceId, entryType, metadataJson, correlationId);
        } catch (DataIntegrityViolationException e) {
            return handleRaceCredit(userId, referenceType, referenceId);
        }
    }

    /**
     * Списание средств с кошелька. Если кошелька нет, операция считается неуспешной - InsufficientFundsException
     * @param userId - идентификатор пользователя
     * @param amountMinor - сумма в единицах
     * @param referenceType - тип операции для идемпотентности (например, "PURCHASE", "REWARD", "REFUND" и т.п.)
     * @param referenceId - идентификатор операции для идемпотентности (например, идентификатор покупки, награды, возврата и т.п.)
     * @param entryType - произвольная строка для классификации записи в журнале
     * @param metadataJson - произвольный JSON для сохранения в записи журнала.
     * @param correlationId - произвольная строка для корреляции событий.
     * @return - результат операции с информацией о новом балансе и идентификаторе записи в журнале.
     * Если запрос был повтором, возвращается флаг idempotentReplay=true.
     */
    @Transactional
    public WalletOperationResult debit(
            UUID userId,
            long amountMinor,
            String referenceType,
            String referenceId,
            String entryType,
            String metadataJson,
            String correlationId) {
        validateAmount(amountMinor);
        Wallet wallet = walletRepository
                .findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }

        Optional<LedgerEntry> existing = ledgerEntryRepository.findByWallet_IdAndReferenceTypeAndReferenceId(
                wallet.getId(), referenceType, referenceId);
        if (existing.isPresent()) {
            LedgerEntry le = existing.get();
            return WalletOperationResult.builder()
                    .ledgerEntryId(le.getId())
                    .balanceMinorAfter(wallet.getBalanceMinor())
                    .idempotentReplay(true)
                    .build();
        }

        if (wallet.getBalanceMinor() < amountMinor) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        try {
            return applyDebit(wallet, amountMinor, referenceType, referenceId, entryType, metadataJson, correlationId);
        } catch (DataIntegrityViolationException e) {
            return handleRaceDebit(userId, referenceType, referenceId);
        }
    }

    /**
     * Обработка ситуации гонки при которой два запроса на кредит или дебет с одинаковой комбинацией userId + referenceType + referenceId
     * проходят проверку на существование записи в журнале, не видят друг друга и оба пытаются создать запись.
     * Один из них выигрывает гонку и успешно создает запись,
     * второй получает исключение DataIntegrityViolationException
     * при попытке сохранить запись из-за нарушения уникального ограничения.
     * В этом случае мы ловим исключение и извлекаем результат операции из уже созданной записи в журнале
     * @param userId - идентификатор пользователя
     * @param referenceType - тип операции для идемпотентности (например, "PURCHASE", "REWARD", "REFUND" и т.п.)
     * @param referenceId - идентификатор операции для идемпотентности (например, идентификатор покупки, награды, возврата и т.п.)
     * @return - результат операции с информацией о новом балансе и идентификаторе записи в журнале.
     */
    private WalletOperationResult handleRaceCredit(UUID userId, String referenceType, String referenceId) {
        Wallet w = walletRepository.findByUserId(userId).orElseThrow();
        LedgerEntry le = ledgerEntryRepository
                .findByWallet_IdAndReferenceTypeAndReferenceId(w.getId(), referenceType, referenceId)
                .orElseThrow(() -> new IllegalStateException("Concurrent operation failed"));
        return WalletOperationResult.builder()
                .ledgerEntryId(le.getId())
                .balanceMinorAfter(w.getBalanceMinor())
                .idempotentReplay(true)
                .build();
    }

    /**
     * @see "handleRaceCredit"
     */
    private WalletOperationResult handleRaceDebit(UUID userId, String referenceType, String referenceId) {
        Wallet w = walletRepository.findByUserId(userId).orElseThrow();
        LedgerEntry le = ledgerEntryRepository
                .findByWallet_IdAndReferenceTypeAndReferenceId(w.getId(), referenceType, referenceId)
                .orElseThrow(() -> new IllegalStateException("Concurrent operation failed"));
        return WalletOperationResult.builder()
                .ledgerEntryId(le.getId())
                .balanceMinorAfter(w.getBalanceMinor())
                .idempotentReplay(true)
                .build();
    }

    /**
     * Применить кредит к кошельку. Метод предполагает, что все проверки уже были выполнены
     * @param wallet - кошелек к которому применяется операция
     * @param amountMinor - сумма в единицах
     * @param referenceType - тип операции для идемпотентности (например, "PURCHASE", "REWARD", "REFUND" и т.п.)
     * @param referenceId - идентификатор операции для идемпотентности (например, идентификатор покупки, награды, возврата и т.п.)
     * @param entryType - произвольная строка для классификации записи в журнале
     * @param metadataJson - произвольный JSON для сохранения в записи журнала.
     * @param correlationId - произвольная строка для корреляции событий.
     * @return - результат операции с информацией о новом балансе и идентификаторе записи в журнале.
     */
    private WalletOperationResult applyCredit(
            Wallet wallet,
            long amountMinor,
            String referenceType,
            String referenceId,
            String entryType,
            String metadataJson,
            String correlationId) {
        long newBalance = wallet.getBalanceMinor() + amountMinor;
        wallet.setBalanceMinor(newBalance);
        wallet.setUpdatedAt(Instant.now());

        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID());
        entry.setWallet(wallet);
        entry.setEntryType(entryType);
        entry.setDirection(LedgerDirection.CREDIT);
        entry.setAmountMinor(amountMinor);
        entry.setBalanceAfterMinor(newBalance);
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setMetadataJson(metadataJson);
        ledgerEntryRepository.save(entry);

        enqueueOutbox(wallet, newBalance, entryType, correlationId);
        return WalletOperationResult.builder()
                .ledgerEntryId(entry.getId())
                .balanceMinorAfter(newBalance)
                .idempotentReplay(false)
                .build();
    }

    /**
     * @see "applyCredit"
     */
    private WalletOperationResult applyDebit(
            Wallet wallet,
            long amountMinor,
            String referenceType,
            String referenceId,
            String entryType,
            String metadataJson,
            String correlationId) {
        long newBalance = wallet.getBalanceMinor() - amountMinor;
        wallet.setBalanceMinor(newBalance);
        wallet.setUpdatedAt(Instant.now());

        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID());
        entry.setWallet(wallet);
        entry.setEntryType(entryType);
        entry.setDirection(LedgerDirection.DEBIT);
        entry.setAmountMinor(amountMinor);
        entry.setBalanceAfterMinor(newBalance);
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setMetadataJson(metadataJson);
        ledgerEntryRepository.save(entry);

        enqueueOutbox(wallet, newBalance, entryType, correlationId);
        return WalletOperationResult.builder()
                .ledgerEntryId(entry.getId())
                .balanceMinorAfter(newBalance)
                .idempotentReplay(false)
                .build();
    }

    /**
     * Помещает событие изменения баланса в таблицу исходящих сообщений (outbox) для последующей публикации в Kafka
     * @param wallet - кошелек для которого произошло изменение баланса
     * @param balanceAfter - новый баланс после операции
     * @param entryType - произвольная строка для классификации записи в журнале
     * @param correlationId - произвольная строка для корреляции событий. Если null, будет заменена на пустую строку.
     */
    private void enqueueOutbox(Wallet wallet, long balanceAfter, String entryType, String correlationId) {
        WalletBalanceChangedEvent evt = new WalletBalanceChangedEvent(
                wallet.getUserId().toString(),
                wallet.getId().toString(),
                wallet.getCurrency(),
                balanceAfter,
                entryType,
                correlationId != null ? correlationId : "",
                Instant.now().toString());
        try {
            OutboxEvent ob = new OutboxEvent();
            ob.setId(UUID.randomUUID());
            ob.setWalletId(wallet.getId());
            ob.setUserId(wallet.getUserId());
            ob.setTopic(eventsTopic);
            ob.setPayloadJson(objectMapper.writeValueAsString(evt));
            outboxEventRepository.save(ob);
            // Может нужно более точное исключение
        } catch (RuntimeException e) {
            throw new IllegalStateException("Cannot serialize outbox payload", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Создает кошелек при первой операции кредитования, если кошелька еще нет. Метод предполагает, что проверка на существование кошелька уже была выполнена и он не найден
     * @param userId - идентификатор пользователя для которого создается кошелек
     * @return - созданный кошелек. Если в процессе создания произошла гонка и другой запрос уже создал кошелек, возвращается уже существующий кошелек.
     */
    private Wallet createWalletFirstCredit(UUID userId) {
        Wallet created = Wallet.newWallet(userId);
        try {
            return walletRepository.save(created);
        } catch (DataIntegrityViolationException e) {
            return walletRepository
                    .findByUserIdForUpdate(userId)
                    .orElseThrow(() -> new IllegalStateException("Wallet race resolution failed"));
        }
    }

    private static void validateAmount(long amountMinor) {
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
