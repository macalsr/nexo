package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.application.auth.SessionToken;
import com.mariaribeiro.nexo.identity.application.auth.TokenClaims;
import java.util.UUID;

public interface TokenServicePort {

    SessionToken issueToken(UUID userId, String normalizedEmail);

    TokenClaims verify(String token);
}

