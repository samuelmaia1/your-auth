package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.infra.repository.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByProjectIdAndId(String projectId, String id);

    Optional<UserEntity> findByProjectIdAndEmailIgnoreCase(String projectId, String email);

    boolean existsByProjectIdAndEmailIgnoreCase(String projectId, String email);

    boolean existsByProjectIdAndEmailIgnoreCaseAndIdNot(String projectId, String email, String id);

    @Query("""
            select user
            from UserEntity user
            where user.projectId = :projectId
              and (:email is null or lower(user.email) like lower(concat('%', :email, '%')))
              and (:status is null or user.status = :status)
            """)
    Page<UserEntity> findAllByProjectId(
            @Param("projectId") String projectId,
            @Param("email") String email,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
