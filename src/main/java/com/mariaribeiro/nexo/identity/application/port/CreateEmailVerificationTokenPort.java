package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;

public interface CreateEmailVerificationTokenPort {

    EmailVerificationToken save(EmailVerificationToken emailVerificationToken);
}

