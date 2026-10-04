package br.com.gregfabio.gamersbusiness.infrastructure.security;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.gregfabio.gamersbusiness.application.port.PasswordHasher;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private static final int MAX_PASSWORD_BYTES = 72;

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(CharSequence rawPassword) {
        if (!isWithinByteLimit(rawPassword)) {
            throw DomainException.badRequest("Password must contain at most 72 UTF-8 bytes");
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // BCrypt can ignore bytes beyond its limit when checking an existing hash.
        return isWithinByteLimit(rawPassword) && passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private static boolean isWithinByteLimit(CharSequence password) {
        return password != null
                && password.toString().getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }
}
