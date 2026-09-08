package com.samuelmaia1_github.yourauth.domain.projectmember;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;

public interface ProjectMemberDetailsRepository {
    PageResult<ProjectMemberDetails> findAllByProjectId(String projectId, Pagination pagination);
}
