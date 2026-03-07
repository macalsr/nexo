package com.mariaribeiro.nexo.identity.adapters.out.persistence;

import com.mariaribeiro.nexo.identity.domain.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 320, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(
            UUID id,
            String email,
            String passwordHash,
            boolean emailVerified,
            Instant emailVerifiedAt,
            Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.emailVerifiedAt = emailVerifiedAt;
        this.createdAt = createdAt;
    }

    public static UserJpaEntity from(User user) {
        return new UserJpaEntity(
                user.id(),
                user.email().value(),
                user.passwordHash().value(),
                user.emailVerified(),
                user.emailVerifiedAt(),
                user.createdAt());
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void markEmailVerified(Instant verifiedAt) {
        this.emailVerified = true;
        this.emailVerifiedAt = verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

