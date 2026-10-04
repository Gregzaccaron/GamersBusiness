package br.com.gregfabio.gamersbusiness.domain.model;

import br.com.gregfabio.gamersbusiness.domain.error.DomainException;

public record PageRequest(int page, int size) {
    public PageRequest {
        if (page < 0) {
            throw DomainException.badRequest("page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw DomainException.badRequest("size must be between 1 and 100");
        }
    }
}
