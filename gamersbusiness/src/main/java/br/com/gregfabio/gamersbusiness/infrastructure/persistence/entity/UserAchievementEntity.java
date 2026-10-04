package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_conquista")
@Getter
@Setter
@NoArgsConstructor
public class UserAchievementEntity {
    @EmbeddedId
    private UserAchievementId id;

    @MapsId("achievementId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conquista_id", nullable = false)
    private AchievementEntity achievement;

    @Column(name = "data_desbloqueio", nullable = false)
    private Instant unlockedAt;
}
