package com.mariaribeiro.nexo.identity.application.recovery;

import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordResetDeliveryPort;
import com.mariaribeiro.nexo.identity.adapters.out.security.PasswordResetProperties;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final CreatePasswordResetTokenPort createPasswordResetTokenPort;
    private final PasswordResetDeliveryPort passwordResetDeliveryPort;
    private final Clock clock;
    private final PasswordResetProperties passwordResetProperties;

    public void requestReset(ForgotPasswordCommand command) {
        String normalizedEmail = EmailAddress.of(command.email()).value();
        loadUserByEmailPort.findByEmail(normalizedEmail)
                .ifPresent(user -> {
                    Instant issuedAt = Instant.now(clock);
                    PasswordResetToken passwordResetToken = PasswordResetToken.issue(
                            user.id(),
                            UUID.randomUUID().toString(),
                            issuedAt,
                            passwordResetProperties.getTokenTtl());

                    createPasswordResetTokenPort.save(passwordResetToken);
                    passwordResetDeliveryPort.deliver(
                            user.email(),
                            passwordResetToken.token(),
                            passwordResetToken.expiresAt());
                });
    }
}

