package com.mariaribeiro.nexo.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record User(UUID id, EmailAddress email, PasswordHash passwordHash, Instant createdAt) {

    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static User create(UUID id, String email, String passwordHash, Instant createdAt) {
        return new User(id, EmailAddress.of(email), PasswordHash.of(passwordHash), createdAt);
    }
}
