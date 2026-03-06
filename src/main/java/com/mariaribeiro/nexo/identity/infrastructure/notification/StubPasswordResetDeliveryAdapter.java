package com.mariaribeiro.nexo.identity.infrastructure.notification;

import com.mariaribeiro.nexo.identity.application.port.PasswordResetDeliveryPort;
import java.time.Instant;

public class StubPasswordResetDeliveryAdapter implements PasswordResetDeliveryPort {

    @Override
    public void deliver(String email, String token, Instant expiresAt) {
        // Email delivery is intentionally stubbed for now.
    }
}
