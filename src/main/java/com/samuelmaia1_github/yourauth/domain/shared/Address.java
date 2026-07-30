package com.samuelmaia1_github.yourauth.domain.shared;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String cep;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String number;
}