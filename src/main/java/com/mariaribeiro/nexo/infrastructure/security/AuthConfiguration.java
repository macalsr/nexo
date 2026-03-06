package com.mariaribeiro.nexo.infrastructure.security;

import com.mariaribeiro.nexo.adapters.out.persistence.user.SpringDataUserRepository;
import com.mariaribeiro.nexo.adapters.out.persistence.user.UserPersistenceAdapter;
import com.mariaribeiro.nexo.application.auth.LoadUserByEmailPort;
import com.mariaribeiro.nexo.application.auth.LoginService;
import com.mariaribeiro.nexo.application.auth.LoginUseCase;
import com.mariaribeiro.nexo.application.auth.PasswordHashVerifierPort;
import com.mariaribeiro.nexo.application.auth.TokenServicePort;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AuthTokenProperties.class)
public class AuthConfiguration {

    @Bean
    Clock authClock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    LoadUserByEmailPort loadUserByEmailPort(SpringDataUserRepository userRepository) {
        return new UserPersistenceAdapter(userRepository);
    }

    @Bean
    PasswordHashVerifierPort passwordHashVerifierPort(PasswordEncoder passwordEncoder) {
        return new BcryptPasswordHashVerifier(passwordEncoder);
    }

    @Bean
    TokenServicePort tokenServicePort(Clock authClock, AuthTokenProperties properties) {
        return new JwtTokenService(authClock, properties);
    }

    @Bean
    LoginUseCase loginUseCase(
            LoadUserByEmailPort loadUserByEmailPort,
            PasswordHashVerifierPort passwordHashVerifierPort,
            TokenServicePort tokenServicePort) {
        return new LoginService(loadUserByEmailPort, passwordHashVerifierPort, tokenServicePort);
    }
}
