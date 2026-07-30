package com.samuelmaia1_github.yourauth.infra.persistence;

import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CPFConverter implements AttributeConverter<CPF, String> {

    @Override
    public String convertToDatabaseColumn(CPF cpf) {
        return cpf == null ? null : cpf.getValue();
    }

    @Override
    public CPF convertToEntityAttribute(String value) {
        return value == null ? null : new CPF(value);
    }
}
