package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.project.passwordconfig.PasswordConfig;
import com.samuelmaia1_github.yourauth.presentation.dto.passwordconfig.PasswordConfigDTO;

public class PasswordConfigPresentationMapper {
    public static PasswordConfig toDomain(PasswordConfigDTO dto) {
        if (dto == null) {
            return null;
        }

        return PasswordConfig.builder()
                .lowercaseRequired(
                        Boolean.TRUE.equals(dto.lowercaseRequired())
                )
                .uppercaseRequired(
                        Boolean.TRUE.equals(dto.uppercaseRequired())
                )
                .numberRequired(
                        Boolean.TRUE.equals(dto.numberRequired())
                )
                .specialCharRequired(
                        Boolean.TRUE.equals(dto.specialCharRequired())
                )
                .minSize(
                        dto.minSize() != null
                                ? dto.minSize()
                                : PasswordConfig.DEFAULT_MIN_SIZE
                )
                .maxSize(
                        dto.maxSize() != null
                                ? dto.maxSize()
                                : PasswordConfig.DEFAULT_MAX_SIZE
                )
                .build();
    }

    public static PasswordConfigDTO toDto(PasswordConfig domain) {
        return new PasswordConfigDTO(
                domain.getMinSize(),
                domain.getMaxSize(),
                domain.isNumberRequired(),
                domain.isUppercaseRequired(),
                domain.isLowercaseRequired(),
                domain.isSpecialCharRequired()
        );
    }
}
