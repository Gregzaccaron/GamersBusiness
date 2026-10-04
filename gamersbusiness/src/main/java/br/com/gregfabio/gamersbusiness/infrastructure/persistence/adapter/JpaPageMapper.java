package br.com.gregfabio.gamersbusiness.infrastructure.persistence.adapter;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;

final class JpaPageMapper {
    private JpaPageMapper() {
    }

    static Pageable pageable(PageRequest request) {
        return org.springframework.data.domain.PageRequest.of(
                request.page(), request.size(), Sort.by(Sort.Direction.ASC, "id"));
    }

    static <E, T> PageResult<T> map(Page<E> page, Function<E, T> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
