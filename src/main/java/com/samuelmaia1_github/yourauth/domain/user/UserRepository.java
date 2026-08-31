package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByProjectIdAndId(String projectId, String id);

    Optional<User> findByProjectIdAndEmailIgnoreCase(String projectId, String email);

    boolean existsByProjectIdAndEmailIgnoreCase(String projectId, String email);

    boolean existsByProjectIdAndEmailIgnoreCaseAndIdNot(String projectId, String email, String id);

    PageResult<User> findAllByProjectId(String projectId, Pagination pagination);

    void deleteById(String id);
}
