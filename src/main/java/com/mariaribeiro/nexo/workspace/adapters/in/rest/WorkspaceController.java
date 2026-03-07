package com.mariaribeiro.nexo.workspace.adapters.in.rest;

import com.mariaribeiro.nexo.identity.adapters.in.security.AuthenticatedUserContext;
import com.mariaribeiro.nexo.identity.adapters.in.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.adapters.out.security.UnauthorizedException;
import com.mariaribeiro.nexo.workspace.application.CreateWorkspaceService;
import com.mariaribeiro.nexo.workspace.application.CreateWorkspaceResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final AuthenticationRequestContext authenticationRequestContext;
    private final CreateWorkspaceService createWorkspaceService;

    @PostMapping
    public ResponseEntity<CreateWorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            HttpServletRequest httpRequest) {
        AuthenticatedUserContext authenticatedUser = authenticationRequestContext.getAuthenticatedUser(httpRequest)
                .orElseThrow(UnauthorizedException::new);

        CreateWorkspaceResult result = createWorkspaceService.create(request.name(), authenticatedUser.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateWorkspaceResponse(result.id(), result.name()));
    }
}
