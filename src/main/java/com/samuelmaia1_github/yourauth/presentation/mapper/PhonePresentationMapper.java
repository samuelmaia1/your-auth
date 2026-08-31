package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.PhoneDTO;

public class PhonePresentationMapper {
    public static Phone toDomain(PhoneDTO dto) {
        if (dto == null) {
            return null;
        }

        return Phone
                .builder()
                .ddd(dto.ddd())
                .number(dto.number())
                .build();
    }

    public static PhoneDTO toDTO(Phone phone) {
        if (phone == null) {
            return null;
        }

        return new PhoneDTO(
                phone.getDdd(),
                phone.getNumber()
        );
    }
}
