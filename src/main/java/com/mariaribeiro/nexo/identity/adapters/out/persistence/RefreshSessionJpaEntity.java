package com.mariaribeiro.nexo.identity.adapters.out.persistence;

import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_sessions")
public class RefreshSessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 128, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_session_id")
    private UUID replacedBySessionId;

    protected RefreshSessionJpaEntity() {
    }

    public RefreshSessionJpaEntity(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant lastUsedAt,
            Instant revokedAt,
            UUID replacedBySessionId) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.replacedBySessionId = replacedBySessionId;
    }

    public static RefreshSessionJpaEntity from(RefreshSession refreshSession) {
        return new RefreshSessionJpaEntity(
                refreshSession.id(),
                refreshSession.userId(),
                refreshSession.tokenHash(),
                refreshSession.expiresAt(),
                refreshSession.createdAt(),
                refreshSession.lastUsedAt(),
                refreshSession.revokedAt(),
                refreshSession.replacedBySessionId());
    }

    public RefreshSession toDomain() {
        return RefreshSession.restore(
                id,
                userId,
                tokenHash,
                expiresAt,
                createdAt,
                lastUsedAt,
                revokedAt,
                replacedBySessionId);
    }
}

