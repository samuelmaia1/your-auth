package com.samuelmaia1_github.yourauth.infra.config;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountProjectSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountSummary;
import com.samuelmaia1_github.yourauth.domain.account.AccountUsage;
import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanFeature;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimit;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitPeriod;
import com.samuelmaia1_github.yourauth.domain.project.Project;
import com.samuelmaia1_github.yourauth.domain.project.ProjectEnvironment;
import com.samuelmaia1_github.yourauth.domain.project.ProjectStatus;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.SessionMode;
import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKey;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyDetails;
import com.samuelmaia1_github.yourauth.domain.projectapikey.ProjectApiKeyScope;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberDetails;
import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import com.samuelmaia1_github.yourauth.domain.shared.PageResult;
import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.BillingCycle;
import com.samuelmaia1_github.yourauth.domain.subscription.SubscriptionStatus;
import com.samuelmaia1_github.yourauth.domain.user.User;
import com.samuelmaia1_github.yourauth.domain.user.UserStatus;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.cache.names.AccountCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.PlanCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.ProjectApiKeyCacheNames;
import com.samuelmaia1_github.yourauth.infra.cache.names.UserCacheNames;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void shouldConfigureCustomCacheTtls() {
        RedisCacheConfiguration defaultConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> configurations =
                new CacheConfig().customCacheConfigurations(defaultConfiguration);

        assertThat(ttl(configurations, PlanCacheNames.PLANS_CACHE))
                .isEqualTo(Duration.ofHours(24));
        assertThat(ttl(configurations, AccountCacheNames.ACCOUNT_SUMMARY_BY_ACCOUNT_ID))
                .isEqualTo(Duration.ofMinutes(1));
        assertThat(ttl(configurations, AccountCacheNames.ACCOUNT_USAGE_BY_OWNER_ACCOUNT_ID))
                .isEqualTo(Duration.ofMinutes(1));
        assertThat(ttl(configurations, UserCacheNames.USER_BY_ID))
                .isEqualTo(Duration.ofMinutes(2));
        assertThat(ttl(configurations, UserCacheNames.USERS_BY_PROJECT_ID))
                .isEqualTo(Duration.ofMinutes(2));
        assertThat(ttl(configurations, ProjectApiKeyCacheNames.PROJECT_API_KEY_BY_ID))
                .isEqualTo(Duration.ofMinutes(2));
        assertThat(ttl(configurations, ProjectApiKeyCacheNames.PROJECT_API_KEYS_BY_PROJECT_ID))
                .isEqualTo(Duration.ofMinutes(2));
    }

    @Test
    void shouldSerializeAndDeserializePlanListAsObject() {
        RedisSerializer<Object> serializer =
                new CacheConfig().valueSerializer();

        List<Plan> plans = List.of(Plan.builder()
                .id("plan-id")
                .code(PlanCode.FREE)
                .name("Free")
                .description("Plano gratuito")
                .active(true)
                .displayOrder(1)
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .features(List.of(PlanFeature.builder()
                        .id("feature-id")
                        .planId("plan-id")
                        .code("feature")
                        .description("Feature")
                        .enabled(true)
                        .build()))
                .limits(List.of(PlanLimit.builder()
                        .id("limit-id")
                        .planId("plan-id")
                        .code("MAX_PROJECTS")
                        .value(1L)
                        .unit("COUNT")
                        .period(PlanLimitPeriod.NONE)
                        .build()))
                .build());

        byte[] serializedPlans = serializer.serialize(plans);
        Object deserializedPlans = serializer.deserialize(serializedPlans);

        assertThat(deserializedPlans).isInstanceOf(List.class);

        List<?> planList = (List<?>) deserializedPlans;
        assertThat(planList).hasSize(1);
        assertThat(planList.getFirst()).isInstanceOf(Plan.class);

        Plan plan = (Plan) planList.getFirst();
        assertThat(plan.getCode()).isEqualTo(PlanCode.FREE);
        assertThat(plan.getName()).isEqualTo("Free");
        assertThat(plan.getCreatedAt())
                .isEqualTo(LocalDateTime.parse("2026-01-01T10:00:00"));
        assertThat(plan.getFeatures()).hasSize(1);
        assertThat(plan.getFeatures().getFirst().getCode())
                .isEqualTo("feature");
        assertThat(plan.getLimits()).hasSize(1);
        assertThat(plan.getLimits().getFirst().getCode())
                .isEqualTo("MAX_PROJECTS");
    }

    @Test
    void shouldSerializeAndDeserializeRecordAsObject() {
        RedisSerializer<Object> serializer =
                new CacheConfig().valueSerializer();

        CachedPlanResponse response =
                new CachedPlanResponse("plan-id", "Free");

        byte[] serializedResponse = serializer.serialize(response);
        Object deserializedResponse = serializer.deserialize(serializedResponse);

        assertThat(deserializedResponse).isEqualTo(response);
    }

    @Test
    void shouldSerializeAndDeserializeProjectPageResultAsObject() {
        RedisSerializer<Object> serializer =
                new CacheConfig().valueSerializer();

        PageResult<Project> projects = new PageResult<>(
                List.of(project()),
                0,
                10,
                1,
                1
        );

        byte[] serializedProjects = serializer.serialize(projects);
        Object deserializedProjects = serializer.deserialize(serializedProjects);

        assertThat(deserializedProjects).isInstanceOf(PageResult.class);

        PageResult<?> page = (PageResult<?>) deserializedProjects;
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst()).isInstanceOf(Project.class);

        Project project = (Project) page.content().getFirst();
        assertThat(project.getId()).isEqualTo("project-id");
        assertThat(project.getName()).isEqualTo("Auth API");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(project.getEnvironment())
                .isEqualTo(ProjectEnvironment.PRODUCTION);
    }

    @Test
    void shouldSerializeAndDeserializePasswordConfigAsObject() {
        PasswordConfig passwordConfig =
                roundTrip(passwordConfig(), PasswordConfig.class);

        assertThat(passwordConfig.getProjectId()).isEqualTo("project-id");
        assertThat(passwordConfig.getMinSize()).isEqualTo(8);
        assertThat(passwordConfig.isUppercaseRequired()).isTrue();
    }

    @Test
    void shouldSerializeAndDeserializeAuthConfigAsObject() {
        AuthConfig authConfig =
                roundTrip(authConfig(), AuthConfig.class);

        assertThat(authConfig.getProjectId()).isEqualTo("project-id");
        assertThat(authConfig.getSessionMode())
                .isEqualTo(SessionMode.LIMITED_ACTIVE_SESSIONS);
        assertThat(authConfig.getMaxActiveSessions()).isEqualTo(3);
    }

    @Test
    void shouldSerializeAndDeserializeUserAsObject() {
        User user = roundTrip(user(), User.class);

        assertThat(user.getId()).isEqualTo("user-id");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getPhone().getDdd()).isEqualTo("11");
    }

    @Test
    void shouldSerializeAndDeserializeUserPageResultAsObject() {
        PageResult<?> page = roundTrip(
                new PageResult<>(List.of(user()), 0, 10, 1, 1),
                PageResult.class
        );

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst()).isInstanceOf(User.class);
    }

    @Test
    void shouldSerializeAndDeserializeProjectApiKeyDetailsAsObject() {
        ProjectApiKeyDetails details =
                roundTrip(projectApiKeyDetails(), ProjectApiKeyDetails.class);

        assertThat(details.apiKey().getId()).isEqualTo("api-key-id");
        assertThat(details.apiKey().getScopes())
                .contains(ProjectApiKeyScope.USERS_READ);
        assertThat(details.createdByAccount().getId()).isEqualTo("account-id");
        assertThat(details.createdByAccount().getCPF().getValue())
                .isEqualTo("12345678901");
    }

    @Test
    void shouldSerializeAndDeserializeProjectApiKeyDetailsPageResultAsObject() {
        PageResult<?> page = roundTrip(
                new PageResult<>(
                        List.of(projectApiKeyDetails()),
                        0,
                        10,
                        1,
                        1
                ),
                PageResult.class
        );

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst())
                .isInstanceOf(ProjectApiKeyDetails.class);
    }

    @Test
    void shouldSerializeAndDeserializeAccountSubscriptionAsObject() {
        AccountSubscription subscription =
                roundTrip(accountSubscription(), AccountSubscription.class);

        assertThat(subscription.getAccountId()).isEqualTo("account-id");
        assertThat(subscription.getStatus())
                .isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getBillingCycle())
                .isEqualTo(BillingCycle.MONTHLY);
        assertThat(subscription.getPlan().getCode()).isEqualTo(PlanCode.PRO);
    }

    @Test
    void shouldSerializeAndDeserializeAccountSummaryAsObject() {
        AccountSummary summary =
                roundTrip(accountSummary(), AccountSummary.class);

        assertThat(summary.projects()).hasSize(1);
        assertThat(summary.totalProjects()).isEqualTo(1);
        assertThat(summary.totalUsers()).isEqualTo(20);
        assertThat(summary.totalActiveSessions()).isEqualTo(7);
    }

    @Test
    void shouldSerializeAndDeserializeAccountUsageAsObject() {
        AccountUsage usage =
                roundTrip(new AccountUsage(2, 20, 7), AccountUsage.class);

        assertThat(usage.totalProjects()).isEqualTo(2);
        assertThat(usage.totalUsers()).isEqualTo(20);
        assertThat(usage.totalActiveSessions()).isEqualTo(7);
    }

    @Test
    void shouldSerializeAndDeserializeProjectMemberDetailsPageResultAsObject() {
        PageResult<?> page = roundTrip(
                new PageResult<>(
                        List.of(new ProjectMemberDetails(
                                "Samuel",
                                "Maia",
                                ProjectMemberRole.OWNER,
                                LocalDateTime.parse("2026-01-01T10:00:00")
                        )),
                        0,
                        10,
                        1,
                        1
                ),
                PageResult.class
        );

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst())
                .isInstanceOf(ProjectMemberDetails.class);
    }

    private record CachedPlanResponse(String id, String name) {
    }

    private <T> T roundTrip(Object value, Class<T> type) {
        Object result = roundTrip(value);

        assertThat(result).isInstanceOf(type);

        return type.cast(result);
    }

    private Object roundTrip(Object value) {
        RedisSerializer<Object> serializer =
                new CacheConfig().valueSerializer();

        byte[] serializedValue = serializer.serialize(value);

        return serializer.deserialize(serializedValue);
    }

    private Project project() {
        return Project.builder()
                .id("project-id")
                .name("Auth API")
                .description("Projeto de autenticacao")
                .ownerAccountId("account-id")
                .status(ProjectStatus.ACTIVE)
                .environment(ProjectEnvironment.PRODUCTION)
                .tokenAudience("project-id")
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .build();
    }

    private PasswordConfig passwordConfig() {
        return PasswordConfig.builder()
                .id("password-config-id")
                .projectId("project-id")
                .numberRequired(true)
                .specialCharRequired(true)
                .uppercaseRequired(true)
                .lowercaseRequired(true)
                .minSize(8)
                .maxSize(64)
                .build();
    }

    private AuthConfig authConfig() {
        return AuthConfig.builder()
                .id("auth-config-id")
                .projectId("project-id")
                .accessTokenExpirationMinutes(15)
                .refreshTokenExpirationDays(7)
                .sessionMode(SessionMode.LIMITED_ACTIVE_SESSIONS)
                .maxActiveSessions(3)
                .refreshTokenRotationEnabled(true)
                .revokeTokensOnPasswordChange(true)
                .failedLoginAttemptsLimit(5)
                .lockDurationMinutes(15)
                .requireEmailVerification(false)
                .registrationEnabled(true)
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .build();
    }

    private User user() {
        return User.builder()
                .id("user-id")
                .projectId("project-id")
                .email("user@example.com")
                .password("hashed-password")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .phone(Phone.builder()
                        .ddd("11")
                        .number("999999999")
                        .build())
                .build();
    }

    private ProjectApiKeyDetails projectApiKeyDetails() {
        return new ProjectApiKeyDetails(projectApiKey(), account());
    }

    private ProjectApiKey projectApiKey() {
        return ProjectApiKey.builder()
                .id("api-key-id")
                .projectId("project-id")
                .name("Backend")
                .keyId("key-id")
                .prefix("ya_live")
                .secretHash("hashed-secret")
                .secretLastFour("1234")
                .environment("production")
                .scopes(Set.of(ProjectApiKeyScope.USERS_READ))
                .createdByAccountId("account-id")
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .build();
    }

    private Account account() {
        return Account.builder()
                .id("account-id")
                .name("Samuel")
                .lastName("Maia")
                .email("samuel@example.com")
                .password("hashed-password")
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .phone(Phone.builder()
                        .ddd("11")
                        .number("988888888")
                        .build())
                .CPF(new CPF("12345678901"))
                .build();
    }

    private AccountSubscription accountSubscription() {
        return AccountSubscription.builder()
                .id("subscription-id")
                .accountId("account-id")
                .planId("pro")
                .plan(Plan.builder()
                        .id("pro")
                        .code(PlanCode.PRO)
                        .name("Pro")
                        .active(true)
                        .displayOrder(3)
                        .features(List.of())
                        .limits(List.of())
                        .build())
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.MONTHLY)
                .currentPeriodStart(
                        LocalDateTime.parse("2026-01-01T10:00:00")
                )
                .currentPeriodEnd(
                        LocalDateTime.parse("2026-02-01T10:00:00")
                )
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .build();
    }

    private AccountSummary accountSummary() {
        return new AccountSummary(List.of(new AccountProjectSummary(
                "project-id",
                "Auth API",
                "Projeto de autenticacao",
                "account-id",
                ProjectStatus.ACTIVE,
                ProjectEnvironment.PRODUCTION,
                "project-id",
                LocalDateTime.parse("2026-01-01T10:00:00"),
                LocalDateTime.parse("2026-01-02T10:00:00"),
                ProjectMemberRole.OWNER,
                20,
                7
        )));
    }

    private Duration ttl(
            Map<String, RedisCacheConfiguration> configurations,
            String cacheName
    ) {
        return configurations
                .get(cacheName)
                .getTtlFunction()
                .getTimeToLive("key", "value");
    }
}
