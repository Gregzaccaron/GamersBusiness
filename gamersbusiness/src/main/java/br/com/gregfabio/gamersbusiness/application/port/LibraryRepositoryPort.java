package br.com.gregfabio.gamersbusiness.application.port;

import java.util.Optional;

import br.com.gregfabio.gamersbusiness.domain.model.LibraryEntry;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;

public interface LibraryRepositoryPort {
    boolean existsByUserIdAndGameId(long userId, long gameId);

    Optional<LibraryEntry> findByIdAndUserId(long id, long userId);

    PageResult<LibraryEntry> findByUserId(long userId, PageRequest page);

    LibraryEntry save(LibraryEntry entry);

    void deleteById(long id);
}
