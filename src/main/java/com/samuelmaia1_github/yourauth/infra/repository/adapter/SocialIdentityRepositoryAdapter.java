package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.social.SocialIdentity;
import com.samuelmaia1_github.yourauth.domain.social.SocialIdentityRepository;
import com.samuelmaia1_github.yourauth.domain.social.SocialProvider;
import com.samuelmaia1_github.yourauth.domain.social.exceptions.SocialIdentityConflictException;
import com.samuelmaia1_github.yourauth.infra.mappers.SocialIdentityMapper;
import com.samuelmaia1_github.yourauth.infra.repository.SocialIdentityJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.fingerprint;
import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.maskEmail;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialIdentityRepositoryAdapter implements SocialIdentityRepository {
    private final SocialIdentityJpaRepository repository;

    @Override
    public SocialIdentity save(SocialIdentity identity) {
        try {
            log.debug(
                    "Persistindo identidade social: accountId={}, provider={}, providerUserId={}, providerEmail={}",
                    identity.getAccountId(),
                    identity.getProvider(),
                    fingerprint(identity.getProviderUserId()),
                    maskEmail(identity.getProviderEmail())
            );
            SocialIdentity savedIdentity = SocialIdentityMapper.toDomain(
                    repository.saveAndFlush(SocialIdentityMapper.toEntity(identity))
            );
            log.debug(
                    "Identidade social persistida: identityId={}, accountId={}, provider={}, providerUserId={}",
                    savedIdentity.getId(),
                    savedIdentity.getAccountId(),
                    savedIdentity.getProvider(),
                    fingerprint(savedIdentity.getProviderUserId())
            );
            return savedIdentity;
        } catch (DataIntegrityViolationException exception) {
            log.warn(
                    "Conflito de integridade ao persistir identidade social: accountId={}, provider={}, providerUserId={}",
                    identity.getAccountId(),
                    identity.getProvider(),
                    fingerprint(identity.getProviderUserId())
            );
            throw new SocialIdentityConflictException();
        }
    }

    @Override
    public Optional<SocialIdentity> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    ) {
        Optional<SocialIdentity> identity = repository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(SocialIdentityMapper::toDomain);
        log.debug(
                "Busca de identidade social por provider/userId: provider={}, providerUserId={}, found={}",
                provider,
                fingerprint(providerUserId),
                identity.isPresent()
        );
        return identity;
    }

    @Override
    public Optional<SocialIdentity> findByAccountIdAndProvider(String accountId, SocialProvider provider) {
        Optional<SocialIdentity> identity = repository.findByAccountIdAndProvider(accountId, provider)
                .map(SocialIdentityMapper::toDomain);
        log.debug(
                "Busca de identidade social por conta/provider: accountId={}, provider={}, found={}",
                accountId,
                provider,
                identity.isPresent()
        );
        return identity;
    }

    @Override
    public boolean existsByAccountId(String accountId) {
        boolean exists = repository.existsByAccountId(accountId);
        log.debug("Verificacao de identidade social por conta: accountId={}, exists={}", accountId, exists);
        return exists;
    }
}
