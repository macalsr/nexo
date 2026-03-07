package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;

public interface SaveRefreshSessionPort {

    RefreshSession save(RefreshSession refreshSession);
}
