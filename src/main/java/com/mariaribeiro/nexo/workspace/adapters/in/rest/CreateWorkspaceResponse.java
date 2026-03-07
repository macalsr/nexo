package com.mariaribeiro.nexo.workspace.adapters.in.rest;

import java.util.UUID;

public record CreateWorkspaceResponse(
        UUID id,
        String name) {
}
