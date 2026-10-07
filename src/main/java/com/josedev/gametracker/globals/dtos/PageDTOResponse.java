package com.josedev.gametracker.globals.dtos;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Stable JSON shape for paginated responses (instead of serializing Spring's Page directly).
 * page is 0-based.
 */
public record PageDTOResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <E, T> PageDTOResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageDTOResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

}
