package com.samuelmaia1_github.yourauth.projectapikey.unit;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountRepository;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAccessDeniedException;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyDetails;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyFilter;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyRepository;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyService;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMember;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRepository;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectApiKeyServiceTest {
    private static final String PROJECT_ID = "project-id";
    private static final String ACCOUNT_ID = "account-id";
    private static final ProjectApiKeyFilter EMPTY_FILTER = new ProjectApiKeyFilter(null);

    @Test
    void shouldListApiKeysWithCreatedByAccountData() {
        RecordingProjectApiKeyRepository apiKeyRepository = new RecordingProjectApiKeyRepository();
        RecordingAccountRepository accountRepository = new RecordingAccountRepository();
        ProjectApiKeyService service = new ProjectApiKeyService(
                apiKeyRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true),
                accountRepository,
                null,
                null
        );
        Pagination pagination = new Pagination(0, 20);

        PageResult<ProjectApiKeyDetails> apiKeys = service.findAllByProjectId(
                PROJECT_ID,
                ACCOUNT_ID,
                pagination,
                EMPTY_FILTER
        );

        assertThat(apiKeys.content()).hasSize(1);
        assertThat(apiKeys.content().getFirst().apiKey().getId()).isEqualTo("api-key-id");
        assertThat(apiKeys.content().getFirst().createdByAccount().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(accountRepository.accountId).isEqualTo(ACCOUNT_ID);
        assertThat(apiKeyRepository.projectId).isEqualTo(PROJECT_ID);
        assertThat(apiKeyRepository.pagination).isSameAs(pagination);
        assertThat(apiKeyRepository.filter).isSameAs(EMPTY_FILTER);
    }

    @Test
    void shouldListApiKeysWithCreatedByFilter() {
        RecordingProjectApiKeyRepository apiKeyRepository = new RecordingProjectApiKeyRepository();
        ProjectApiKeyService service = new ProjectApiKeyService(
                apiKeyRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(true),
                new RecordingAccountRepository(),
                null,
                null
        );
        Pagination pagination = new Pagination(0, 20);
        ProjectApiKeyFilter filter = new ProjectApiKeyFilter(" owner@email.com ");

        service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, pagination, filter);

        assertThat(apiKeyRepository.pagination).isSameAs(pagination);
        assertThat(apiKeyRepository.filter).isSameAs(filter);
        assertThat(apiKeyRepository.filter.createdBy()).isEqualTo("owner@email.com");
    }

    @Test
    void shouldDenyListApiKeysWhenAuthenticatedAccountIsNotProjectMember() {
        RecordingProjectApiKeyRepository apiKeyRepository = new RecordingProjectApiKeyRepository();
        ProjectApiKeyService service = new ProjectApiKeyService(
                apiKeyRepository,
                new StubProjectRepository(true),
                new RecordingProjectMemberRepository(false),
                new RecordingAccountRepository(),
                null,
                null
        );

        assertThatThrownBy(() -> service.findAllByProjectId(PROJECT_ID, ACCOUNT_ID, new Pagination(0, 20), EMPTY_FILTER))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessage("A conta autenticada não tem permissão para acessar este projeto.");

        assertThat(apiKeyRepository.projectId).isNull();
    }

    private static class RecordingProjectApiKeyRepository implements ProjectApiKeyRepository {
        private String projectId;
        private Pagination pagination;
        private ProjectApiKeyFilter filter;

        @Override
        public ProjectApiKey save(ProjectApiKey apiKey) {
            return apiKey;
        }

        @Override
        public Optional<ProjectApiKey> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<ProjectApiKey> findByProjectIdAndId(String projectId, String id) {
            return Optional.empty();
        }

        @Override
        public Optional<ProjectApiKey> findByKeyId(String keyId) {
            return Optional.empty();
        }

        @Override
        public Optional<ProjectApiKey> findByPrefix(String prefix) {
            return Optional.empty();
        }

        @Override
        public PageResult<ProjectApiKey> findAllByProjectId(
                String projectId,
                Pagination pagination,
                ProjectApiKeyFilter filter
        ) {
            this.projectId = projectId;
            this.pagination = pagination;
            this.filter = filter;

            return new PageResult<>(
                    List.of(ProjectApiKey.builder()
                            .id("api-key-id")
                            .projectId(projectId)
                            .name("API Key")
                            .createdByAccountId(ACCOUNT_ID)
                            .build()),
                    pagination.page(),
                    pagination.size(),
                    1,
                    1
            );
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class RecordingAccountRepository implements AccountRepository {
        private String accountId;

        @Override
        public Account save(Account account) {
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            accountId = id;
            return Optional.of(Account.builder()
                    .id(id)
                    .name("Owner")
                    .lastName("Account")
                    .email("owner@email.com")
                    .build());
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByCPF(CPF cpf) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findByEmailIgnoreCaseOrCPF(String email, CPF cpf) {
            return Optional.empty();
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class StubProjectRepository implements ProjectRepository {
        private final boolean exists;

        private StubProjectRepository(boolean exists) {
            this.exists = exists;
        }

        @Override
        public Project save(Project project) {
            return project;
        }

        @Override
        public Optional<Project> findById(String id) {
            return exists ? Optional.of(Project.builder().id(id).build()) : Optional.empty();
        }

        @Override
        public boolean existsByOwnerAccountIdAndName(String ownerAccountId, String name) {
            return false;
        }

        @Override
        public boolean existsByOwnerAccountIdAndNameAndIdNot(String ownerAccountId, String name, String id) {
            return false;
        }

        @Override
        public PageResult<Project> findAllByOwnerAccountId(String id, Pagination pagination) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public PageResult<Project> findAllByMemberAccountId(String accountId, Pagination pagination) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0, 0);
        }

        @Override
        public void deleteById(String id) {
        }
    }

    private static class RecordingProjectMemberRepository implements ProjectMemberRepository {
        private final boolean member;

        private RecordingProjectMemberRepository(boolean member) {
            this.member = member;
        }

        @Override
        public ProjectMember save(ProjectMember projectMember) {
            return projectMember;
        }

        @Override
        public Optional<ProjectMember> findByProjectIdAndAccountId(String projectId, String accountId) {
            return Optional.empty();
        }

        @Override
        public boolean existsByProjectIdAndAccountId(String projectId, String accountId) {
            return member;
        }

        @Override
        public boolean existsByProjectIdAndAccountIdAndRoleIn(
                String projectId,
                String accountId,
                Collection<ProjectMemberRole> roles
        ) {
            return member;
        }

        @Override
        public void deleteAllByProjectId(String projectId) {
        }
    }
}
