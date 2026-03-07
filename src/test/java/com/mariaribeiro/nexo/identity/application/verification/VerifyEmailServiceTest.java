package com.mariaribeiro.nexo.identity.application.verification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.MarkUserEmailVerifiedPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VerifyEmailServiceTest {

    private final LoadEmailVerificationTokenPort loadEmailVerificationTokenPort = mock(LoadEmailVerificationTokenPort.class);
    private final MarkUserEmailVerifiedPort markUserEmailVerifiedPort = mock(MarkUserEmailVerifiedPort.class);
    private final DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort = mock(DeleteEmailVerificationTokenPort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
    private final VerifyEmailService verifyEmailService = new VerifyEmailService(
            loadEmailVerificationTokenPort,
            markUserEmailVerifiedPort,
            deleteEmailVerificationTokenPort,
            clock);

    @Test
    void marksUserVerifiedAndDeletesTokenWhenTokenIsValid() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EmailVerificationToken token = new EmailVerificationToken(
                tokenId,
                userId,
                "verify-token",
                Instant.parse("2026-03-06T12:15:00Z"),
                Instant.parse("2026-03-06T12:00:00Z"));

        when(loadEmailVerificationTokenPort.findByToken("verify-token")).thenReturn(Optional.of(token));

        verifyEmailService.verify(new VerifyEmailCommand("verify-token"));

        verify(markUserEmailVerifiedPort).markEmailVerified(userId, Instant.parse("2026-03-06T12:00:00Z"));
        verify(deleteEmailVerificationTokenPort).deleteById(tokenId);
    }

    @Test
    void rejectsExpiredTokenWithGenericError() {
        EmailVerificationToken token = new EmailVerificationToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "expired-token",
                Instant.parse("2026-03-06T11:59:59Z"),
                Instant.parse("2026-03-06T11:00:00Z"));

        when(loadEmailVerificationTokenPort.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> verifyEmailService.verify(new VerifyEmailCommand("expired-token")))
                .isInstanceOf(InvalidEmailVerificationTokenException.class)
                .hasMessage("Invalid verification token");

        verify(markUserEmailVerifiedPort, never()).markEmailVerified(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(deleteEmailVerificationTokenPort, never()).deleteById(org.mockito.ArgumentMatchers.any());
    }
}

