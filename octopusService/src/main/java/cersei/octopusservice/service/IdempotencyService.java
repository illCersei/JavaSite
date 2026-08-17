package cersei.octopusservice.service;

import cersei.octopusservice.exception.IdempotencyConflictException;
import cersei.octopusservice.model.ActionIdempotency;
import cersei.octopusservice.repository.ActionIdempotencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final ActionIdempotencyRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public <T> T run(UUID userId, String actionType, String idempotencyKey, Class<T> responseType, Supplier<T> action) {
        ActionIdempotency row = begin(userId, actionType, idempotencyKey);

        if (STATUS_SUCCESS.equals(row.getStatus()) && row.getResponseJson() != null) {
            log.info("idempotency replay userId={} actionType={} key={}", userId, actionType, idempotencyKey);
            return read(row.getResponseJson(), responseType);
        }
        if (STATUS_IN_PROGRESS.equals(row.getStatus())) {
            log.info("idempotency in_progress userId={} actionType={} key={}", userId, actionType, idempotencyKey);
            throw new IdempotencyConflictException("Action already in progress");
        }

        try {
            row.setStatus(STATUS_IN_PROGRESS);
            row.setUpdatedAt(Instant.now());
            repo.save(row);
            log.info("idempotency start userId={} actionType={} key={}", userId, actionType, idempotencyKey);

            T response = action.get();

            row.setStatus(STATUS_SUCCESS);
            row.setResponseJson(write(response));
            row.setUpdatedAt(Instant.now());
            repo.save(row);
            log.info("idempotency success userId={} actionType={} key={}", userId, actionType, idempotencyKey);
            return response;
        } catch (RuntimeException ex) {
            row.setStatus(STATUS_FAILED);
            row.setUpdatedAt(Instant.now());
            repo.save(row);
            log.warn("idempotency failed userId={} actionType={} key={} error={}",
                    userId, actionType, idempotencyKey, ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private ActionIdempotency begin(UUID userId, String actionType, String idempotencyKey) {
        Instant now = Instant.now();
        List<?> insertedIds = entityManager.createNativeQuery("""
                        INSERT INTO action_idempotency
                            (user_id, action_type, idempotency_key, status, response_json, created_at, updated_at)
                        VALUES
                            (:user_id, :action_type, :idempotency_key, :status, NULL, :created_at, :updated_at)
                        ON CONFLICT (user_id, action_type, idempotency_key)
                            DO NOTHING
                        RETURNING id
                        """)
                .setParameter("user_id", userId)
                .setParameter("action_type", actionType)
                .setParameter("idempotency_key", idempotencyKey)
                .setParameter("status", STATUS_CREATED)
                .setParameter("created_at", now)
                .setParameter("updated_at", now)
                .getResultList();

        if (!insertedIds.isEmpty()) {
            Long id = ((Number) insertedIds.get(0)).longValue();
            log.debug("idempotency create userId={} actionType={} key={} id={}", userId, actionType, idempotencyKey, id);
            return repo.findById(id).orElseThrow(() -> new IllegalStateException("Inserted idempotency row not found"));
        }

        log.debug("idempotency exists userId={} actionType={} key={}", userId, actionType, idempotencyKey);
        return repo.findByUserIdAndActionTypeAndIdempotencyKey(userId, actionType, idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Idempotency row not found after conflict"));
    }

    private String write(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize idempotent response", e);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize idempotent response", e);
        }
    }
}

