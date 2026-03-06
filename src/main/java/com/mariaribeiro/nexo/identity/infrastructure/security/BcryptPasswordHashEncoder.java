package com.mariaribeiro.nexo.identity.infrastructure.security;

import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BcryptPasswordHashEncoder implements PasswordHashEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHashEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
