package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.GameCategoryEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.GameCategoryId;

public interface GameCategoryJpaRepository extends JpaRepository<GameCategoryEntity, GameCategoryId> {
    List<GameCategoryEntity> findAllById_GameIdIn(Collection<Long> gameIds);

    void deleteAllById_GameId(long gameId);
}
