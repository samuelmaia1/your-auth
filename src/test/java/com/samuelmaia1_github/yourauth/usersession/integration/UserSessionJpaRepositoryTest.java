package com.samuelmaia1_github.yourauth.usersession.integration;

import com.samuelmaia1_github.yourauth.infra.repository.UserSessionJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:user_session_jpa_repository_test")
class UserSessionJpaRepositoryTest {
    @Autowired
    private UserSessionJpaRepository repository;

    @Test
    void shouldExecuteDetailsQueryWithStatusFilters() {
        assertThatCode(() -> repository.findAllDetailsByProjectId(
                "project-id",
                true,
                false,
                null,
                null,
                null,
                PageRequest.of(0, 20)
        )).doesNotThrowAnyException();

        assertThatCode(() -> repository.findAllDetailsByProjectId(
                "project-id",
                false,
                true,
                null,
                null,
                null,
                PageRequest.of(0, 20)
        )).doesNotThrowAnyException();
    }
}
