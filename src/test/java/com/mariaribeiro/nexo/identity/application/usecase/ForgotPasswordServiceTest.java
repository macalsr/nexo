package com.mariaribeiro.nexo.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordResetDeliveryPort;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ForgotPasswordServiceTest {

    private final LoadUserByEmailPort loadUserByEmailPort = mock(LoadUserByEmailPort.class);
    private final CreatePasswordResetTokenPort createPasswordResetTokenPort = mock(CreatePasswordResetTokenPort.class);
    private final PasswordResetDeliveryPort passwordResetDeliveryPort = mock(PasswordResetDeliveryPort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService(
            loadUserByEmailPort,
            createPasswordResetTokenPort,
            passwordResetDeliveryPort,
            clock,
            Duration.ofMinutes(15));

    @Test
    void createsExpiringResetTokenAndInvokesDeliveryForExistingUser() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUserView user = new AuthenticatedUserView(
                userId,
                "person@example.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                false,
                null,
                Instant.parse("2026-03-06T10:00:00Z"));

        when(loadUserByEmailPort.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(createPasswordResetTokenPort.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        forgotPasswordService.requestReset(new ForgotPasswordCommand("Person@Example.com"));

        ArgumentCaptor<PasswordResetToken> passwordResetTokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(createPasswordResetTokenPort).save(passwordResetTokenCaptor.capture());
        PasswordResetToken passwordResetToken = passwordResetTokenCaptor.getValue();

        assertThat(passwordResetToken.userId()).isEqualTo(userId);
        assertThat(passwordResetToken.token()).isNotBlank();
        assertThat(passwordResetToken.createdAt()).isEqualTo(Instant.parse("2026-03-06T12:00:00Z"));
        assertThat(passwordResetToken.expiresAt()).isEqualTo(Instant.parse("2026-03-06T12:15:00Z"));

        verify(passwordResetDeliveryPort).deliver(
                "person@example.com",
                passwordResetToken.token(),
                Instant.parse("2026-03-06T12:15:00Z"));
    }

    @Test
    void doesNothingWhenUserDoesNotExist() {
        when(loadUserByEmailPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        forgotPasswordService.requestReset(new ForgotPasswordCommand("missing@example.com"));

        verify(createPasswordResetTokenPort, never()).save(any());
        verify(passwordResetDeliveryPort, never()).deliver(any(), any(), any());
    }
}
