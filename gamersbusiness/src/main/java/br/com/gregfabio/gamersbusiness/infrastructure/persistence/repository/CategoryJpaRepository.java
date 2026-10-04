package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.CategoryEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
}
