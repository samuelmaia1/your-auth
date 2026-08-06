package com.samuelmaia1_github.yourauth.domain.shared;

import com.samuelmaia1_github.yourauth.domain.shared.exceptions.IllegalPaginationException;

public record Pagination(int page, int size) {
    public Pagination {
        if (page < 0)
            throw new IllegalPaginationException("A página não deve ser negativa");

        if (size <= 0 || size > 100)
            throw new IllegalPaginationException("O número de itens por página deve ser de 1 a 100");
    }
}
