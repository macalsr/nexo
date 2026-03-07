package com.mariaribeiro.nexo.identity.adapters.out.persistence;

import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationTokenJpaEntity {

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

    protected EmailVerificationTokenJpaEntity() {
    }

    public EmailVerificationTokenJpaEntity(UUID id, UUID userId, String token, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static EmailVerificationTokenJpaEntity from(EmailVerificationToken emailVerificationToken) {
        return new EmailVerificationTokenJpaEntity(
                emailVerificationToken.id(),
                emailVerificationToken.userId(),
                emailVerificationToken.token(),
                emailVerificationToken.expiresAt(),
                emailVerificationToken.createdAt());
    }

    public EmailVerificationToken toDomain() {
        return new EmailVerificationToken(id, userId, token, expiresAt, createdAt);
    }
}
