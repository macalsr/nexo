package com.mariaribeiro.nexo.identity.application.usecase;

import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.MarkUserEmailVerifiedPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.time.Clock;
import java.time.Instant;

public class VerifyEmailService implements VerifyEmailUseCase {

    private final LoadEmailVerificationTokenPort loadEmailVerificationTokenPort;
    private final MarkUserEmailVerifiedPort markUserEmailVerifiedPort;
    private final DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort;
    private final Clock clock;

    public VerifyEmailService(
            LoadEmailVerificationTokenPort loadEmailVerificationTokenPort,
            MarkUserEmailVerifiedPort markUserEmailVerifiedPort,
            DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort,
            Clock clock) {
        this.loadEmailVerificationTokenPort = loadEmailVerificationTokenPort;
        this.markUserEmailVerifiedPort = markUserEmailVerifiedPort;
        this.deleteEmailVerificationTokenPort = deleteEmailVerificationTokenPort;
        this.clock = clock;
    }

    @Override
    public void verify(VerifyEmailCommand command) {
        EmailVerificationToken emailVerificationToken = loadEmailVerificationTokenPort.findByToken(command.token())
                .orElseThrow(InvalidEmailVerificationTokenException::new);

        if (emailVerificationToken.expiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidEmailVerificationTokenException();
        }

        Instant verifiedAt = Instant.now(clock);
        markUserEmailVerifiedPort.markEmailVerified(emailVerificationToken.userId(), verifiedAt);
        deleteEmailVerificationTokenPort.deleteById(emailVerificationToken.id());
    }
}
