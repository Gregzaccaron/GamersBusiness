package br.com.gregfabio.gamersbusiness.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.gregfabio.gamersbusiness.domain.error.DomainException;

class BCryptPasswordHasherTest {
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(new BCryptPasswordEncoder());

    @ParameterizedTest
    @ValueSource(strings = {"a", "é", "🎮"})
    void acceptsPasswordsAtTheUtf8ByteLimit(String character) {
        int byteLength = character.getBytes(StandardCharsets.UTF_8).length;
        String password = character.repeat(72 / byteLength);
        String hash = hasher.hash(password);

        assertTrue(hasher.matches(password, hash));
        assertFalse(hasher.matches("wrong-password", hash));
        assertFalse(hasher.matches(password + character, hash));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "é", "🎮"})
    void rejectsPasswordsBeyondTheUtf8ByteLimitAsBadRequests(String character) {
        int byteLength = character.getBytes(StandardCharsets.UTF_8).length;
        String password = character.repeat(72 / byteLength + 1);

        DomainException exception = assertThrows(DomainException.class, () -> hasher.hash(password));

        assertEquals(DomainException.Reason.BAD_REQUEST, exception.reason());
    }
}
