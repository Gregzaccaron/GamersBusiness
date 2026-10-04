package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, long id);

    boolean existsByEmailAndIdNot(String email, long id);
}
