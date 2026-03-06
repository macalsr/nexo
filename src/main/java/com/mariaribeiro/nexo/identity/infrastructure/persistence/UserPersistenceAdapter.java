package com.mariaribeiro.nexo.identity.infrastructure.persistence;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.usecase.AuthenticatedUserView;
import java.util.Optional;

public class UserPersistenceAdapter implements LoadUserByEmailPort {

    private final SpringDataUserRepository userRepository;

    public UserPersistenceAdapter(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<AuthenticatedUserView> findByEmail(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail)
                .map(user -> new AuthenticatedUserView(
                        user.getId(),
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.getCreatedAt()));
    }
}
