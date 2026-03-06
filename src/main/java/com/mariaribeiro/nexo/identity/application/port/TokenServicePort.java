package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.application.usecase.SessionToken;
import com.mariaribeiro.nexo.identity.application.usecase.TokenClaims;
import java.util.UUID;

public interface TokenServicePort {

    SessionToken issueToken(UUID userId, String normalizedEmail);

    TokenClaims verify(String token);
}
