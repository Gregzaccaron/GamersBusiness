package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.DeveloperEntity;

public interface DeveloperJpaRepository extends JpaRepository<DeveloperEntity, Long> {
}
