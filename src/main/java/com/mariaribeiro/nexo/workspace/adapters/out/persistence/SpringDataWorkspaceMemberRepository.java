package com.mariaribeiro.nexo.workspace.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWorkspaceMemberRepository extends JpaRepository<WorkspaceMemberJpaEntity, WorkspaceMemberId> {
}
