package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import br.com.gregfabio.gamersbusiness.domain.model.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.id(), category.name());
    }
}
