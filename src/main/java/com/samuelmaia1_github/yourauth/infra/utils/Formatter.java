package com.samuelmaia1_github.yourauth.infra.utils;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class Formatter {
    public static String onlyDigits(String value) {
        if (value == null) return null;
        return value.replaceAll("\\D", "");
    }

    public static String normalizeCpf(String cpf) {
        return onlyDigits(cpf);
    }

    public static String normalizeCnpj(String cnpj) {
        return onlyDigits(cnpj);
    }

    public static String normalizeCep(String cep) {
        return onlyDigits(cep);
    }

    public static String formatCpf(String cpf) {
        String digits = onlyDigits(cpf);
        if (digits.length() != 11) return cpf;

        return digits.replaceFirst(
                "(\\d{3})(\\d{3})(\\d{3})(\\d{2})",
                "$1.$2.$3-$4"
        );
    }

    public static String formatCnpj(String cnpj) {
        String digits = onlyDigits(cnpj);
        if (digits.length() != 14) return cnpj;

        return digits.replaceFirst(
                "(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})",
                "$1.$2.$3/$4-$5"
        );
    }

    public static String formatCep(String cep) {
        String digits = onlyDigits(cep);
        if (digits.length() != 8) return cep;

        return digits.replaceFirst(
                "(\\d{5})(\\d{3})",
                "$1-$2"
        );
    }

    public static String formatLocalDateTime(LocalDateTime dateTime) {
        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return dateTime.format(format);
    }
}
