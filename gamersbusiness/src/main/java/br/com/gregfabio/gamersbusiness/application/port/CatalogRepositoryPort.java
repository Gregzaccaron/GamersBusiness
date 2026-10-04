package br.com.gregfabio.gamersbusiness.application.port;

import java.util.List;
import java.util.Optional;

import br.com.gregfabio.gamersbusiness.domain.model.Achievement;
import br.com.gregfabio.gamersbusiness.domain.model.Category;
import br.com.gregfabio.gamersbusiness.domain.model.Developer;
import br.com.gregfabio.gamersbusiness.domain.model.Game;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.domain.model.PageResult;

public interface CatalogRepositoryPort {
    Developer saveDeveloper(Developer developer);

    Optional<Developer> findDeveloperById(long id);

    PageResult<Developer> findDevelopers(PageRequest page);

    void deleteDeveloper(long id);

    Category saveCategory(Category category);

    Optional<Category> findCategoryById(long id);

    List<Category> findCategoriesByIds(List<Long> ids);

    PageResult<Category> findCategories(PageRequest page);

    void deleteCategory(long id);

    Game saveGame(Game game);

    Optional<Game> findGameById(long id);

    PageResult<Game> findGames(PageRequest page, String title, Long categoryId, Long developerId);

    void deleteGame(long id);

    Achievement saveAchievement(Achievement achievement);

    Optional<Achievement> findAchievementById(long id);

    PageResult<Achievement> findAchievements(PageRequest page);

    void deleteAchievement(long id);
}
