package br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserAchievementId implements Serializable {
    @Column(name = "usuario_id")
    private Long userId;

    @Column(name = "conquista_id")
    private Long achievementId;

    protected UserAchievementId() {
    }

    public UserAchievementId(Long userId, Long achievementId) {
        this.userId = userId;
        this.achievementId = achievementId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAchievementId() {
        return achievementId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserAchievementId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(achievementId, that.achievementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, achievementId);
    }
}
