package com.studypilot.idempotency;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "idempotency_records")
@IdClass(IdempotencyRecord.Key.class)
public class IdempotencyRecord implements Persistable<IdempotencyRecord.Key> {

    @Id
    private String scope;
    @Id
    private String idempotencyKey;
    private String requestHash;
    private int responseStatus;
    private String responseBody;
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    IdempotencyRecord(String scope, String idempotencyKey, String requestHash, int responseStatus,
            String responseBody, Instant createdAt) {
        this.scope = scope;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    @Override
    public Key getId() { return new Key(scope, idempotencyKey); }

    /** Always INSERT, never merge: a concurrent duplicate must fail on the primary key, not overwrite. */
    @Override
    public boolean isNew() { return true; }

    String getRequestHash() { return requestHash; }
    int getResponseStatus() { return responseStatus; }
    String getResponseBody() { return responseBody; }

    public static class Key implements Serializable {
        private String scope;
        private String idempotencyKey;

        protected Key() {
        }

        public Key(String scope, String idempotencyKey) {
            this.scope = scope;
            this.idempotencyKey = idempotencyKey;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(scope, k.scope) && Objects.equals(idempotencyKey, k.idempotencyKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scope, idempotencyKey);
        }
    }
}
