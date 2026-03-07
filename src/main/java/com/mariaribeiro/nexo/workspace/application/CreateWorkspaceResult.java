package com.mariaribeiro.nexo.workspace.application;

import java.util.UUID;

public record CreateWorkspaceResult(
        UUID id,
        String name) {
}
