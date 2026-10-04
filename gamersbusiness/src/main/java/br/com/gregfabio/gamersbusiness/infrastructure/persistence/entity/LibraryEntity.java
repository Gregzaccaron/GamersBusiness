package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import java.math.BigDecimal;
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
@Table(name = "biblioteca")
@Getter
@Setter
@NoArgsConstructor
public class LibraryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long userId;

    @Column(name = "jogo_id", nullable = false)
    private Long gameId;

    @Column(name = "data_aquisicao", nullable = false)
    private Instant acquiredAt;

    @Column(name = "preco_pago", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidPrice;

    @Column(name = "horas_jogadas", nullable = false)
    private int hoursPlayed;
}
