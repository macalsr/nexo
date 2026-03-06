package com.mariaribeiro.nexo.identity.application.port;

import java.time.Instant;

public interface EmailVerificationDeliveryPort {

    void deliver(String email, String token, Instant expiresAt);
}
