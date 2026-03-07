package com.mariaribeiro.nexo.identity.application.recovery;

import com.mariaribeiro.nexo.identity.application.port.DeletePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadPasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.UpdateUserPasswordPort;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.time.Clock;
import java.time.Instant;

public class ResetPasswordService implements ResetPasswordUseCase {

    private final LoadPasswordResetTokenPort loadPasswordResetTokenPort;
    private final UpdateUserPasswordPort updateUserPasswordPort;
    private final DeletePasswordResetTokenPort deletePasswordResetTokenPort;
    private final PasswordHashEncoderPort passwordHashEncoderPort;
    private final Clock clock;

    public ResetPasswordService(
            LoadPasswordResetTokenPort loadPasswordResetTokenPort,
            UpdateUserPasswordPort updateUserPasswordPort,
            DeletePasswordResetTokenPort deletePasswordResetTokenPort,
            PasswordHashEncoderPort passwordHashEncoderPort,
            Clock clock) {
        this.loadPasswordResetTokenPort = loadPasswordResetTokenPort;
        this.updateUserPasswordPort = updateUserPasswordPort;
        this.deletePasswordResetTokenPort = deletePasswordResetTokenPort;
        this.passwordHashEncoderPort = passwordHashEncoderPort;
        this.clock = clock;
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        PasswordResetToken passwordResetToken = loadPasswordResetTokenPort.findByToken(command.token())
                .orElseThrow(InvalidResetTokenException::new);

        if (passwordResetToken.expiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidResetTokenException();
        }

        String passwordHash = passwordHashEncoderPort.encode(command.newPassword());
        updateUserPasswordPort.updatePassword(passwordResetToken.userId(), passwordHash);
        deletePasswordResetTokenPort.deleteById(passwordResetToken.id());
    }
}

