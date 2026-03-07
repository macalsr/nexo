package com.mariaribeiro.nexo.workspace.application;

import com.mariaribeiro.nexo.workspace.adapters.out.persistence.SpringDataWorkspaceMemberRepository;
import com.mariaribeiro.nexo.workspace.adapters.out.persistence.SpringDataWorkspaceRepository;
import com.mariaribeiro.nexo.workspace.adapters.out.persistence.WorkspaceJpaEntity;
import com.mariaribeiro.nexo.workspace.adapters.out.persistence.WorkspaceMemberJpaEntity;
import com.mariaribeiro.nexo.workspace.domain.model.WorkspaceRole;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWorkspaceService {

    private final SpringDataWorkspaceRepository workspaceRepository;
    private final SpringDataWorkspaceMemberRepository workspaceMemberRepository;
    private final Clock clock;

    @Transactional
    public CreateWorkspaceResult create(String workspaceName, UUID userId) {
        Instant now = Instant.now(clock);
        WorkspaceJpaEntity workspace = WorkspaceJpaEntity.create(
                UUID.randomUUID(),
                workspaceName.trim(),
                userId,
                now);

        WorkspaceJpaEntity createdWorkspace = workspaceRepository.saveAndFlush(workspace);
        workspaceMemberRepository.saveAndFlush(
                WorkspaceMemberJpaEntity.of(createdWorkspace.getId(), userId, WorkspaceRole.OWNER, now));

        return new CreateWorkspaceResult(createdWorkspace.getId(), createdWorkspace.getName());
    }
}
