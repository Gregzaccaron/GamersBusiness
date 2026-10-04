package br.com.gregfabio.gamersbusiness.domain.model;

import java.time.Instant;

public record UnlockedAchievement(Long userId, Long achievementId, Long gameId, Instant unlockedAt) {
}
