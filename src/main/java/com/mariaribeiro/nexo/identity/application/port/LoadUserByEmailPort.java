package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.application.usecase.AuthenticatedUserView;
import java.util.Optional;

public interface LoadUserByEmailPort {

    Optional<AuthenticatedUserView> findByEmail(String normalizedEmail);
}
