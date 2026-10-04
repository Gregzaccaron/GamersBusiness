package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import java.time.Instant;

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
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jogo_id", nullable = false)
    private Long gameId;

    @Column(name = "usuario_id", nullable = false)
    private Long userId;

    @Column(name = "nota", nullable = false)
    private short rating;

    @Column(name = "comentario", columnDefinition = "text")
    private String comment;

    @Column(name = "data_avaliacao", nullable = false)
    private Instant reviewedAt;
}
