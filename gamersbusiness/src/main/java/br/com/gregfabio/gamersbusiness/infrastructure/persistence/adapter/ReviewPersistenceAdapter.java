package br.com.gregfabio.gamersbusiness.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import br.com.gregfabio.gamersbusiness.application.port.ReviewRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.Review;
import br.com.gregfabio.gamersbusiness.domain.model.ReviewAggregate;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.ReviewEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.ReviewJpaRepository;

@Repository
public class ReviewPersistenceAdapter implements ReviewRepositoryPort {
    private final ReviewJpaRepository reviews;

    public ReviewPersistenceAdapter(ReviewJpaRepository reviews) {
        this.reviews = reviews;
    }

    @Override
    public boolean existsByUserIdAndGameId(long userId, long gameId) {
        return reviews.existsByUserIdAndGameId(userId, gameId);
    }

    @Override
    public Optional<Review> findById(long id) {
        return reviews.findById(id).map(ReviewPersistenceAdapter::toDomain);
    }

    @Override
    public PageResult<Review> findByGameId(long gameId, PageRequest page) {
        return JpaPageMapper.map(reviews.findByGameId(gameId, JpaPageMapper.pageable(page)),
                ReviewPersistenceAdapter::toDomain);
    }

    @Override
    public ReviewAggregate aggregateByGameId(long gameId) {
        var aggregate = reviews.aggregateByGameId(gameId);
        return new ReviewAggregate(aggregate.getAverageRating(), aggregate.getReviewCount());
    }

    @Override
    public Review save(Review review) {
        try {
            ReviewEntity entity = review.id() == null
                    ? new ReviewEntity()
                    : reviews.findById(review.id())
                            .orElseThrow(() -> DomainException.notFound("Review not found"));
            entity.setGameId(review.gameId());
            entity.setUserId(review.userId());
            entity.setRating((short) review.rating());
            entity.setComment(review.comment());
            entity.setReviewedAt(review.reviewedAt());
            return toDomain(reviews.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("A review already exists for this user and game");
        }
    }

    @Override
    public void deleteById(long id) {
        reviews.deleteById(id);
        reviews.flush();
    }

    private static Review toDomain(ReviewEntity entity) {
        return new Review(
                entity.getId(),
                entity.getGameId(),
                entity.getUserId(),
                entity.getRating(),
                entity.getComment(),
                entity.getReviewedAt());
    }
}
