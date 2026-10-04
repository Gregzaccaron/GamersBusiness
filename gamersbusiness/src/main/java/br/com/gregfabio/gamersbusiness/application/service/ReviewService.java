package br.com.gregfabio.gamersbusiness.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.CatalogRepositoryPort;
import br.com.gregfabio.gamersbusiness.application.port.LibraryRepositoryPort;
import br.com.gregfabio.gamersbusiness.application.port.ReviewRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.Review;
import br.com.gregfabio.gamersbusiness.domain.model.ReviewAggregate;

@Service
public class ReviewService {
    private final ReviewRepositoryPort reviews;
    private final LibraryRepositoryPort library;
    private final CatalogRepositoryPort catalog;
    private final Clock clock;

    public ReviewService(
            ReviewRepositoryPort reviews,
            LibraryRepositoryPort library,
            CatalogRepositoryPort catalog,
            Clock clock) {
        this.reviews = reviews;
        this.library = library;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReviewPage list(long gameId, PageRequest page) {
        requireGame(gameId);
        return new ReviewPage(reviews.findByGameId(gameId, page), reviews.aggregateByGameId(gameId));
    }

    @Transactional
    public Review create(long userId, long gameId, int rating, String comment) {
        requireGame(gameId);
        validateRating(rating);
        if (!library.existsByUserIdAndGameId(userId, gameId)) {
            throw DomainException.forbidden("An active library entry is required to review this game");
        }
        if (reviews.existsByUserIdAndGameId(userId, gameId)) {
            throw DomainException.conflict("A review for this game already exists");
        }
        return reviews.save(new Review(null, gameId, userId, rating, comment, clock.instant()));
    }

    @Transactional
    public Review update(long userId, long reviewId, Integer rating, String comment, boolean commentProvided) {
        Review review = reviews.findById(reviewId)
                .orElseThrow(() -> DomainException.notFound("Review not found"));
        if (!review.userId().equals(userId)) {
            throw DomainException.forbidden("Only the review author can change this review");
        }
        if (!library.existsByUserIdAndGameId(userId, review.gameId())) {
            throw DomainException.forbidden("An active library entry is required to edit this review");
        }
        if (rating == null && !commentProvided) {
            throw DomainException.badRequest("At least one review field must be provided");
        }
        if (rating != null) {
            validateRating(rating);
        }
        return reviews.save(new Review(
                review.id(),
                review.gameId(),
                review.userId(),
                rating == null ? review.rating() : rating,
                commentProvided ? comment : review.comment(),
                review.reviewedAt()));
    }

    @Transactional
    public void delete(long userId, long reviewId) {
        Review review = reviews.findById(reviewId)
                .orElseThrow(() -> DomainException.notFound("Review not found"));
        if (!review.userId().equals(userId)) {
            throw DomainException.forbidden("Only the review author can delete this review");
        }
        reviews.deleteById(reviewId);
    }

    private void requireGame(long gameId) {
        if (catalog.findGameById(gameId).isEmpty()) {
            throw DomainException.notFound("Game not found");
        }
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw DomainException.badRequest("rating must be between 1 and 5");
        }
    }

    public record ReviewPage(PageResult<Review> page, ReviewAggregate aggregate) {
    }
}
