package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.GameEntity;

public interface GameJpaRepository extends JpaRepository<GameEntity, Long> {
    @Query(
            value = "select distinct g from GameEntity g "
                    + "left join GameCategoryEntity gc on gc.id.gameId = g.id "
                    + "where (:title is null or lower(g.title) like concat('%', lower(:title), '%')) "
                    + "and (:categoryId is null or gc.id.categoryId = :categoryId) "
                    + "and (:developerId is null or g.developerId = :developerId)",
            countQuery = "select count(distinct g.id) from GameEntity g "
                    + "left join GameCategoryEntity gc on gc.id.gameId = g.id "
                    + "where (:title is null or lower(g.title) like concat('%', lower(:title), '%')) "
                    + "and (:categoryId is null or gc.id.categoryId = :categoryId) "
                    + "and (:developerId is null or g.developerId = :developerId)")
    Page<GameEntity> search(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("developerId") Long developerId,
            Pageable pageable);
}
