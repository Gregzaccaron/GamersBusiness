package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UnlockAchievementRequest(@NotNull @Positive Long achievementId) {
}
