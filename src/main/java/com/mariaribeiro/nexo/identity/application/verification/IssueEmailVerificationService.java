package com.mariaribeiro.nexo.identity.application.verification;

import com.mariaribeiro.nexo.identity.application.auth.AuthenticatedUserView;
import com.mariaribeiro.nexo.identity.application.port.CreateEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.EmailVerificationDeliveryPort;
import com.mariaribeiro.nexo.identity.adapters.out.security.EmailVerificationProperties;
import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueEmailVerificationService {

    private final CreateEmailVerificationTokenPort createEmailVerificationTokenPort;
    private final DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort;
    private final EmailVerificationDeliveryPort emailVerificationDeliveryPort;
    private final Clock clock;
    private final EmailVerificationProperties emailVerificationProperties;

    public void issueVerificationFor(AuthenticatedUserView user) {
        if (user.emailVerified()) {
            return;
        }

        Instant issuedAt = Instant.now(clock);
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.issue(
                user.id(),
                UUID.randomUUID().toString(),
                issuedAt,
                emailVerificationProperties.getTokenTtl());

        deleteEmailVerificationTokenPort.deleteByUserId(user.id());
        createEmailVerificationTokenPort.save(emailVerificationToken);
        emailVerificationDeliveryPort.deliver(
                user.email(),
                emailVerificationToken.token(),
                emailVerificationToken.expiresAt());
    }
}

