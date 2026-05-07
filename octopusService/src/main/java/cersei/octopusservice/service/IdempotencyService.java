package cersei.octopusservice.service;

import cersei.octopusservice.exception.IdempotencyConflictException;
import cersei.octopusservice.model.ActionIdempotency;
import cersei.octopusservice.repository.ActionIdempotencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final ActionIdempotencyRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public <T> T run(UUID userId, String actionType, String idempotencyKey, Class<T> responseType, Supplier<T> action) {
        ActionIdempotency row = begin(userId, actionType, idempotencyKey);

        if (STATUS_SUCCESS.equals(row.getStatus()) && row.getResponseJson() != null) {
            return read(row.getResponseJson(), responseType);
        }
        if (STATUS_IN_PROGRESS.equals(row.getStatus())) {
            throw new IdempotencyConflictException("Action already in progress");
        }

        try {
            row.setStatus(STATUS_IN_PROGRESS);
            row.setUpdatedAt(Instant.now());
            repo.save(row);

            T response = action.get();

            row.setStatus(STATUS_SUCCESS);
            row.setResponseJson(write(response));
            row.setUpdatedAt(Instant.now());
            repo.save(row);
            return response;
        } catch (RuntimeException ex) {
            row.setStatus(STATUS_FAILED);
            row.setUpdatedAt(Instant.now());
            repo.save(row);
            throw ex;
        }
    }

    private ActionIdempotency begin(UUID userId, String actionType, String idempotencyKey) {
        ActionIdempotency fresh = new ActionIdempotency();
        fresh.setUserId(userId);
        fresh.setActionType(actionType);
        fresh.setIdempotencyKey(idempotencyKey);
        fresh.setStatus(STATUS_CREATED);
        fresh.setCreatedAt(Instant.now());
        fresh.setUpdatedAt(Instant.now());
        try {
            return repo.save(fresh);
        } catch (DataIntegrityViolationException e) {
            return repo.findByUserIdAndActionTypeAndIdempotencyKey(userId, actionType, idempotencyKey)
                    .orElseThrow(() -> e);
        }
    }

    private String write(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotent response", e);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize idempotent response", e);
        }
    }
}

