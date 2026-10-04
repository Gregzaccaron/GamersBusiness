package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jogo_categoria")
@Getter
@Setter
@NoArgsConstructor
public class GameCategoryEntity {
    @EmbeddedId
    private GameCategoryId id;

    public GameCategoryEntity(GameCategoryId id) {
        this.id = id;
    }
}
