package br.com.gregfabio.gamersbusiness.domain.model;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    public PageResult {
        items = List.copyOf(items);
    }

    public static <T> PageResult<T> of(List<T> items, PageRequest request, long totalElements) {
        int totalPages = Math.toIntExact((totalElements + request.size() - 1) / request.size());
        return new PageResult<>(items, request.page(), request.size(), totalElements, totalPages);
    }
}
