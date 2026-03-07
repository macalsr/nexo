package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataUserRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.UserJpaEntity;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class LocalDevUserSeederTest {

    private final SpringDataUserRepository userRepository = mock(SpringDataUserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final Clock authClock = Clock.fixed(Instant.parse("2026-03-07T21:00:00Z"), ZoneOffset.UTC);

    private final LocalDevUserSeeder localDevUserSeeder = new LocalDevUserSeeder(
            userRepository,
            passwordEncoder,
            authClock);

    @Test
    void doesNothingWhenSeedUserAlreadyExists() {
        when(userRepository.findByEmail("local.dev@nexo.local")).thenReturn(Optional.of(mock(UserJpaEntity.class)));

        localDevUserSeeder.seedDefaultUser();

        verify(userRepository, never()).saveAndFlush(any(UserJpaEntity.class));
    }

    @Test
    void createsVerifiedSeedUserWhenMissing() {
        when(userRepository.findByEmail("local.dev@nexo.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("LocalDevOnly123!")).thenReturn("encoded-password");

        localDevUserSeeder.seedDefaultUser();

        ArgumentCaptor<UserJpaEntity> userCaptor = ArgumentCaptor.forClass(UserJpaEntity.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        UserJpaEntity created = userCaptor.getValue();

        assertThat(created.getEmail()).isEqualTo("local.dev@nexo.local");
        assertThat(created.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(created.isEmailVerified()).isTrue();
        assertThat(created.getEmailVerifiedAt()).isEqualTo(Instant.parse("2026-03-07T21:00:00Z"));
        assertThat(created.getCreatedAt()).isEqualTo(Instant.parse("2026-03-07T21:00:00Z"));
    }
}
