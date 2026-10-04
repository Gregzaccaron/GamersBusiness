package br.com.gregfabio.gamersbusiness.infrastructure.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BootstrapAdminCredentials(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank String password) {
}
