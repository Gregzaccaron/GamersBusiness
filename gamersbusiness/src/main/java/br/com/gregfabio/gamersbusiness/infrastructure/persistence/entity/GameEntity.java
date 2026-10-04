package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "jogo")
@Getter
@Setter
@NoArgsConstructor
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 160)
    private String title;

    @Column(name = "descricao", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "preco", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "desenvolvedora_id", nullable = false)
    private Long developerId;
}
