package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.util.Optional;

public interface LoadPasswordResetTokenPort {

    Optional<PasswordResetToken> findByToken(String token);
}
