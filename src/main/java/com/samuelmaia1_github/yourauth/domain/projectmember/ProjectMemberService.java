package com.samuelmaia1_github.yourauth.domain.projectmember;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.projectmember.exceptions.ProjectMemberNotFoundException;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.infra.cache.names.AuthConfigCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.PasswordConfigCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.ProjectApiKeyCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.ProjectCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.ProjectMemberCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.UserCacheNames;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private static final List<ProjectMemberRole> PROJECT_MEMBER_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final ProjectMemberDetailsRepository detailsRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Cacheable(
            cacheNames = ProjectMemberCacheNames.PROJECT_MEMBERS_BY_PROJECT_ID,
            key = "#projectId + ':' + #accountId + ':' + #pagination.page + ':' + #pagination.size"
    )
    public PageResult<ProjectMemberDetails> findAllByProjectId(
            String projectId,
            String accountId,
            Pagination pagination
    ) {
        ensureProjectExists(projectId);
        ensureCanRead(projectId, accountId);

        return detailsRepository.findAllByProjectId(projectId, pagination);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = ProjectMemberCacheNames.PROJECT_MEMBERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ProjectCacheNames.PROJECT_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ProjectCacheNames.PROJECT_BY_ACCOUNT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = AuthConfigCacheNames.AUTH_CONFIG_BY_PROJECT_AND_ACCOUNT,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = PasswordConfigCacheNames.PASSWORD_CONFIG_BY_PROJECT_AND_ACCOUNT,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = UserCacheNames.USER_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = UserCacheNames.USERS_BY_PROJECT_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ProjectApiKeyCacheNames.PROJECT_API_KEY_BY_ID,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = ProjectApiKeyCacheNames.PROJECT_API_KEYS_BY_PROJECT_ID,
                    allEntries = true
            )
    })
    public void delete(String projectId, String targetAccountId, String authenticatedAccountId) {
        ensureProjectExists(projectId);

        ProjectMember requester = findRequesterOrThrow(projectId, authenticatedAccountId);
        ensureCanDeleteMembers(requester);

        ProjectMember target = findMemberOrThrow(projectId, targetAccountId);
        ensureCanDeleteTarget(requester, target);

        projectMemberRepository.deleteByProjectIdAndAccountId(projectId, targetAccountId);
    }

    private void ensureProjectExists(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("Projeto não encontrado: " + projectId);
        }
    }

    private void ensureCanRead(String projectId, String accountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountId(projectId, accountId)) {
            throw new ProjectAccessDeniedException();
        }
    }

    private ProjectMember findMemberOrThrow(String projectId, String accountId) {
        return projectMemberRepository.findByProjectIdAndAccountId(projectId, accountId)
                .orElseThrow(ProjectMemberNotFoundException::new);
    }

    private ProjectMember findRequesterOrThrow(String projectId, String accountId) {
        return projectMemberRepository.findByProjectIdAndAccountId(projectId, accountId)
                .orElseThrow(ProjectAccessDeniedException::new);
    }

    private void ensureCanDeleteMembers(ProjectMember requester) {
        if (!PROJECT_MEMBER_MANAGEMENT_ROLES.contains(requester.getRole())) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensureCanDeleteTarget(ProjectMember requester, ProjectMember target) {
        if (ProjectMemberRole.OWNER.equals(target.getRole())) {
            throw new ProjectAccessDeniedException("O owner do projeto não pode ser removido dos membros.");
        }

        if (
                ProjectMemberRole.ADMIN.equals(target.getRole())
                        && !ProjectMemberRole.OWNER.equals(requester.getRole())
        ) {
            throw new ProjectAccessDeniedException("Apenas o owner pode deletar um membro admin.");
        }
    }
}
