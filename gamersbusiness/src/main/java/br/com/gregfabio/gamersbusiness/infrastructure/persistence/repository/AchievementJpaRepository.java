package br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.AchievementEntity;

public interface AchievementJpaRepository extends JpaRepository<AchievementEntity, Long> {
}
