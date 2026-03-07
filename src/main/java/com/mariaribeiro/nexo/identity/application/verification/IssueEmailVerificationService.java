package com.mariaribeiro.nexo.identity.application.verification;

import com.mariaribeiro.nexo.identity.application.auth.AuthenticatedUserView;
import com.mariaribeiro.nexo.identity.application.port.CreateEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.EmailVerificationDeliveryPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class IssueEmailVerificationService implements IssueEmailVerificationUseCase {

    private final CreateEmailVerificationTokenPort createEmailVerificationTokenPort;
    private final DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort;
    private final EmailVerificationDeliveryPort emailVerificationDeliveryPort;
    private final Clock clock;
    private final Duration tokenTtl;

    public IssueEmailVerificationService(
            CreateEmailVerificationTokenPort createEmailVerificationTokenPort,
            DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort,
            EmailVerificationDeliveryPort emailVerificationDeliveryPort,
            Clock clock,
            Duration tokenTtl) {
        this.createEmailVerificationTokenPort = createEmailVerificationTokenPort;
        this.deleteEmailVerificationTokenPort = deleteEmailVerificationTokenPort;
        this.emailVerificationDeliveryPort = emailVerificationDeliveryPort;
        this.clock = clock;
        this.tokenTtl = tokenTtl;
    }

    @Override
    public void issueVerificationFor(AuthenticatedUserView user) {
        if (user.emailVerified()) {
            return;
        }

        Instant issuedAt = Instant.now(clock);
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.issue(
                user.id(),
                UUID.randomUUID().toString(),
                issuedAt,
                tokenTtl);

        deleteEmailVerificationTokenPort.deleteByUserId(user.id());
        createEmailVerificationTokenPort.save(emailVerificationToken);
        emailVerificationDeliveryPort.deliver(
                user.email(),
                emailVerificationToken.token(),
                emailVerificationToken.expiresAt());
    }
}

