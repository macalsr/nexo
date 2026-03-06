package com.mariaribeiro.nexo.domain.user;

import java.util.Locale;
import java.util.Objects;

public record EmailAddress(String value) {

    public EmailAddress {
        Objects.requireNonNull(value, "email must not be null");

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        value = normalizedValue;
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
