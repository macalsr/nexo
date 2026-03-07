CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_workspaces_name_not_blank CHECK (TRIM(name) <> ''),
    CONSTRAINT fk_workspaces_created_by_user
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE workspace_members (
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(16) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_workspace_members PRIMARY KEY (workspace_id, user_id),
    CONSTRAINT fk_workspace_members_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT fk_workspace_members_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_workspace_members_role CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER'))
);

CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);
