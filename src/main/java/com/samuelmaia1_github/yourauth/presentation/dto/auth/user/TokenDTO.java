package com.samuelmaia1_github.yourauth.presentation.dto.auth.user;

import java.time.Duration;

public record TokenDTO(
        String raw,
        Duration duration
) {
}
