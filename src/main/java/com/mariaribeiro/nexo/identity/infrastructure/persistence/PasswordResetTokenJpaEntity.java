package com.mariaribeiro.nexo.identity.infrastructure.persistence;

import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 128, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PasswordResetTokenJpaEntity() {
    }

    public PasswordResetTokenJpaEntity(UUID id, UUID userId, String token, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static PasswordResetTokenJpaEntity from(PasswordResetToken passwordResetToken) {
        return new PasswordResetTokenJpaEntity(
                passwordResetToken.id(),
                passwordResetToken.userId(),
                passwordResetToken.token(),
                passwordResetToken.expiresAt(),
                passwordResetToken.createdAt());
    }

    public PasswordResetToken toDomain() {
        return new PasswordResetToken(id, userId, token, expiresAt, createdAt);
    }
}
