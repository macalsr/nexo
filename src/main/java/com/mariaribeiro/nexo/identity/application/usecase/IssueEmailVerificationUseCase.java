package com.mariaribeiro.nexo.identity.application.usecase;

public interface IssueEmailVerificationUseCase {

    void issueVerificationFor(AuthenticatedUserView user);
}
