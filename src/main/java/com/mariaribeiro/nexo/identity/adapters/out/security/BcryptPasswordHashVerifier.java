package com.mariaribeiro.nexo.identity.adapters.out.security;

import com.mariaribeiro.nexo.identity.application.port.PasswordHashVerifierPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class BcryptPasswordHashVerifier implements PasswordHashVerifierPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}

