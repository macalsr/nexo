package com.mariaribeiro.nexo.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mariaribeiro.nexo.identity.application.port.DeletePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadPasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.UpdateUserPasswordPort;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResetPasswordServiceTest {

    private final LoadPasswordResetTokenPort loadPasswordResetTokenPort = mock(LoadPasswordResetTokenPort.class);
    private final UpdateUserPasswordPort updateUserPasswordPort = mock(UpdateUserPasswordPort.class);
    private final DeletePasswordResetTokenPort deletePasswordResetTokenPort = mock(DeletePasswordResetTokenPort.class);
    private final PasswordHashEncoderPort passwordHashEncoderPort = mock(PasswordHashEncoderPort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
    private final ResetPasswordService resetPasswordService = new ResetPasswordService(
            loadPasswordResetTokenPort,
            updateUserPasswordPort,
            deletePasswordResetTokenPort,
            passwordHashEncoderPort,
            clock);

    @Test
    void updatesPasswordAndInvalidatesTokenWhenTokenIsValid() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PasswordResetToken passwordResetToken = new PasswordResetToken(
                tokenId,
                userId,
                "reset-token",
                Instant.parse("2026-03-06T12:15:00Z"),
                Instant.parse("2026-03-06T12:00:00Z"));

        when(loadPasswordResetTokenPort.findByToken("reset-token")).thenReturn(Optional.of(passwordResetToken));
        when(passwordHashEncoderPort.encode("new-secret123")).thenReturn("hashed-password");

        resetPasswordService.resetPassword(new ResetPasswordCommand("reset-token", "new-secret123"));

        verify(updateUserPasswordPort).updatePassword(userId, "hashed-password");
        verify(deletePasswordResetTokenPort).deleteById(tokenId);
    }

    @Test
    void rejectsMissingTokenWithGenericError() {
        when(loadPasswordResetTokenPort.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordService.resetPassword(
                new ResetPasswordCommand("missing-token", "new-secret123")))
                .isInstanceOf(InvalidResetTokenException.class)
                .hasMessage("Invalid reset token");

        verify(updateUserPasswordPort, never()).updatePassword(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(deletePasswordResetTokenPort, never()).deleteById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsExpiredTokenWithGenericError() {
        PasswordResetToken expiredToken = new PasswordResetToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "expired-token",
                Instant.parse("2026-03-06T11:59:59Z"),
                Instant.parse("2026-03-06T11:45:00Z"));

        when(loadPasswordResetTokenPort.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> resetPasswordService.resetPassword(
                new ResetPasswordCommand("expired-token", "new-secret123")))
                .isInstanceOf(InvalidResetTokenException.class)
                .hasMessage("Invalid reset token");

        verify(updateUserPasswordPort, never()).updatePassword(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(deletePasswordResetTokenPort, never()).deleteById(org.mockito.ArgumentMatchers.any());
    }
}
