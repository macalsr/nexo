package com.mariaribeiro.nexo.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    void normalizesEmailToTrimmedLowercaseValue() {
        EmailAddress emailAddress = EmailAddress.of("  Person@Example.com ");

        assertThat(emailAddress.value()).isEqualTo("person@example.com");
    }

    @Test
    void rejectsBlankEmails() {
        assertThatThrownBy(() -> EmailAddress.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email must not be blank");
    }
}
