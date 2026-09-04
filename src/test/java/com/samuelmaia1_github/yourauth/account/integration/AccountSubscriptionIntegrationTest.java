package com.samuelmaia1_github.yourauth.account.integration;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.shared.Address;
import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscription;
import com.samuelmaia1_github.yourauth.domain.subscription.AccountSubscriptionService;
import com.samuelmaia1_github.yourauth.domain.subscription.BillingCycle;
import com.samuelmaia1_github.yourauth.domain.subscription.SubscriptionStatus;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:account_subscription_integration_test")
class AccountSubscriptionIntegrationTest {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountSubscriptionService subscriptionService;

    @Test
    void shouldCreateFreeSubscriptionWhenAccountIsCreated() {
        Account createdAccount = accountService.create(account());

        AccountSubscription subscription = subscriptionService.findCurrentByAccountId(createdAccount.getId());

        assertThat(subscription.getAccountId()).isEqualTo(createdAccount.getId());
        assertThat(subscription.getPlan().getCode()).isEqualTo(PlanCode.FREE);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.NONE);
    }

    private Account account() {
        return Account.builder()
                .name("Samuel")
                .lastName("Maia")
                .email("samuel@example.com")
                .password("RawPassword1")
                .CPF(new CPF("12345678909"))
                .address(Address.builder()
                        .cep("01001000")
                        .street("Rua Teste")
                        .neighborhood("Centro")
                        .city("Sao Paulo")
                        .state("SP")
                        .number("100")
                        .build())
                .phone(Phone.builder()
                        .ddd("11")
                        .number("999999999")
                        .build())
                .build();
    }
}
