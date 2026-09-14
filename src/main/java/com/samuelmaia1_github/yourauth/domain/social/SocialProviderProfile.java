package com.samuelmaia1_github.yourauth.domain.social;

public record SocialProviderProfile(
        SocialProvider provider,
        String providerUserId,
        String email,
        boolean emailVerified,
        String name,
        String lastName,
        String avatarUrl
) {
    public SocialProviderProfile withEmail(String email) {
        return new SocialProviderProfile(
                provider,
                providerUserId,
                email,
                emailVerified,
                name,
                lastName,
                avatarUrl
        );
    }
}
