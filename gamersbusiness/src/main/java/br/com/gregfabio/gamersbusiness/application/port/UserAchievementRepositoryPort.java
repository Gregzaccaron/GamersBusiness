package br.com.gregfabio.gamersbusiness.application.port;

import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.UnlockedAchievement;

public interface UserAchievementRepositoryPort {
    boolean existsByUserIdAndAchievementId(long userId, long achievementId);

    UnlockedAchievement save(UnlockedAchievement unlockedAchievement);

    PageResult<UnlockedAchievement> findByUserIdAndGameId(long userId, Long gameId, PageRequest page);
}
