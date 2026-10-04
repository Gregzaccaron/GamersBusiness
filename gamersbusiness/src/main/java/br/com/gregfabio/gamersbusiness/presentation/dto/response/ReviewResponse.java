package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.time.Instant;

import br.com.gregfabio.gamersbusiness.domain.model.Review;

public record ReviewResponse(
        Long id, Long gameId, Long userId, int rating, String comment, Instant reviewedAt) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.id(), review.gameId(), review.userId(), review.rating(), review.comment(), review.reviewedAt());
    }
}
