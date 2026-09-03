package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.UserEntity;
import com.samuelmaia1_github.yourauth.infra.repository.entity.UserSessionEntity;

public interface UserSessionDetailsProjection {
    UserSessionEntity getSession();

    UserEntity getSessionUser();
}
