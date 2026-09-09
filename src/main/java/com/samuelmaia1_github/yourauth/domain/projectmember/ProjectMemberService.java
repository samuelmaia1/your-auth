package com.samuelmaia1_github.yourauth.domain.projectmember;

import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.infra.cache.names.ProjectMemberCacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
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
}
