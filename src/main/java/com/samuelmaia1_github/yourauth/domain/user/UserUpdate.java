package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.domain.shared.Phone;

public record UserUpdate(
        String email,
        boolean emailProvided,
        String password,
        boolean passwordProvided,
        Phone phone,
        boolean phoneProvided
) {
}
