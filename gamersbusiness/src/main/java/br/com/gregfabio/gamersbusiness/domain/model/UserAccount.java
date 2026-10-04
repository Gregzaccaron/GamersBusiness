package br.com.gregfabio.gamersbusiness.domain.model;

import java.time.Instant;

public record UserAccount(
        Long id,
        String username,
        String email,
        String passwordHash,
        Role role,
        Instant registeredAt) {
}
