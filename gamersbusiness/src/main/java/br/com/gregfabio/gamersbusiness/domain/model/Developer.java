package br.com.gregfabio.gamersbusiness.domain.model;

import java.time.LocalDate;

public record Developer(Long id, String name, String country, LocalDate foundationDate) {
}
