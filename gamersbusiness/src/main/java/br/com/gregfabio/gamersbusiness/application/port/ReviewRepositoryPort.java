package br.com.gregfabio.gamersbusiness.application.port;

import java.util.Optional;

import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.Review;
import br.com.gregfabio.gamersbusiness.domain.model.ReviewAggregate;

public interface ReviewRepositoryPort {
    boolean existsByUserIdAndGameId(long userId, long gameId);

    Optional<Review> findById(long id);

    PageResult<Review> findByGameId(long gameId, PageRequest page);

    ReviewAggregate aggregateByGameId(long gameId);

    Review save(Review review);

    void deleteById(long id);
}
