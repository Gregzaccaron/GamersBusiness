package br.com.gregfabio.gamersbusiness.infrastructure.persistence.adapter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import br.com.gregfabio.gamersbusiness.application.port.CatalogRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.Achievement;
import br.com.gregfabio.gamersbusiness.domain.model.Category;
import br.com.gregfabio.gamersbusiness.domain.model.Developer;
import br.com.gregfabio.gamersbusiness.domain.model.Game;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.AchievementEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.CategoryEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.DeveloperEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.GameCategoryEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.GameCategoryId;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.entity.GameEntity;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.AchievementJpaRepository;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.CategoryJpaRepository;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.DeveloperJpaRepository;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.GameCategoryJpaRepository;
import br.com.gregfabio.gamersbusiness.infrastructure.persistence.repository.GameJpaRepository;

@Repository
public class CatalogPersistenceAdapter implements CatalogRepositoryPort {
    private final DeveloperJpaRepository developers;
    private final CategoryJpaRepository categories;
    private final GameJpaRepository games;
    private final GameCategoryJpaRepository gameCategories;
    private final AchievementJpaRepository achievements;

    public CatalogPersistenceAdapter(
            DeveloperJpaRepository developers,
            CategoryJpaRepository categories,
            GameJpaRepository games,
            GameCategoryJpaRepository gameCategories,
            AchievementJpaRepository achievements) {
        this.developers = developers;
        this.categories = categories;
        this.games = games;
        this.gameCategories = gameCategories;
        this.achievements = achievements;
    }

    @Override
    public Developer saveDeveloper(Developer developer) {
        try {
            DeveloperEntity entity = developer.id() == null
                    ? new DeveloperEntity()
                    : developers.findById(developer.id())
                            .orElseThrow(() -> DomainException.notFound("Developer not found"));
            entity.setName(developer.name());
            entity.setCountry(developer.country());
            entity.setFoundationDate(developer.foundationDate());
            return toDomain(developers.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Developer name is already in use or the record is referenced");
        }
    }

    @Override
    public Optional<Developer> findDeveloperById(long id) {
        return developers.findById(id).map(CatalogPersistenceAdapter::toDomain);
    }

    @Override
    public PageResult<Developer> findDevelopers(PageRequest page) {
        return JpaPageMapper.map(developers.findAll(JpaPageMapper.pageable(page)), CatalogPersistenceAdapter::toDomain);
    }

    @Override
    public void deleteDeveloper(long id) {
        try {
            developers.delete(developers.findById(id)
                    .orElseThrow(() -> DomainException.notFound("Developer not found")));
            developers.flush();
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Developer is referenced by one or more games");
        }
    }

    @Override
    public Category saveCategory(Category category) {
        try {
            CategoryEntity entity = category.id() == null
                    ? new CategoryEntity()
                    : categories.findById(category.id())
                            .orElseThrow(() -> DomainException.notFound("Category not found"));
            entity.setName(category.name());
            return toDomain(categories.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Category name is already in use or the record is referenced");
        }
    }

    @Override
    public Optional<Category> findCategoryById(long id) {
        return categories.findById(id).map(CatalogPersistenceAdapter::toDomain);
    }

    @Override
    public List<Category> findCategoriesByIds(List<Long> ids) {
        return categories.findAllById(ids).stream().map(CatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public PageResult<Category> findCategories(PageRequest page) {
        return JpaPageMapper.map(categories.findAll(JpaPageMapper.pageable(page)), CatalogPersistenceAdapter::toDomain);
    }

    @Override
    public void deleteCategory(long id) {
        try {
            categories.delete(categories.findById(id)
                    .orElseThrow(() -> DomainException.notFound("Category not found")));
            categories.flush();
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Category is referenced by one or more games");
        }
    }

    @Override
    public Game saveGame(Game game) {
        try {
            GameEntity entity = game.id() == null
                    ? new GameEntity()
                    : games.findById(game.id()).orElseThrow(() -> DomainException.notFound("Game not found"));
            entity.setTitle(game.title());
            entity.setDescription(game.description());
            entity.setPrice(game.price());
            entity.setReleaseDate(game.releaseDate());
            entity.setDeveloperId(game.developerId());
            GameEntity saved = games.saveAndFlush(entity);
            gameCategories.deleteAllById_GameId(saved.getId());
            gameCategories.flush();
            for (Long categoryId : game.categoryIds()) {
                gameCategories.save(new GameCategoryEntity(new GameCategoryId(saved.getId(), categoryId)));
            }
            gameCategories.flush();
            return toDomain(saved, game.categoryIds());
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Game violates a catalog uniqueness or reference constraint");
        }
    }

    @Override
    public Optional<Game> findGameById(long id) {
        return games.findById(id).map(entity -> toDomain(entity, categoryIds(List.of(entity)).getOrDefault(id, List.of())));
    }

    @Override
    public PageResult<Game> findGames(PageRequest page, String title, Long categoryId, Long developerId) {
        String normalizedTitle = title == null ? "" : title.toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        var result = games.search(normalizedTitle, categoryId, developerId, JpaPageMapper.pageable(page));
        Map<Long, List<Long>> categoriesByGame = categoryIds(result.getContent());
        return JpaPageMapper.map(result, entity ->
                toDomain(entity, categoriesByGame.getOrDefault(entity.getId(), List.of())));
    }

    @Override
    public void deleteGame(long id) {
        try {
            GameEntity game = games.findById(id).orElseThrow(() -> DomainException.notFound("Game not found"));
            gameCategories.deleteAllById_GameId(id);
            gameCategories.flush();
            games.delete(game);
            games.flush();
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Game has dependent library, review, or achievement history");
        }
    }

    @Override
    public Achievement saveAchievement(Achievement achievement) {
        try {
            AchievementEntity entity = achievement.id() == null
                    ? new AchievementEntity()
                    : achievements.findById(achievement.id())
                            .orElseThrow(() -> DomainException.notFound("Achievement not found"));
            entity.setGameId(achievement.gameId());
            entity.setName(achievement.name());
            entity.setDescription(achievement.description());
            return toDomain(achievements.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Achievement is referenced by an existing unlock");
        }
    }

    @Override
    public Optional<Achievement> findAchievementById(long id) {
        return achievements.findById(id).map(CatalogPersistenceAdapter::toDomain);
    }

    @Override
    public PageResult<Achievement> findAchievements(PageRequest page) {
        return JpaPageMapper.map(
                achievements.findAll(JpaPageMapper.pageable(page)), CatalogPersistenceAdapter::toDomain);
    }

    @Override
    public void deleteAchievement(long id) {
        try {
            achievements.delete(achievements.findById(id)
                    .orElseThrow(() -> DomainException.notFound("Achievement not found")));
            achievements.flush();
        } catch (DataIntegrityViolationException exception) {
            throw DomainException.conflict("Achievement has recorded unlocks");
        }
    }

    private Map<Long, List<Long>> categoryIds(List<GameEntity> page) {
        if (page.isEmpty()) {
            return Map.of();
        }
        List<Long> gameIds = page.stream().map(GameEntity::getId).toList();
        Map<Long, List<Long>> grouped = new HashMap<>();
        gameCategories.findAllById_GameIdIn(gameIds).stream()
                .sorted(Comparator.comparing(link -> link.getId().getCategoryId()))
                .forEach(link -> grouped.computeIfAbsent(link.getId().getGameId(), ignored -> new java.util.ArrayList<>())
                        .add(link.getId().getCategoryId()));
        return grouped.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Developer toDomain(DeveloperEntity entity) {
        return new Developer(entity.getId(), entity.getName(), entity.getCountry(), entity.getFoundationDate());
    }

    private static Category toDomain(CategoryEntity entity) {
        return new Category(entity.getId(), entity.getName());
    }

    private static Game toDomain(GameEntity entity, List<Long> categoryIds) {
        return new Game(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getReleaseDate(),
                entity.getDeveloperId(),
                categoryIds);
    }

    private static Achievement toDomain(AchievementEntity entity) {
        return new Achievement(entity.getId(), entity.getGameId(), entity.getName(), entity.getDescription());
    }
}
