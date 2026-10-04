package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.UserAchievementEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.UserAchievementId;

public interface UserAchievementJpaRepository extends JpaRepository<UserAchievementEntity, UserAchievementId> {
    boolean existsById_UserIdAndId_AchievementId(long userId, long achievementId);

    @Query(
            value = "select ua from UserAchievementEntity ua join fetch ua.achievement a "
                    + "where ua.id.userId = :userId and (:gameId is null or a.gameId = :gameId)",
            countQuery = "select count(ua) from UserAchievementEntity ua "
                    + "where ua.id.userId = :userId and (:gameId is null or ua.achievement.gameId = :gameId)")
    Page<UserAchievementEntity> findForUserAndGame(
            @Param("userId") long userId,
            @Param("gameId") Long gameId,
            Pageable pageable);
}
