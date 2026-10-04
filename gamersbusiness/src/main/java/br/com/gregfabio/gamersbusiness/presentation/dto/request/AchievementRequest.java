package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AchievementRequest(
        @NotNull @Positive Long gameId,
        @NotBlank @Size(max = 120) String name,
        @NotBlank String description) {
}
