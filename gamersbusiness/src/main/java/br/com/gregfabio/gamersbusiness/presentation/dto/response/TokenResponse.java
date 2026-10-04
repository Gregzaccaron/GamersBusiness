package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.time.Instant;

import br.com.gregfabio.gamersbusiness.application.port.TokenIssuer;

public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {
    public static TokenResponse from(TokenIssuer.IssuedToken token) {
        return new TokenResponse(token.value(), "Bearer", token.expiresAt());
    }
}
