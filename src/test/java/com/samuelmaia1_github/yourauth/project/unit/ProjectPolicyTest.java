package com.samuelmaia1_github.yourauth.project.unit;

import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectPolicy;
import com.samuelmaia1_github.yourauth.domain.project.ProjectRepository;
import com.samuelmaia1_github.yourauth.domain.project.exceptions.ProjectAlreadyExistsException;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Pagination;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProjectPolicyTest {
    @Test
    void shouldAllowCreateProjectWhenOwnerDoesNotHaveProjectWithSameName() {
        Project project = project();
        ProjectPolicy policy = new ProjectPolicy(new StubProjectRepository(false, false));

        assertThatNoException().isThrownBy(() -> policy.ensureCanCreate(project));
    }

    @Test
    void shouldNotAllowCreateProjectWhenOwnerAlreadyHasProjectWithSameName() {
        Project project = project();
        ProjectPolicy policy = new ProjectPolicy(new StubProjectRepository(true, false));

        assertThatThrownBy(() -> policy.ensureCanCreate(project))
                .isInstanceOf(ProjectAlreadyExistsException.class)
                .hasMessage("Projeto já cadastrado para esta conta");
    }

    @Test
    void shouldNotAllowUpdateProjectWhenOwnerAlreadyHasAnotherProjectWithSameName() {
        Project project = Project.builder()
                .id("project-id")
                .ownerAccountId("account-id")
                .name("My Project")
                .build();
        ProjectPolicy policy = new ProjectPolicy(new StubProjectRepository(false, true));

        assertThatThrownBy(() -> policy.ensureCanUpdate(project))
                .isInstanceOf(ProjectAlreadyExistsException.class)
                .hasMessage("Projeto já cadastrado para esta conta");
    }

    private Project project() {
        return Project.builder()
                .ownerAccountId("account-id")
                .name("My Project")
                .build();
    }

    private static class StubProjectRepository implements ProjectRepository {
        private final boolean existsByOwnerAccountIdAndName;
        private final boolean existsByOwnerAccountIdAndNameAndIdNot;

        private StubProjectRepository(boolean existsByOwnerAccountIdAndName, boolean existsByOwnerAccountIdAndNameAndIdNot) {
            this.existsByOwnerAccountIdAndName = existsByOwnerAccountIdAndName;
            this.existsByOwnerAccountIdAndNameAndIdNot = existsByOwnerAccountIdAndNameAndIdNot;
        }

        @Override
        public Project save(Project project) {
            return project;
        }

        @Override
        public Optional<Project> findById(String id) {
            return Optional.empty();
        }

        @Override
        public boolean existsByOwnerAccountIdAndName(String ownerAccountId, String name) {
            return existsByOwnerAccountIdAndName;
        }

        @Override
        public boolean existsByOwnerAccountIdAndNameAndIdNot(String ownerAccountId, String name, String id) {
            return existsByOwnerAccountIdAndNameAndIdNot;
        }

        @Override
        public PageResult<Project> findAllByOwnerAccountId(String id, Pagination pagination) {
            return null;
        }

        @Override
        public PageResult<Project> findAllByMemberAccountId(String accountId, Pagination pagination) {
            return null;
        }

        @Override
        public void deleteById(String id) {
        }
    }
}
