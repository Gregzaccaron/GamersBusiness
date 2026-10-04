package br.com.gregfabio.gamersbusiness.application.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.PasswordHasher;
import br.com.gregfabio.gamersbusiness.application.port.TokenIssuer;
import br.com.gregfabio.gamersbusiness.application.port.UserRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.Role;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

@Service
public class AuthService {
    private final UserRepositoryPort users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    public AuthService(
            UserRepositoryPort users,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer,
            Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Transactional
    public UserAccount register(String username, String email, String password) {
        if (users.findByUsername(username).isPresent() || users.findByEmail(email).isPresent()) {
            throw DomainException.conflict("Username or email is already registered");
        }
        return users.save(new UserAccount(
                null, username, email, passwordHasher.hash(password), Role.USER, clock.instant()));
    }

    @Transactional(readOnly = true)
    public TokenIssuer.IssuedToken login(String email, String password) {
        UserAccount account = users.findByEmail(email)
                .filter(found -> passwordHasher.matches(password, found.passwordHash()))
                .orElseThrow(() -> DomainException.unauthenticated("Invalid email or password"));
        return tokenIssuer.issue(account);
    }

    @Transactional
    public UserAccount provisionAdmin(String username, String email, String password) {
        Optional<UserAccount> byUsername = users.findByUsername(username);
        Optional<UserAccount> byEmail = users.findByEmail(email);
        if (byUsername.isPresent() || byEmail.isPresent()) {
            if (byUsername.isPresent()
                    && byEmail.isPresent()
                    && byUsername.get().id().equals(byEmail.get().id())
                    && byUsername.get().role() == Role.ADMIN) {
                return byUsername.get();
            }
            throw DomainException.conflict("Bootstrap identity collides with an existing account");
        }
        return users.save(new UserAccount(
                null, username, email, passwordHasher.hash(password), Role.ADMIN, clock.instant()));
    }
}
