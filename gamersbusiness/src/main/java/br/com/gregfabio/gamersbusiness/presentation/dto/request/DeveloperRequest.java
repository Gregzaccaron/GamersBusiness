package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeveloperRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 100) String country,
        @NotNull LocalDate foundationDate) {
}
