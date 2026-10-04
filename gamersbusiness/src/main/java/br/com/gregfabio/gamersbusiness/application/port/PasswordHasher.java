package br.com.gregfabio.gamersbusiness.application.port;

public interface PasswordHasher {
    String hash(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
