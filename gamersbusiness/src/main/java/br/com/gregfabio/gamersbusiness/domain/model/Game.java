package br.com.gregfabio.gamersbusiness.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Game(
        Long id,
        String title,
        String description,
        BigDecimal price,
        LocalDate releaseDate,
        Long developerId,
        List<Long> categoryIds) {
    public Game {
        categoryIds = List.copyOf(categoryIds);
    }
}
