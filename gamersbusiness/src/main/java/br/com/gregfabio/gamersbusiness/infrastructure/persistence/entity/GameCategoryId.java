package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GameCategoryId implements Serializable {
    @Column(name = "jogo_id")
    private Long gameId;

    @Column(name = "categoria_id")
    private Long categoryId;

    protected GameCategoryId() {
    }

    public GameCategoryId(Long gameId, Long categoryId) {
        this.gameId = gameId;
        this.categoryId = categoryId;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GameCategoryId that)) {
            return false;
        }
        return Objects.equals(gameId, that.gameId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, categoryId);
    }
}
