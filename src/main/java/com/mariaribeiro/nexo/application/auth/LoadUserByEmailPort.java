package com.mariaribeiro.nexo.application.auth;

import java.util.Optional;

public interface LoadUserByEmailPort {

    Optional<AuthenticatedUserView> findByEmail(String normalizedEmail);
}
