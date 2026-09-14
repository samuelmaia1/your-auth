package com.samuelmaia1_github.yourauth.domain.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialIdentity {
    private String id;
    private String accountId;
    private SocialProvider provider;
    private String providerUserId;
    private String providerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
