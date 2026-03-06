package com.mariaribeiro.nexo.identity.application.usecase;

import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordResetDeliveryPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final CreatePasswordResetTokenPort createPasswordResetTokenPort;
    private final PasswordResetDeliveryPort passwordResetDeliveryPort;
    private final Clock clock;
    private final Duration resetTokenTtl;

    public ForgotPasswordService(
            LoadUserByEmailPort loadUserByEmailPort,
            CreatePasswordResetTokenPort createPasswordResetTokenPort,
            PasswordResetDeliveryPort passwordResetDeliveryPort,
            Clock clock,
            Duration resetTokenTtl) {
        this.loadUserByEmailPort = loadUserByEmailPort;
        this.createPasswordResetTokenPort = createPasswordResetTokenPort;
        this.passwordResetDeliveryPort = passwordResetDeliveryPort;
        this.clock = clock;
        this.resetTokenTtl = resetTokenTtl;
    }

    @Override
    public void requestReset(ForgotPasswordCommand command) {
        String normalizedEmail = EmailAddress.of(command.email()).value();
        loadUserByEmailPort.findByEmail(normalizedEmail)
                .ifPresent(user -> {
                    Instant issuedAt = Instant.now(clock);
                    PasswordResetToken passwordResetToken = PasswordResetToken.issue(
                            user.id(),
                            UUID.randomUUID().toString(),
                            issuedAt,
                            resetTokenTtl);

                    createPasswordResetTokenPort.save(passwordResetToken);
                    passwordResetDeliveryPort.deliver(
                            user.email(),
                            passwordResetToken.token(),
                            passwordResetToken.expiresAt());
                });
    }
}
