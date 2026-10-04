package br.com.gregfabio.gamersbusiness.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import br.com.gregfabio.gamersbusiness.application.port.UserRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.UserEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.UserJpaRepository;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {
    private final UserJpaRepository users;

    public UserPersistenceAdapter(UserJpaRepository users) {
        this.users = users;
    }

    @Override
    public Optional<UserAccount> findById(long id) {
        return users.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return users.findByEmail(email).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return users.findByUsername(username).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByUsernameAndIdNot(String username, long id) {
        return users.existsByUsernameAndIdNot(username, id);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, long id) {
        return users.existsByEmailAndIdNot(email, id);
    }

    @Override
    public UserAccount save(UserAccount account) {
        try {
            UserEntity entity = account.id() == null
                    ? new UserEntity()
                    : users.findById(account.id()).orElseThrow(() -> DomainException.notFound("User not found"));
            entity.setUsername(account.username());
            entity.setEmail(account.email());
            entity.setPasswordHash(account.passwordHash());
            entity.setRole(account.role());
            entity.setRegisteredAt(account.registeredAt());
            return toDomain(users.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("User conflicts with an existing account or stored history");
        }
    }

    @Override
    public PageResult<UserAccount> findAll(PageRequest page) {
        return JpaPageMapper.map(users.findAll(JpaPageMapper.pageable(page)), UserPersistenceAdapter::toDomain);
    }

    @Override
    public void deleteById(long id) {
        try {
            UserEntity entity = users.findById(id)
                    .orElseThrow(() -> DomainException.notFound("User not found"));
            users.delete(entity);
            users.flush();
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("User has dependent historical records");
        }
    }

    private static UserAccount toDomain(UserEntity entity) {
        return new UserAccount(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getRegisteredAt());
    }
}
