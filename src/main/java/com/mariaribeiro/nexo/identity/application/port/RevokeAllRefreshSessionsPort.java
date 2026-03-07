package com.mariaribeiro.nexo.identity.application.port;

import java.time.Instant;
import java.util.UUID;

public interface RevokeAllRefreshSessionsPort {

    void revokeAllByUserId(UUID userId, Instant revokedAt);
}
