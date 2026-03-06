package com.mariaribeiro.nexo.identity.infrastructure.persistence;

import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.MarkUserEmailVerifiedPort;
import com.mariaribeiro.nexo.identity.application.port.UpdateUserPasswordPort;
import com.mariaribeiro.nexo.identity.application.usecase.AuthenticatedUserView;
import com.mariaribeiro.nexo.identity.application.usecase.DuplicateEmailException;
import com.mariaribeiro.nexo.identity.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

public class UserPersistenceAdapter
        implements LoadUserByEmailPort, CreateUserPort, UpdateUserPasswordPort, MarkUserEmailVerifiedPort {

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
                        user.isEmailVerified(),
                        user.getEmailVerifiedAt(),
                        user.getCreatedAt()));
    }

    @Override
    public User create(User user) {
        try {
            UserJpaEntity savedUser = userRepository.saveAndFlush(UserJpaEntity.from(user));
            return User.create(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getPasswordHash(),
                    savedUser.getCreatedAt());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Override
    public void updatePassword(UUID userId, String passwordHash) {
        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for password update"));

        user.updatePasswordHash(passwordHash);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void markEmailVerified(UUID userId, Instant verifiedAt) {
        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for email verification"));

        user.markEmailVerified(verifiedAt);
        userRepository.saveAndFlush(user);
    }
}
