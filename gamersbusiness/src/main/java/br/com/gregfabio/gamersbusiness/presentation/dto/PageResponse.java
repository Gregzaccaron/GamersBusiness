package br.com.gregfabio.gamersbusiness.presentation.dto;

import java.util.List;
import java.util.function.Function;

import br.com.gregfabio.gamersbusiness.domain.model.PageResult;

public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    public PageResponse {
        items = List.copyOf(items);
    }

    public static <S, T> PageResponse<T> from(PageResult<S> result, Function<S, T> mapper) {
        return new PageResponse<>(
                result.items().stream().map(mapper).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

}
