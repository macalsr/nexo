package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.util.Optional;

public interface LoadEmailVerificationTokenPort {

    Optional<EmailVerificationToken> findByToken(String token);
}

