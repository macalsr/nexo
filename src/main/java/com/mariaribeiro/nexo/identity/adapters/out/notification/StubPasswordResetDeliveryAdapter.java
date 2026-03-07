package com.mariaribeiro.nexo.identity.adapters.out.notification;

import com.mariaribeiro.nexo.identity.application.port.PasswordResetDeliveryPort;
import java.time.Instant;

public class StubPasswordResetDeliveryAdapter implements PasswordResetDeliveryPort {

    @Override
    public void deliver(String email, String token, Instant expiresAt) {
        //Todo Email delivery is intentionally stubbed for now.
    }
}

