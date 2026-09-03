package com.samuelmaia1_github.yourauth.domain.usersession;

import com.samuelmaia1_github.yourauth.domain.user.User;

public record UserSessionDetails(
        UserSession session,
        User user
) {
}
