package br.com.gregfabio.gamersbusiness.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.CatalogRepositoryPort;
import br.com.gregfabio.gamersbusiness.domain.error.DomainException;
import br.com.gregfabio.gamersbusiness.domain.model.Achievement;
import br.com.gregfabio.gamersbusiness.domain.model.Category;
import br.com.gregfabio.gamersbusiness.domain.model.Developer;
import br.com.gregfabio.gamersbusiness.domain.model.Game;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;

@Service
public class CatalogService {
    private final CatalogRepositoryPort catalog;

    public CatalogService(CatalogRepositoryPort catalog) {
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public PageResult<Developer> developers(PageRequest page) {
        return catalog.findDevelopers(page);
    }

    @Transactional(readOnly = true)
    public Developer developer(long id) {
        return catalog.findDeveloperById(id).orElseThrow(() -> DomainException.notFound("Developer not found"));
    }

    @Transactional
    public Developer saveDeveloper(Long id, String name, String country, LocalDate foundationDate) {
        if (id != null) {
            developer(id);
        }
        return catalog.saveDeveloper(new Developer(id, name, country, foundationDate));
    }

    @Transactional
    public void deleteDeveloper(long id) {
        developer(id);
        catalog.deleteDeveloper(id);
    }

    @Transactional(readOnly = true)
    public PageResult<Category> categories(PageRequest page) {
        return catalog.findCategories(page);
    }

    @Transactional(readOnly = true)
    public Category category(long id) {
        return catalog.findCategoryById(id).orElseThrow(() -> DomainException.notFound("Category not found"));
    }

    @Transactional
    public Category saveCategory(Long id, String name) {
        if (id != null) {
            category(id);
        }
        return catalog.saveCategory(new Category(id, name));
    }

    @Transactional
    public void deleteCategory(long id) {
        category(id);
        catalog.deleteCategory(id);
    }

    @Transactional(readOnly = true)
    public PageResult<Game> games(PageRequest page, String title, Long categoryId, Long developerId) {
        return catalog.findGames(page, title, categoryId, developerId);
    }

    @Transactional(readOnly = true)
    public Game game(long id) {
        return catalog.findGameById(id).orElseThrow(() -> DomainException.notFound("Game not found"));
    }

    @Transactional
    public Game saveGame(
            Long id,
            String title,
            String description,
            BigDecimal price,
            LocalDate releaseDate,
            long developerId,
            List<Long> categoryIds) {
        if (id != null) {
            game(id);
        }
        if (price.signum() < 0) {
            throw DomainException.badRequest("price must be zero or greater");
        }
        if (categoryIds.isEmpty() || new HashSet<>(categoryIds).size() != categoryIds.size()) {
            throw DomainException.badRequest("categoryIds must contain distinct IDs and at least one category");
        }
        if (catalog.findDeveloperById(developerId).isEmpty()) {
            throw DomainException.notFound("Developer not found");
        }
        if (catalog.findCategoriesByIds(categoryIds).size() != categoryIds.size()) {
            throw DomainException.notFound("One or more categories were not found");
        }
        return catalog.saveGame(new Game(id, title, description, price, releaseDate, developerId, categoryIds));
    }

    @Transactional
    public void deleteGame(long id) {
        game(id);
        catalog.deleteGame(id);
    }

    @Transactional(readOnly = true)
    public PageResult<Achievement> achievements(PageRequest page) {
        return catalog.findAchievements(page);
    }

    @Transactional(readOnly = true)
    public Achievement achievement(long id) {
        return catalog.findAchievementById(id)
                .orElseThrow(() -> DomainException.notFound("Achievement not found"));
    }

    @Transactional
    public Achievement saveAchievement(Long id, long gameId, String name, String description) {
        if (id != null) {
            achievement(id);
        }
        game(gameId);
        return catalog.saveAchievement(new Achievement(id, gameId, name, description));
    }

    @Transactional
    public void deleteAchievement(long id) {
        achievement(id);
        catalog.deleteAchievement(id);
    }
}
