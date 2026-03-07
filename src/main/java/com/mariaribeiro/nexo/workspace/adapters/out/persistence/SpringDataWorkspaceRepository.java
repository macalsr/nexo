package com.mariaribeiro.nexo.workspace.adapters.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWorkspaceRepository extends JpaRepository<WorkspaceJpaEntity, UUID> {
}
