package com.mariaribeiro.nexo.infrastructure.security;

import com.mariaribeiro.nexo.application.auth.PasswordHashVerifierPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BcryptPasswordHashVerifier implements PasswordHashVerifierPort {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHashVerifier(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
