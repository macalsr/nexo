package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import com.mariaribeiro.nexo.identity.adapters.out.security.AuthTokenProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.EmailVerificationProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.PasswordResetProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties({
        AuthTokenProperties.class,
        PasswordResetProperties.class,
        EmailVerificationProperties.class,
        RefreshTokenProperties.class
})
public class AuthSecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
