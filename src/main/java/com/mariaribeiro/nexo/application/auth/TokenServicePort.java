package com.mariaribeiro.nexo.application.auth;

import java.util.UUID;

public interface TokenServicePort {

    SessionToken issueToken(UUID userId, String normalizedEmail);

    TokenClaims verify(String token);
}
