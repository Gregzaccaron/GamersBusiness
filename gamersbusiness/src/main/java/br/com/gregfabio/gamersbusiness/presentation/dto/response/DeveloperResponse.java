package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.time.LocalDate;

import br.com.gregfabio.gamersbusiness.domain.model.Developer;

public record DeveloperResponse(Long id, String name, String country, LocalDate foundationDate) {
    public static DeveloperResponse from(Developer developer) {
        return new DeveloperResponse(
                developer.id(), developer.name(), developer.country(), developer.foundationDate());
    }
}
