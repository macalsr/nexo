package com.mariaribeiro.nexo.identity.application.port;

import java.time.Instant;
import java.util.UUID;

public interface MarkUserEmailVerifiedPort {

    void markEmailVerified(UUID userId, Instant verifiedAt);
}
