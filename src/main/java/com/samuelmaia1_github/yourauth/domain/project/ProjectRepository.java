package com.samuelmaia1_github.yourauth.domain.project;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;

import java.util.Optional;

public interface ProjectRepository {
    Project save(Project project);

    Optional<Project> findById(String id);

    boolean existsByOwnerAccountIdAndName(String ownerAccountId, String name);

    boolean existsByOwnerAccountIdAndNameAndIdNot(String ownerAccountId, String name, String id);

    PageResult<Project> findAllByOwnerAccountId(String id, Pagination pagination);

    PageResult<Project> findAllByMemberAccountId(String accountId, Pagination pagination);

    void deleteById(String id);
}
