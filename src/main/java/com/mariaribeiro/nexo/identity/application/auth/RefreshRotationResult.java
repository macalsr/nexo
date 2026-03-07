package com.mariaribeiro.nexo.identity.application.auth;

import java.util.UUID;

public record RefreshRotationResult(UUID userId, String refreshToken) {
}

