package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GameRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank String description,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull LocalDate releaseDate,
        @NotNull @Positive Long developerId,
        @NotEmpty List<@NotNull @Positive Long> categoryIds) {
    @AssertTrue(message = "categoryIds must not contain duplicate IDs")
    public boolean hasDistinctCategoryIds() {
        return categoryIds == null || new HashSet<>(categoryIds).size() == categoryIds.size();
    }
}
