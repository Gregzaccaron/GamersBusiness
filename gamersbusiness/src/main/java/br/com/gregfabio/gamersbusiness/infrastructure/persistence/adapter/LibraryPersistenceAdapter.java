package br.com.gregfabio.gamersbusiness.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import br.com.gregfabio.gamersbusiness.application.port.LibraryRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.LibraryEntry;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.LibraryEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.LibraryJpaRepository;

@Repository
public class LibraryPersistenceAdapter implements LibraryRepositoryPort {
    private final LibraryJpaRepository entries;

    public LibraryPersistenceAdapter(LibraryJpaRepository entries) {
        this.entries = entries;
    }

    @Override
    public boolean existsByUserIdAndGameId(long userId, long gameId) {
        return entries.existsByUserIdAndGameId(userId, gameId);
    }

    @Override
    public Optional<LibraryEntry> findByIdAndUserId(long id, long userId) {
        return entries.findByIdAndUserId(id, userId).map(LibraryPersistenceAdapter::toDomain);
    }

    @Override
    public PageResult<LibraryEntry> findByUserId(long userId, PageRequest page) {
        return JpaPageMapper.map(entries.findByUserId(userId, JpaPageMapper.pageable(page)),
                LibraryPersistenceAdapter::toDomain);
    }

    @Override
    public LibraryEntry save(LibraryEntry entry) {
        try {
            LibraryEntity entity = entry.id() == null
                    ? new LibraryEntity()
                    : entries.findById(entry.id())
                            .orElseThrow(() -> DomainException.notFound("Library entry not found"));
            entity.setUserId(entry.userId());
            entity.setGameId(entry.gameId());
            entity.setAcquiredAt(entry.acquiredAt());
            entity.setPaidPrice(entry.paidPrice());
            entity.setHoursPlayed(entry.hoursPlayed());
            return toDomain(entries.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Library entry conflicts with an active acquisition or reference");
        }
    }

    @Override
    public void deleteById(long id) {
        LibraryEntity entity = entries.findById(id)
                .orElseThrow(() -> DomainException.notFound("Library entry not found"));
        entries.delete(entity);
        entries.flush();
    }

    private static LibraryEntry toDomain(LibraryEntity entity) {
        return new LibraryEntry(
                entity.getId(),
                entity.getUserId(),
                entity.getGameId(),
                entity.getAcquiredAt(),
                entity.getPaidPrice(),
                entity.getHoursPlayed());
    }
}
