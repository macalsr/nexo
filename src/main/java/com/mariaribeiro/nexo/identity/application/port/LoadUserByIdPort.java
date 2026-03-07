package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.application.auth.AuthenticatedUserView;
import java.util.Optional;
import java.util.UUID;

public interface LoadUserByIdPort {

    Optional<AuthenticatedUserView> findById(UUID userId);
}

