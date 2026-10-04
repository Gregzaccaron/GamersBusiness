package br.com.gregfabio.gamersbusiness.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import br.com.gregfabio.gamersbusiness.application.port.UserRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String configuredSecret) {
        byte[] key = configuredSecret.getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes for HS256");
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setClock(clock);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestamps,
                new JwtClaimValidator<>(JwtClaimNames.EXP, Objects::nonNull),
                new JwtClaimValidator<>(JwtClaimNames.SUB, SecurityConfiguration::isPositiveLong)));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
            jwtAuthenticationConverter(UserRepositoryPort users) {
        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter =
                new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            long userId;
            try {
                userId = Long.parseLong(jwt.getSubject());
            } catch (NumberFormatException exception) {
                throw new InvalidBearerTokenException("Invalid token subject");
            }
            if (userId <= 0) {
                throw new InvalidBearerTokenException("Invalid token subject");
            }
            UserAccount account = users.findById(userId)
                    .orElseThrow(() -> new InvalidBearerTokenException("Account no longer exists"));
            Collection<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + account.role().name()));
            return authorities;
        });
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Environment environment,
            org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtConverter,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(
                            HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll();
                    if (environment.acceptsProfiles(Profiles.of("local"))) {
                        authorize.requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    }
                    authorize.requestMatchers("/api/v1/users/**").hasRole("ADMIN");
                    authorize.requestMatchers(HttpMethod.POST,
                            "/api/v1/developers", "/api/v1/categories", "/api/v1/games", "/api/v1/achievements")
                            .hasRole("ADMIN");
                    authorize.requestMatchers(HttpMethod.PUT,
                            "/api/v1/developers/**", "/api/v1/categories/**", "/api/v1/games/**", "/api/v1/achievements/**")
                            .hasRole("ADMIN");
                    authorize.requestMatchers(HttpMethod.DELETE,
                            "/api/v1/developers/**", "/api/v1/categories/**", "/api/v1/games/**", "/api/v1/achievements/**")
                            .hasRole("ADMIN");
                    authorize.requestMatchers("/api/v1/**").authenticated();
                    authorize.anyRequest().denyAll();
                })
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                        .authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    private static boolean isPositiveLong(String subject) {
        if (subject == null) {
            return false;
        }
        try {
            return Long.parseLong(subject) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
