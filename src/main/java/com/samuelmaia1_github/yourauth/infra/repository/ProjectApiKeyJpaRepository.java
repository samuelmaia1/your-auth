package com.samuelmaia1_github.yourauth.infra.repository;

import com.samuelmaia1_github.yourauth.infra.repository.entity.ProjectApiKeyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectApiKeyJpaRepository extends JpaRepository<ProjectApiKeyEntity, String> {
    Optional<ProjectApiKeyEntity> findByProjectIdAndId(String projectId, String id);

    Optional<ProjectApiKeyEntity> findByKeyId(String keyId);

    Optional<ProjectApiKeyEntity> findByPrefix(String prefix);

    @Query(
            value = """
                    select apiKey
                    from ProjectApiKeyEntity apiKey
                    join AccountEntity account
                        on account.id = apiKey.createdByAccountId
                    where apiKey.projectId = :projectId
                      and (
                          :createdBy is null
                          or lower(account.email) like lower(concat('%', :createdBy, '%'))
                          or lower(concat(concat(account.name, ' '), account.lastName)) like lower(concat('%', :createdBy, '%'))
                      )
                    """,
            countQuery = """
                    select count(apiKey)
                    from ProjectApiKeyEntity apiKey
                    join AccountEntity account
                        on account.id = apiKey.createdByAccountId
                    where apiKey.projectId = :projectId
                      and (
                          :createdBy is null
                          or lower(account.email) like lower(concat('%', :createdBy, '%'))
                          or lower(concat(concat(account.name, ' '), account.lastName)) like lower(concat('%', :createdBy, '%'))
                      )
                    """
    )
    Page<ProjectApiKeyEntity> findAllByProjectId(
            @Param("projectId") String projectId,
            @Param("createdBy") String createdBy,
            Pageable pageable
    );
}
