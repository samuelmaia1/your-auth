package com.samuelmaia1_github.yourauth.infra.mappers;

import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.infra.repository.entity.PasswordConfigEntity;

public class PasswordConfigMapper {
    private PasswordConfigMapper(){}

    public static PasswordConfig toDomain(PasswordConfigEntity entity) {
        return PasswordConfig
                .builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .specialCharRequired(entity.isSpecialCharRequired())
                .lowercaseRequired(entity.isLowercaseRequired())
                .uppercaseRequired(entity.isUppercaseRequired())
                .numberRequired(entity.isNumberRequired())
                .minSize(entity.getMinSize())
                .maxSize(entity.getMaxSize())
                .build();
    }

    public static PasswordConfigEntity toEntity(PasswordConfig domain) {
        return PasswordConfigEntity
                .builder()
                .id(domain.getId())
                .projectId(domain.getProjectId())
                .minSize(domain.getMinSize())
                .maxSize(domain.getMaxSize())
                .lowercaseRequired(domain.isLowercaseRequired())
                .uppercaseRequired(domain.isUppercaseRequired())
                .numberRequired(domain.isNumberRequired())
                .specialCharRequired(domain.isSpecialCharRequired())
                .build();
    }
}
