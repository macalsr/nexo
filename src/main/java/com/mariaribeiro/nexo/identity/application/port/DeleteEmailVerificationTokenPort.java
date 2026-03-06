package com.mariaribeiro.nexo.identity.application.port;

import java.util.UUID;

public interface DeleteEmailVerificationTokenPort {

    void deleteById(UUID id);

    void deleteByUserId(UUID userId);
}
