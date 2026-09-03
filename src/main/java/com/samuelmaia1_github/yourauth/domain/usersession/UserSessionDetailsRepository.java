package com.samuelmaia1_github.yourauth.domain.usersession;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;

public interface UserSessionDetailsRepository {
    PageResult<UserSessionDetails> findAllByProjectId(String projectId, Pagination pagination);
}
