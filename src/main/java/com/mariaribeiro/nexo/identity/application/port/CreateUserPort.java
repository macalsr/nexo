package com.mariaribeiro.nexo.identity.application.port;

import com.mariaribeiro.nexo.identity.domain.model.User;

public interface CreateUserPort {

    User create(User user);
}
