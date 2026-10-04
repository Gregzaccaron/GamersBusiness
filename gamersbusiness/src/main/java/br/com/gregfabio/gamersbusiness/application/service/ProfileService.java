package br.com.gregfabio.gamersbusiness.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.PasswordHasher;
import br.com.gregfabio.gamersbusiness.application.port.UserRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

@Service
public class ProfileService {
    private final UserRepositoryPort users;
    private final PasswordHasher passwordHasher;

    public ProfileService(UserRepositoryPort users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public UserAccount getProfile(long userId) {
        return requireUser(userId);
    }

    @Transactional
    public UserAccount updateProfile(
            long userId,
            String username,
            String email,
            String currentPassword,
            String newPassword) {
        UserAccount current = requireUser(userId);
        if (username == null && email == null && currentPassword == null && newPassword == null) {
            throw DomainException.badRequest("At least one profile field must be provided");
        }
        boolean changingPassword = currentPassword != null || newPassword != null;
        if (changingPassword && (currentPassword == null || newPassword == null)) {
            throw DomainException.badRequest("currentPassword and newPassword must be provided together");
        }
        if (changingPassword && !passwordHasher.matches(currentPassword, current.passwordHash())) {
            throw DomainException.forbidden("Current password is incorrect");
        }
        if (username != null
                && !username.equals(current.username())
                && users.existsByUsernameAndIdNot(username, userId)) {
            throw DomainException.conflict("Username is already registered");
        }
        if (email != null && !email.equals(current.email()) && users.existsByEmailAndIdNot(email, userId)) {
            throw DomainException.conflict("Email is already registered");
        }
        String passwordHash = changingPassword ? passwordHasher.hash(newPassword) : current.passwordHash();
        return users.save(new UserAccount(
                current.id(),
                username == null ? current.username() : username,
                email == null ? current.email() : email,
                passwordHash,
                current.role(),
                current.registeredAt()));
    }

    @Transactional(readOnly = true)
    public PageResult<UserAccount> listUsers(PageRequest page) {
        return users.findAll(page);
    }

    @Transactional
    public void deleteUser(long userId) {
        requireUser(userId);
        users.deleteById(userId);
    }

    private UserAccount requireUser(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> DomainException.notFound("User not found"));
    }
}
