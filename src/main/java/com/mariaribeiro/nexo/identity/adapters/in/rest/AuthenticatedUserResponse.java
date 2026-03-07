package com.mariaribeiro.nexo.identity.adapters.in.rest;

import java.util.UUID;

public record AuthenticatedUserResponse(UUID userId, String email, boolean emailVerified) {
}
