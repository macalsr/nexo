package com.mariaribeiro.nexo.identity.adapters.out.notification;

import com.mariaribeiro.nexo.identity.application.port.EmailVerificationDeliveryPort;
import java.time.Instant;

public class StubEmailVerificationDeliveryAdapter implements EmailVerificationDeliveryPort {

    @Override
    public void deliver(String email, String token, Instant expiresAt) {
        // Email delivery is intentionally stubbed for now.
    }
}
