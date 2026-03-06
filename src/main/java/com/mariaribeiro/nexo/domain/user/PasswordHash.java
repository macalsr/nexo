package com.mariaribeiro.nexo.domain.user;

import java.util.Objects;

public record PasswordHash(String value) {

    public PasswordHash {
        Objects.requireNonNull(value, "passwordHash must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}
