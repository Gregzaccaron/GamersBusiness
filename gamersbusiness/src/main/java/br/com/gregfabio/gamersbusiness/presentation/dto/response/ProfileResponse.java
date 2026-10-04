package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.time.Instant;

import br.com.gregfabio.gamersbusiness.domain.model.Role;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

public record ProfileResponse(Long id, String username, String email, Role role, Instant registeredAt) {
    public static ProfileResponse from(UserAccount account) {
        return new ProfileResponse(
                account.id(), account.username(), account.email(), account.role(), account.registeredAt());
    }
}
