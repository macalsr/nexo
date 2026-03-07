package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import com.mariaribeiro.nexo.identity.adapters.out.persistence.EmailVerificationTokenPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.PasswordResetTokenPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.RefreshSessionPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataEmailVerificationTokenRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataPasswordResetTokenRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataRefreshSessionRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataUserRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.UserPersistenceAdapter;
import com.mariaribeiro.nexo.identity.application.port.CreateEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeletePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadPasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByIdPort;
import com.mariaribeiro.nexo.identity.application.port.MarkUserEmailVerifiedPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import com.mariaribeiro.nexo.identity.application.port.UpdateUserPasswordPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthPersistenceConfiguration {

    @Bean
    UserPersistenceAdapter userPersistenceAdapter(SpringDataUserRepository userRepository) {
        return new UserPersistenceAdapter(userRepository);
    }

    @Bean
    PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter(
            SpringDataPasswordResetTokenRepository passwordResetTokenRepository) {
        return new PasswordResetTokenPersistenceAdapter(passwordResetTokenRepository);
    }

    @Bean
    EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter(
            SpringDataEmailVerificationTokenRepository emailVerificationTokenRepository) {
        return new EmailVerificationTokenPersistenceAdapter(emailVerificationTokenRepository);
    }

    @Bean
    RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter(
            SpringDataRefreshSessionRepository refreshSessionRepository) {
        return new RefreshSessionPersistenceAdapter(refreshSessionRepository);
    }

    @Bean
    LoadUserByEmailPort loadUserByEmailPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    LoadUserByIdPort loadUserByIdPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    CreateUserPort createUserPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    UpdateUserPasswordPort updateUserPasswordPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    MarkUserEmailVerifiedPort markUserEmailVerifiedPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    CreatePasswordResetTokenPort createPasswordResetTokenPort(
            PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter) {
        return passwordResetTokenPersistenceAdapter;
    }

    @Bean
    CreateEmailVerificationTokenPort createEmailVerificationTokenPort(
            EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter) {
        return emailVerificationTokenPersistenceAdapter;
    }

    @Bean
    LoadPasswordResetTokenPort loadPasswordResetTokenPort(
            PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter) {
        return passwordResetTokenPersistenceAdapter;
    }

    @Bean
    DeletePasswordResetTokenPort deletePasswordResetTokenPort(
            PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter) {
        return passwordResetTokenPersistenceAdapter;
    }

    @Bean
    LoadEmailVerificationTokenPort loadEmailVerificationTokenPort(
            EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter) {
        return emailVerificationTokenPersistenceAdapter;
    }

    @Bean
    DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort(
            EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter) {
        return emailVerificationTokenPersistenceAdapter;
    }

    @Bean
    SaveRefreshSessionPort saveRefreshSessionPort(RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter) {
        return refreshSessionPersistenceAdapter;
    }

    @Bean
    LoadRefreshSessionByTokenHashPort loadRefreshSessionByTokenHashPort(
            RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter) {
        return refreshSessionPersistenceAdapter;
    }

    @Bean
    RevokeAllRefreshSessionsPort revokeAllRefreshSessionsPort(
            RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter) {
        return refreshSessionPersistenceAdapter;
    }
}
