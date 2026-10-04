package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import br.com.gregfabio.gamersbusiness.domain.model.Achievement;

public record AchievementResponse(Long id, Long gameId, String name, String description) {
    public static AchievementResponse from(Achievement achievement) {
        return new AchievementResponse(
                achievement.id(), achievement.gameId(), achievement.name(), achievement.description());
    }
}
