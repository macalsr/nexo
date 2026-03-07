package com.mariaribeiro.nexo.identity.application.verification;

import com.mariaribeiro.nexo.identity.application.auth.AuthenticatedUserView;

public interface IssueEmailVerificationUseCase {

    void issueVerificationFor(AuthenticatedUserView user);
}

