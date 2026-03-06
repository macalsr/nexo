package com.mariaribeiro.nexo.identity.application.port;

import java.util.UUID;

public interface DeletePasswordResetTokenPort {

    void deleteById(UUID id);
}
