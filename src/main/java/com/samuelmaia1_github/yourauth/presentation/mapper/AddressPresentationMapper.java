package com.samuelmaia1_github.yourauth.presentation.mapper;

import com.samuelmaia1_github.yourauth.domain.shared.Address;
import com.samuelmaia1_github.yourauth.presentation.dto.shared.AddressDTO;

public class AddressPresentationMapper {
    public static Address toDomain(AddressDTO dto) {
        return Address
                .builder()
                .cep(dto.cep())
                .street(dto.street())
                .number(dto.number())
                .neighborhood(dto.neighborhood())
                .state(dto.state())
                .city(dto.city())
                .build();
    }

    public static AddressDTO toDTO(Address address) {
        return new AddressDTO(
                address.getCep(),
                address.getStreet(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getNumber()
        );
    }
}
