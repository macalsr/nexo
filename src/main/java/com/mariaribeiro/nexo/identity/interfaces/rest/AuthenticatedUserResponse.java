package com.mariaribeiro.nexo.identity.interfaces.rest;

import java.util.UUID;

public record AuthenticatedUserResponse(UUID userId, String email) {
}
