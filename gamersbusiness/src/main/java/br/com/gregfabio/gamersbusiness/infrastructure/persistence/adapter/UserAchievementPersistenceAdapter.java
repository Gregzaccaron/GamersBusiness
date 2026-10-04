package br.com.gregfabio.gamersbusiness.infrastructure.persistence.adapter;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.com.gregfabio.gamersbusiness.application.port.UserAchievementRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.UnlockedAchievement;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.UserAchievementEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.UserAchievementId;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.AchievementJpaRepository;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.UserAchievementJpaRepository;

@Repository
public class UserAchievementPersistenceAdapter implements UserAchievementRepositoryPort {
    private final UserAchievementJpaRepository unlocked;
    private final AchievementJpaRepository achievements;

    public UserAchievementPersistenceAdapter(
            UserAchievementJpaRepository unlocked,
            AchievementJpaRepository achievements) {
        this.unlocked = unlocked;
        this.achievements = achievements;
    }

    @Override
    public boolean existsByUserIdAndAchievementId(long userId, long achievementId) {
        return unlocked.existsById_UserIdAndId_AchievementId(userId, achievementId);
    }

    @Override
    public UnlockedAchievement save(UnlockedAchievement achievement) {
        try {
            UserAchievementEntity entity = new UserAchievementEntity();
            entity.setId(new UserAchievementId(achievement.userId(), achievement.achievementId()));
            entity.setAchievement(achievements.getReferenceById(achievement.achievementId()));
            entity.setUnlockedAt(achievement.unlockedAt());
            return toDomain(unlocked.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Achievement has already been unlocked or its reference is invalid");
        }
    }

    @Override
    public PageResult<UnlockedAchievement> findByUserIdAndGameId(long userId, Long gameId, PageRequest page) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                page.page(), page.size(), Sort.by(Sort.Direction.ASC, "id.achievementId"));
        var result = unlocked.findForUserAndGame(userId, gameId, pageable);
        return JpaPageMapper.map(result, UserAchievementPersistenceAdapter::toDomain);
    }

    private static UnlockedAchievement toDomain(UserAchievementEntity entity) {
        return new UnlockedAchievement(
                entity.getId().getUserId(),
                entity.getId().getAchievementId(),
                entity.getAchievement().getGameId(),
                entity.getUnlockedAt());
    }
}
