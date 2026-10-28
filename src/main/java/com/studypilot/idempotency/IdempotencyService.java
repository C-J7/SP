package com.studypilot.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypilot.common.ConflictException;
import com.studypilot.common.UnprocessableException;

/**
 * Exactly-once semantics for retried POSTs, in the style of payment APIs.
 * <ul>
 *   <li>Same key, same payload: the stored response is replayed and nothing is created twice.</li>
 *   <li>Same key, different payload: 422, because the client is reusing a key by mistake.</li>
 *   <li>Two concurrent requests with one key: the primary key makes one transaction lose; it gets 409 and
 *       everything it wrote, including the created resource, rolls back.</li>
 * </ul>
 */
@Service
public class IdempotencyService {

    public static final String HEADER = "Idempotency-Key";
    public static final String REPLAYED_HEADER = "Idempotent-Replayed";
    private static final Pattern VALID_KEY = Pattern.compile("[A-Za-z0-9_-]{8,100}");

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IdempotencyService(IdempotencyRecordRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /**
     * Runs {@code action} once per (scope, key). Without a key the action simply runs.
     *
     * @param fingerprint canonical representation of the request; its hash detects key reuse with a different body
     */
    @Transactional
    public <T> ResponseEntity<T> execute(String scope, String key, String fingerprint, Class<T> responseType,
            Supplier<T> action) {
        if (key == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(action.get());
        }
        if (!VALID_KEY.matcher(key).matches()) {
            throw new UnprocessableException(HEADER + " must be 8-100 characters of letters, digits, '-' or '_'");
        }
        String requestHash = sha256(fingerprint);
        var existing = repository.findById(new IdempotencyRecord.Key(scope, key));
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new UnprocessableException(HEADER + " was already used with a different request payload");
            }
            return ResponseEntity.status(record.getResponseStatus())
                    .header(REPLAYED_HEADER, "true")
                    .body(read(record.getResponseBody(), responseType));
        }

        T body = action.get();
        try {
            repository.saveAndFlush(new IdempotencyRecord(scope, key, requestHash, HttpStatus.CREATED.value(),
                    objectMapper.writeValueAsString(body), clock.instant()));
        } catch (DataIntegrityViolationException race) {
            throw new ConflictException("A request with this " + HEADER + " is already being processed");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Response is not serialisable", e);
        }
        return ResponseEntity.status(HttpStatus.CREATED).header(REPLAYED_HEADER, "false").body(body);
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored idempotent response is corrupt", e);
        }
    }

    public static String sha256(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is mandatory in every JRE", e);
        }
    }
}
