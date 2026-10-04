package br.com.gregfabio.gamersbusiness.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.CatalogRepositoryPort;
import br.com.gregfabio.gamersbusiness.application.port.LibraryRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.LibraryEntry;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;

@Service
public class LibraryService {
    private final LibraryRepositoryPort library;
    private final CatalogRepositoryPort catalog;
    private final Clock clock;

    public LibraryService(LibraryRepositoryPort library, CatalogRepositoryPort catalog, Clock clock) {
        this.library = library;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResult<LibraryEntry> list(long userId, PageRequest page) {
        return library.findByUserId(userId, page);
    }

    @Transactional
    public LibraryEntry acquire(long userId, long gameId) {
        var game = catalog.findGameById(gameId)
                .orElseThrow(() -> DomainException.notFound("Game not found"));
        if (library.existsByUserIdAndGameId(userId, gameId)) {
            throw DomainException.conflict("Game is already in the library");
        }
        return library.save(new LibraryEntry(null, userId, gameId, clock.instant(), game.price(), 0));
    }

    @Transactional
    public LibraryEntry updateHours(long userId, long entryId, int hoursPlayed) {
        if (hoursPlayed < 0) {
            throw DomainException.badRequest("hoursPlayed must be zero or greater");
        }
        LibraryEntry entry = library.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> DomainException.notFound("Library entry not found"));
        return library.save(new LibraryEntry(
                entry.id(), entry.userId(), entry.gameId(), entry.acquiredAt(), entry.paidPrice(), hoursPlayed));
    }

    @Transactional
    public void remove(long userId, long entryId) {
        library.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> DomainException.notFound("Library entry not found"));
        library.deleteById(entryId);
    }
}
