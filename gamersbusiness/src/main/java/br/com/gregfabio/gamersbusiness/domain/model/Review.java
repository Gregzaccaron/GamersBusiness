package br.com.gregfabio.gamersbusiness.domain.model;

import java.time.Instant;

public record Review(Long id, Long gameId, Long userId, int rating, String comment, Instant reviewedAt) {
}
