package com.samuelmaia1_github.yourauth.domain.project;

import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.account.exceptions.AccountNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectNotFoundException;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfigRepository;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final List<ProjectMemberRole> PROJECT_MANAGEMENT_ROLES = List.of(
            ProjectMemberRole.OWNER,
            ProjectMemberRole.ADMIN
    );

    private final ProjectRepository repository;
    private final PasswordConfigRepository passwordConfigRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AccountRepository accountRepository;
    private final ProjectPolicy policy;

    @Transactional
    public Project create(Project project, PasswordConfig requestedPasswordConfig) {
        accountRepository.findById(project.getOwnerAccountId())
                .orElseThrow(AccountNotFoundException::new);

        policy.ensureCanCreate(project);

        Project createdProject = repository.save(project);

        projectMemberRepository.save(ProjectMember.builder()
                .projectId(createdProject.getId())
                .accountId(createdProject.getOwnerAccountId())
                .role(ProjectMemberRole.OWNER)
                .build());

        PasswordConfig passwordConfig =
                requestedPasswordConfig == null ? PasswordConfig.createDefault() : requestedPasswordConfig;

        passwordConfig.assignToProject(createdProject.getId());

        passwordConfigRepository.save(passwordConfig);

        return createdProject;
    }

    public Project findById(String id, String accountId) {
        Project project = findProjectOrThrow(id);
        ensureCanRead(project.getId(), accountId);

        return project;
    }

    public PageResult<Project> findAllByAccountId(String accountId, Pagination pagination) {
        return repository.findAllByMemberAccountId(accountId, pagination);
    }

    @Transactional
    public Project update(String id, Project project, String accountId) {
        Project currentProject = findProjectOrThrow(id);
        ensureCanManage(currentProject.getId(), accountId);

        Project updatedProject = Project.builder()
                .id(currentProject.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerAccountId(currentProject.getOwnerAccountId())
                .status(project.getStatus())
                .environment(project.getEnvironment())
                .tokenAudience(project.getTokenAudience())
                .createdAt(currentProject.getCreatedAt())
                .updatedAt(currentProject.getUpdatedAt())
                .build();

        policy.ensureCanUpdate(updatedProject);

        return repository.save(updatedProject);
    }

    @Transactional
    public void delete(String id, String accountId) {
        Project project = findProjectOrThrow(id);
        ensureCanManage(project.getId(), accountId);

        projectMemberRepository.deleteAllByProjectId(project.getId());
        repository.deleteById(project.getId());
    }

    private Project findProjectOrThrow(String id) {
        return repository.findById(id).orElseThrow(ProjectNotFoundException::new);
    }

    private void ensureCanRead(String projectId, String accountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountId(projectId, accountId)) {
            throw new ProjectAccessDeniedException();
        }
    }

    private void ensureCanManage(String projectId, String accountId) {
        if (!projectMemberRepository.existsByProjectIdAndAccountIdAndRoleIn(
                projectId,
                accountId,
                PROJECT_MANAGEMENT_ROLES
        )) {
            throw new ProjectAccessDeniedException();
        }
    }
}
