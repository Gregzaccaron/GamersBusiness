package br.com.gregfabio.gamersbusiness.application.port;

import java.time.Instant;

import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

public interface TokenIssuer {
    IssuedToken issue(UserAccount account);

    record IssuedToken(String value, Instant expiresAt) {
    }
}
