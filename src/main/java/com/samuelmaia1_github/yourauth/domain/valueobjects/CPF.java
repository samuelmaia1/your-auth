package com.samuelmaia1_github.yourauth.domain.valueobjects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CPF {

    private final String value;

    public CPF(String value) {

        if (!isValid(normalize(value))) {
            throw new IllegalArgumentException("CPF inválido");
        }

        this.value = normalize(value);
    }

    private String normalize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("CPF cannot be null");
        }

        String normalizedValue = value.replaceAll("\\D", "");
        if (normalizedValue.length() != 11) {
            throw new IllegalArgumentException("CPF must contain 11 digits");
        }

        return normalizedValue;
    }

    private boolean isValid(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }

    @Override
    public String toString() {
        return value;
    }
}