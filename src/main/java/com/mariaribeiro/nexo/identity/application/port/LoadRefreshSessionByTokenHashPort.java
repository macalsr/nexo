package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;
import java.util.Optional;

public interface LoadRefreshSessionByTokenHashPort {

    Optional<RefreshSession> findByTokenHash(String tokenHash);
}
