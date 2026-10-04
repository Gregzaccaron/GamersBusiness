package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.ReviewEntity;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByUserIdAndGameId(long userId, long gameId);

    Page<ReviewEntity> findByGameId(long gameId, Pageable pageable);

    @Query("select avg(r.rating) as averageRating, count(r) as reviewCount "
            + "from ReviewEntity r where r.gameId = :gameId")
    ReviewAggregateProjection aggregateByGameId(@Param("gameId") long gameId);

    interface ReviewAggregateProjection {
        Double getAverageRating();

        Long getReviewCount();
    }
}
