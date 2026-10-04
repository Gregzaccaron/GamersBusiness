package br.com.gregfabio.gamersbusiness.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.CatalogRepositoryPort;
import br.com.gregfabio.gamersbusiness.application.port.LibraryRepositoryPort;
import br.com.gregfabio.gamersbusiness.application.port.UserAchievementRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.UnlockedAchievement;

@Service
public class UserAchievementService {
    private final UserAchievementRepositoryPort achievements;
    private final LibraryRepositoryPort library;
    private final CatalogRepositoryPort catalog;
    private final Clock clock;

    public UserAchievementService(
            UserAchievementRepositoryPort achievements,
            LibraryRepositoryPort library,
            CatalogRepositoryPort catalog,
            Clock clock) {
        this.achievements = achievements;
        this.library = library;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional
    public UnlockedAchievement unlock(long userId, long achievementId) {
        var achievement = catalog.findAchievementById(achievementId)
                .orElseThrow(() -> DomainException.notFound("Achievement not found"));
        if (achievements.existsByUserIdAndAchievementId(userId, achievementId)) {
            throw DomainException.conflict("Achievement has already been unlocked");
        }
        if (!library.existsByUserIdAndGameId(userId, achievement.gameId())) {
            throw DomainException.forbidden("An active library entry is required to unlock this achievement");
        }
        return achievements.save(new UnlockedAchievement(userId, achievementId, achievement.gameId(), clock.instant()));
    }

    @Transactional(readOnly = true)
    public PageResult<UnlockedAchievement> list(long userId, Long gameId, PageRequest page) {
        return achievements.findByUserIdAndGameId(userId, gameId, page);
    }
}
