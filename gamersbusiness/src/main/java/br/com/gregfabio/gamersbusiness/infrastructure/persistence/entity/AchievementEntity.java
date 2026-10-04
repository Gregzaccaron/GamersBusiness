package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conquista")
@Getter
@Setter
@NoArgsConstructor
public class AchievementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jogo_id", nullable = false)
    private Long gameId;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Column(name = "descricao", nullable = false, columnDefinition = "text")
    private String description;
}
