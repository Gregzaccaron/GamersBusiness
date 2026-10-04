package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.LibraryEntity;

public interface LibraryJpaRepository extends JpaRepository<LibraryEntity, Long> {
    boolean existsByUserIdAndGameId(long userId, long gameId);

    Optional<LibraryEntity> findByIdAndUserId(long id, long userId);

    Page<LibraryEntity> findByUserId(long userId, Pageable pageable);
}
