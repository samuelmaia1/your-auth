package com.samuelmaia1_github.yourauth.infra.repository.adapter;

import com.samuelmaia1_github.yourauth.domain.social.SocialLoginCode;
import com.samuelmaia1_github.yourauth.domain.social.SocialLoginCodeRepository;
import com.samuelmaia1_github.yourauth.infra.mappers.SocialLoginCodeMapper;
import com.samuelmaia1_github.yourauth.infra.repository.SocialLoginCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.samuelmaia1_github.yourauth.domain.shared.SafeLog.fingerprint;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialLoginCodeRepositoryAdapter implements SocialLoginCodeRepository {
    private final SocialLoginCodeJpaRepository repository;

    @Override
    public SocialLoginCode save(SocialLoginCode code) {
        log.debug(
                "Persistindo codigo social: accountId={}, codeFingerprint={}, expiresAt={}",
                code.getAccountId(),
                fingerprint(code.getHash()),
                code.getExpiresAt()
        );
        SocialLoginCode savedCode = SocialLoginCodeMapper.toDomain(repository.save(SocialLoginCodeMapper.toEntity(code)));
        log.debug(
                "Codigo social persistido: codeId={}, accountId={}, codeFingerprint={}, expiresAt={}",
                savedCode.getId(),
                savedCode.getAccountId(),
                fingerprint(savedCode.getHash()),
                savedCode.getExpiresAt()
        );
        return savedCode;
    }

    @Override
    public Optional<SocialLoginCode> findByHash(String hash) {
        Optional<SocialLoginCode> code = repository.findByHash(hash).map(SocialLoginCodeMapper::toDomain);
        log.debug(
                "Busca de codigo social por hash: codeFingerprint={}, found={}",
                fingerprint(hash),
                code.isPresent()
        );
        return code;
    }

    @Override
    @Transactional
    public Optional<SocialLoginCode> consumeValid(String hash, Instant consumedAt) {
        int updatedRows = repository.consumeValid(hash, consumedAt);
        log.debug(
                "Tentativa de consumir codigo social: codeFingerprint={}, consumedAt={}, updatedRows={}",
                fingerprint(hash),
                consumedAt,
                updatedRows
        );

        if (updatedRows == 0) {
            return Optional.empty();
        }

        Optional<SocialLoginCode> consumedCode = repository.findByHash(hash).map(SocialLoginCodeMapper::toDomain);
        log.debug(
                "Codigo social consumido carregado: codeFingerprint={}, found={}",
                fingerprint(hash),
                consumedCode.isPresent()
        );
        return consumedCode;
    }
}
