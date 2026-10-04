package br.com.gregfabio.gamersbusiness.infrastructure.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import br.com.gregfabio.gamersbusiness.application.port.TokenIssuer;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

@Component
public class JwtTokenIssuer implements TokenIssuer {
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(2);

    private final JwtEncoder encoder;
    private final Clock clock;

    public JwtTokenIssuer(JwtEncoder encoder, Clock clock) {
        this.encoder = encoder;
        this.clock = clock;
    }

    @Override
    public IssuedToken issue(UserAccount account) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(TOKEN_LIFETIME);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(account.id().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", account.role().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }
}
