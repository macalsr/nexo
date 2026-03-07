package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;

public interface CreatePasswordResetTokenPort {

    PasswordResetToken save(PasswordResetToken passwordResetToken);
}

