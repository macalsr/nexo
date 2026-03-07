package com.mariaribeiro.nexo.identity.adapters.out.security;

import com.mariaribeiro.nexo.identity.application.port.RefreshTokenGeneratorPort;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureRefreshTokenGenerator implements RefreshTokenGeneratorPort {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
