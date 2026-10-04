package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.gregfabio.gamersbusiness.domain.model.Game;

public record GameResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        LocalDate releaseDate,
        Long developerId,
        List<Long> categoryIds) {
    public GameResponse {
        categoryIds = List.copyOf(categoryIds);
    }

    public static GameResponse from(Game game) {
        return new GameResponse(
                game.id(), game.title(), game.description(), game.price(), game.releaseDate(),
                game.developerId(), game.categoryIds());
    }
}
