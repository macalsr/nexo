package com.mariaribeiro.nexo.identity.application.port;

import java.util.UUID;

public interface UpdateUserPasswordPort {

    void updatePassword(UUID userId, String passwordHash);
}
