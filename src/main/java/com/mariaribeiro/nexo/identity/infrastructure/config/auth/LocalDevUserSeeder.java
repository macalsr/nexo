package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataUserRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.UserJpaEntity;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDevUserSeeder implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDevUserSeeder.class);

    static final String DEFAULT_EMAIL = "local.dev@nexo.local";
    static final String DEFAULT_PASSWORD = "LocalDevOnly123!";

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock authClock;

    @Override
    public void run(ApplicationArguments args) {
        seedDefaultUser();
    }

    void seedDefaultUser() {
        String normalizedEmail = DEFAULT_EMAIL.toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            LOGGER.info("Local seeded auth user already exists for {}", normalizedEmail);
            return;
        }

        Instant now = Instant.now(authClock);
        UserJpaEntity localUser = new UserJpaEntity(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                true,
                now,
                now);

        userRepository.saveAndFlush(localUser);
        LOGGER.info(
                "Local seeded auth user is available: email={} password={}",
                DEFAULT_EMAIL,
                DEFAULT_PASSWORD);
    }
}
