package br.com.gregfabio.gamersbusiness.application.port;

import java.util.Optional;

import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;

public interface UserRepositoryPort {
    Optional<UserAccount> findById(long id);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, long id);

    boolean existsByEmailAndIdNot(String email, long id);

    UserAccount save(UserAccount account);

    PageResult<UserAccount> findAll(PageRequest page);

    void deleteById(long id);
}
