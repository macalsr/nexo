package com.mariaribeiro.nexo.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.domain.model.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SignupServiceTest {

    private final CreateUserPort createUserPort = mock(CreateUserPort.class);
    private final PasswordHashEncoderPort passwordHashEncoderPort = mock(PasswordHashEncoderPort.class);
    private final TokenServicePort tokenServicePort = mock(TokenServicePort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
    private final SignupService signupService = new SignupService(
            createUserPort,
            passwordHashEncoderPort,
            tokenServicePort,
            clock);

    @Test
    void createsNormalizedUserAndReturnsAccessToken() {
        Instant expiresAt = Instant.parse("2026-03-07T12:00:00Z");
        when(passwordHashEncoderPort.encode("secret123")).thenReturn("$2a$10$abcdefghijklmnopqrstuv");
        when(createUserPort.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenServicePort.issueToken(any(UUID.class), any(String.class)))
                .thenReturn(new SessionToken("token-value", expiresAt));

        SignupResult result = signupService.signup(new SignupCommand(" Person@Example.com ", "secret123"));

        assertThat(result.accessToken()).isEqualTo("token-value");
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void propagatesDuplicateEmailFailures() {
        when(passwordHashEncoderPort.encode("secret123")).thenReturn("$2a$10$abcdefghijklmnopqrstuv");
        when(createUserPort.create(any(User.class))).thenThrow(new DuplicateEmailException());

        assertThatThrownBy(() -> signupService.signup(new SignupCommand("person@example.com", "secret123")))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Unable to create account");
    }
}
