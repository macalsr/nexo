package com.mariaribeiro.nexo.identity.application.verification;

import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.MarkUserEmailVerifiedPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyEmailService {

    private final LoadEmailVerificationTokenPort loadEmailVerificationTokenPort;
    private final MarkUserEmailVerifiedPort markUserEmailVerifiedPort;
    private final DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort;
    private final Clock clock;

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

