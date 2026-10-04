package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.time.Instant;

import br.com.gregfabio.gamersbusiness.domain.model.UnlockedAchievement;

public record UserAchievementResponse(Long achievementId, Long gameId, Instant unlockedAt) {
    public static UserAchievementResponse from(UnlockedAchievement unlocked) {
        return new UserAchievementResponse(
                unlocked.achievementId(), unlocked.gameId(), unlocked.unlockedAt());
    }
}
