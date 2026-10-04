package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record LibraryHoursRequest(@NotNull @PositiveOrZero Integer hoursPlayed) {
}
