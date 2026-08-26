package com.samuelmaia1_github.yourauth.domain.refreshtoken;

import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfig;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.AuthConfigRepository;
import com.samuelmaia1_github.yourauth.domain.project.authconfig.exceptions.AuthConfigNotFoundException;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.TokenDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.auth.user.UserRefreshResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRefreshTokenService {
    private final RefreshTokenHasher hasher;
    private final RefreshTokenGenerator generator;
    private final UserRefreshTokenRepository repository;
    private final AuthConfigRepository authConfigRepository;

    public TokenDTO createUserRefreshToken(String projectId, String userId, String userAgent) {
        AuthConfig auth = authConfigRepository.findByProjectId(projectId)
                .orElseThrow(AuthConfigNotFoundException::new);

        String raw = generator.generate();

        UserRefreshToken refreshToken = UserRefreshToken
                .builder()
                .userId(userId)
                .projectId(projectId)
                .expiresAt(getExpiration(auth.getRefreshTokenExpirationDays()))
                .familyId(UUID.randomUUID().toString())
                .userAgent(userAgent)
                .hash(hasher.hash(raw))
                .build();

        repository.save(refreshToken);

        return new TokenDTO(raw, Duration.ofDays(auth.getRefreshTokenExpirationDays()));
    }

    public UserRefreshResponseDTO refresh(String currentRawToken) {
        throw new UnsupportedOperationException("UserRefreshTokenService ainda nao implementado.");
    }

    public UserRefreshToken getToken(String hash) {
        throw new UnsupportedOperationException("UserRefreshTokenService ainda nao implementado.");
    }

    public void revokeFamily(String familyId) {
    }

    private Instant getExpiration(Integer days) {
        Duration duration = Duration.ofDays(days);
        return Instant.now().plus(duration);
    }
}
