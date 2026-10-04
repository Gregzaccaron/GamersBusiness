package br.com.gregfabio.gamersbusiness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gregfabio.gamersbusiness.application.service.AuthService;
import br.com.gregfabio.gamersbusiness.domain.model.UserAccount;
import br.com.gregfabio.gamersbusiness.infrastructure.security.BootstrapAdminRunner;
import jakarta.validation.Validator;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class GamersbusinessApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("gamersbusiness")
            .withUsername("integration")
            .withPassword("integration-test-password");

    private static final String JWT_SECRET = "integration-test-secret-with-more-than-thirty-two-bytes";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AuthService authService;

    @Autowired
    private Validator validator;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("DATABASE_URL", POSTGRES::getJdbcUrl);
        registry.add("DATABASE_USERNAME", POSTGRES::getUsername);
        registry.add("DATABASE_PASSWORD", POSTGRES::getPassword);
        registry.add("JWT_SECRET", () -> JWT_SECRET);
        registry.add("BOOTSTRAP_ADMIN_ENABLED", () -> "false");
    }

    @BeforeEach
    void clearBusinessData() {
        jdbc.update("DELETE FROM usuario_conquista");
        jdbc.update("DELETE FROM avaliacao");
        jdbc.update("DELETE FROM biblioteca");
        jdbc.update("DELETE FROM jogo_categoria");
        jdbc.update("DELETE FROM conquista");
        jdbc.update("DELETE FROM jogo");
        jdbc.update("DELETE FROM desenvolvedora");
        jdbc.update("DELETE FROM categoria");
        jdbc.update("DELETE FROM usuario");
    }

    @Test
    void flywayBuildsTheNineTablesOnAnEmptyPostgres17Database() {
        Integer tableCount = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name IN "
                        + "('desenvolvedora', 'jogo', 'categoria', 'jogo_categoria', 'usuario', "
                        + "'conquista', 'biblioteca', 'avaliacao', 'usuario_conquista')",
                Integer.class);
        assertEquals(9, tableCount);
        assertEquals("1", jdbc.queryForObject(
                "SELECT version FROM flyway_schema_history WHERE version = '1' AND success = true",
                String.class));
        Integer cascadingForeignKeys = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.referential_constraints "
                        + "WHERE constraint_schema = 'public' AND delete_rule = 'CASCADE'",
                Integer.class);
        assertEquals(0, cascadingForeignKeys);
    }

    @Test
    void registrationAndLoginReturnSafeProfilesAndUniformErrors() throws Exception {
        String suffix = suffix();
        String username = "player-" + suffix;
        String email = username + "@example.test";
        String password = "Player-password-123!";

        ResponseEntity<String> privilegedRegistration = api(HttpMethod.POST, "/api/v1/auth/register", null,
                Map.of("username", username, "email", email, "password", password, "role", "ADMIN"));
        assertUniformError(privilegedRegistration, 400, "/api/v1/auth/register");

        ResponseEntity<String> created = api(HttpMethod.POST, "/api/v1/auth/register", null,
                Map.of("username", username, "email", email, "password", password));
        assertEquals(201, created.getStatusCode().value());
        JsonNode profile = json(created);
        assertEquals("USER", profile.path("role").asText());
        assertFalse(profile.has("password"));
        assertFalse(profile.has("passwordHash"));
        assertFalse(profile.has("senha_hash"));

        ResponseEntity<String> duplicate = api(HttpMethod.POST, "/api/v1/auth/register", null,
                Map.of("username", username, "email", email, "password", password));
        assertUniformError(duplicate, 409, "/api/v1/auth/register");

        ResponseEntity<String> invalidLogin = api(HttpMethod.POST, "/api/v1/auth/login", null,
                Map.of("email", email, "password", "not-the-password"));
        assertUniformError(invalidLogin, 401, "/api/v1/auth/login");

        String token = login(email, password);
        ResponseEntity<String> unauthenticated = api(HttpMethod.GET, "/api/v1/me", null, null);
        assertUniformError(unauthenticated, 401, "/api/v1/me");
        ResponseEntity<String> currentProfile = api(HttpMethod.GET, "/api/v1/me", token, null);
        assertEquals(200, currentProfile.getStatusCode().value());
        assertEquals(username, json(currentProfile).path("username").asText());
    }

    @Test
    void catalogFiltersAreCombinedAndStableAndReferencesConflictCorrectly() throws Exception {
        UserSession admin = createAdmin();
        long developerOne = createDeveloper(admin.token(), "Studio One");
        long developerTwo = createDeveloper(admin.token(), "Studio Two");
        long categoryOne = createCategory(admin.token(), "Action");
        long categoryTwo = createCategory(admin.token(), "Puzzle");

        long firstMatch = createGame(admin.token(), "Alpha One", developerOne, List.of(categoryOne), "10.00");
        long secondMatch = createGame(admin.token(), "Alpha Two", developerOne, List.of(categoryOne), "11.00");
        createGame(admin.token(), "Alpha Other Studio", developerTwo, List.of(categoryOne), "12.00");
        createGame(admin.token(), "Beta Puzzle", developerOne, List.of(categoryTwo), "13.00");

        ResponseEntity<String> firstPage = api(HttpMethod.GET,
                "/api/v1/games?title=ALPHA&categoryId=" + categoryOne + "&developerId=" + developerOne
                        + "&page=0&size=1",
                admin.token(), null);
        assertEquals(200, firstPage.getStatusCode().value());
        JsonNode firstPageBody = json(firstPage);
        assertEquals(2, firstPageBody.path("totalElements").asLong());
        assertEquals(2, firstPageBody.path("totalPages").asInt());
        assertEquals(firstMatch, firstPageBody.path("items").get(0).path("id").asLong());

        ResponseEntity<String> secondPage = api(HttpMethod.GET,
                "/api/v1/games?title=alpha&categoryId=" + categoryOne + "&developerId=" + developerOne
                        + "&page=1&size=1",
                admin.token(), null);
        assertEquals(secondMatch, json(secondPage).path("items").get(0).path("id").asLong());

        ResponseEntity<String> absentFilter = api(HttpMethod.GET, "/api/v1/games?categoryId=999999",
                admin.token(), null);
        assertEquals(200, absentFilter.getStatusCode().value());
        assertEquals(0, json(absentFilter).path("totalElements").asInt());

        ResponseEntity<String> invalidPagination = api(HttpMethod.GET, "/api/v1/games?page=-1",
                admin.token(), null);
        assertUniformError(invalidPagination, 400, "/api/v1/games");

        ResponseEntity<String> duplicateCategory = api(HttpMethod.POST, "/api/v1/categories", admin.token(),
                Map.of("name", "Action"));
        assertUniformError(duplicateCategory, 409, "/api/v1/categories");

        ResponseEntity<String> duplicateCategories = api(HttpMethod.POST, "/api/v1/games", admin.token(),
                gameRequest("Bad Duplicate", developerOne, List.of(categoryOne, categoryOne), "10.00"));
        assertUniformError(duplicateCategories, 400, "/api/v1/games");

        ResponseEntity<String> negativePrice = api(HttpMethod.POST, "/api/v1/games", admin.token(),
                gameRequest("Negative", developerOne, List.of(categoryOne), "-1.00"));
        assertUniformError(negativePrice, 400, "/api/v1/games");

        ResponseEntity<String> missingDeveloper = api(HttpMethod.POST, "/api/v1/games", admin.token(),
                gameRequest("Missing developer", Long.MAX_VALUE, List.of(categoryOne), "1.00"));
        assertUniformError(missingDeveloper, 404, "/api/v1/games");

        long achievementId = createAchievement(admin.token(), firstMatch, "First Win");
        assertTrue(achievementId > 0);
        assertUniformError(api(HttpMethod.DELETE, "/api/v1/categories/" + categoryOne, admin.token(), null),
                409, "/api/v1/categories/" + categoryOne);
        assertUniformError(api(HttpMethod.DELETE, "/api/v1/developers/" + developerOne, admin.token(), null),
                409, "/api/v1/developers/" + developerOne);
        assertUniformError(api(HttpMethod.DELETE, "/api/v1/games/" + firstMatch, admin.token(), null),
                409, "/api/v1/games/" + firstMatch);
    }

    @Test
    void ownershipAndHistoricalRecordsSurviveLibraryRemovalAndReacquisition() throws Exception {
        UserSession admin = createAdmin();
        long developer = createDeveloper(admin.token(), "History Studio");
        long category = createCategory(admin.token(), "History");
        long game = createGame(admin.token(), "History Game", developer, List.of(category), "19.99");
        long firstAchievement = createAchievement(admin.token(), game, "First Badge");
        long secondAchievement = createAchievement(admin.token(), game, "Second Badge");
        UserSession owner = registerAndLogin();
        UserSession other = registerAndLogin();

        ResponseEntity<String> acquisition = api(HttpMethod.POST, "/api/v1/me/library", owner.token(),
                Map.of("gameId", game));
        assertEquals(201, acquisition.getStatusCode().value());
        JsonNode acquired = json(acquisition);
        long firstEntryId = acquired.path("id").asLong();
        assertEquals(new BigDecimal("19.99"), new BigDecimal(acquired.path("paidPrice").asText()));

        assertUniformError(api(HttpMethod.POST, "/api/v1/me/library", owner.token(), Map.of("gameId", game)),
                409, "/api/v1/me/library");
        assertEquals(404, api(HttpMethod.PATCH, "/api/v1/me/library/" + firstEntryId, other.token(),
                Map.of("hoursPlayed", 2)).getStatusCode().value());
        assertUniformError(api(HttpMethod.PATCH, "/api/v1/me/library/" + firstEntryId, owner.token(),
                Map.of("hoursPlayed", -1)), 400, "/api/v1/me/library/" + firstEntryId);

        ResponseEntity<String> review = api(HttpMethod.POST, "/api/v1/games/" + game + "/reviews", owner.token(),
                Map.of("rating", 5, "comment", "Worth replaying"));
        assertEquals(201, review.getStatusCode().value());
        long reviewId = json(review).path("id").asLong();
        assertUniformError(api(HttpMethod.POST, "/api/v1/games/" + game + "/reviews", owner.token(),
                Map.of("rating", 4)), 409, "/api/v1/games/" + game + "/reviews");
        assertUniformError(api(HttpMethod.POST, "/api/v1/games/" + game + "/reviews", other.token(),
                Map.of("rating", 4)), 403, "/api/v1/games/" + game + "/reviews");

        ResponseEntity<String> unlocked = api(HttpMethod.POST, "/api/v1/me/achievements", owner.token(),
                Map.of("achievementId", firstAchievement));
        assertEquals(201, unlocked.getStatusCode().value());
        assertUniformError(api(HttpMethod.POST, "/api/v1/me/achievements", owner.token(),
                Map.of("achievementId", firstAchievement)), 409, "/api/v1/me/achievements");
        assertUniformError(api(HttpMethod.POST, "/api/v1/me/achievements", other.token(),
                Map.of("achievementId", firstAchievement)), 403, "/api/v1/me/achievements");

        assertEquals(204, api(HttpMethod.DELETE, "/api/v1/me/library/" + firstEntryId, owner.token(), null)
                .getStatusCode().value());
        JsonNode afterRemoval = json(api(HttpMethod.GET, "/api/v1/games/" + game + "/reviews", owner.token(), null));
        assertEquals(1, afterRemoval.path("reviewCount").asLong());
        assertEquals(5.0, afterRemoval.path("averageRating").asDouble());
        assertUniformError(api(HttpMethod.PATCH, "/api/v1/reviews/" + reviewId, owner.token(),
                java.util.Collections.singletonMap("comment", null)), 403, "/api/v1/reviews/" + reviewId);
        assertUniformError(api(HttpMethod.POST, "/api/v1/me/achievements", owner.token(),
                Map.of("achievementId", secondAchievement)), 403, "/api/v1/me/achievements");

        ResponseEntity<String> priceUpdate = api(HttpMethod.PUT, "/api/v1/games/" + game, admin.token(),
                gameRequest("History Game", developer, List.of(category), "29.50"));
        assertEquals(200, priceUpdate.getStatusCode().value());
        ResponseEntity<String> reacquisitionResponse = api(HttpMethod.POST, "/api/v1/me/library", owner.token(),
                Map.of("gameId", game));
        assertEquals(201, reacquisitionResponse.getStatusCode().value());
        JsonNode reacquisition = json(reacquisitionResponse);
        long secondEntryId = reacquisition.path("id").asLong();
        assertNotEquals(firstEntryId, secondEntryId);
        assertEquals(new BigDecimal("29.50"), new BigDecimal(reacquisition.path("paidPrice").asText()));
        assertUniformError(api(HttpMethod.POST, "/api/v1/games/" + game + "/reviews", owner.token(),
                Map.of("rating", 4)), 409, "/api/v1/games/" + game + "/reviews");
        assertUniformError(api(HttpMethod.POST, "/api/v1/me/achievements", owner.token(),
                Map.of("achievementId", firstAchievement)), 409, "/api/v1/me/achievements");
        assertEquals(201, api(HttpMethod.POST, "/api/v1/me/achievements", owner.token(),
                Map.of("achievementId", secondAchievement)).getStatusCode().value());

        ResponseEntity<String> achievementHistory = api(HttpMethod.GET,
                "/api/v1/me/achievements?gameId=" + game, owner.token(), null);
        assertEquals(2, json(achievementHistory).path("totalElements").asInt());
        assertEquals(204, api(HttpMethod.DELETE, "/api/v1/me/library/" + secondEntryId, owner.token(), null)
                .getStatusCode().value());
        assertEquals(204, api(HttpMethod.DELETE, "/api/v1/reviews/" + reviewId, owner.token(), null)
                .getStatusCode().value());
        JsonNode emptyAggregate = json(api(HttpMethod.GET, "/api/v1/games/" + game + "/reviews", owner.token(), null));
        assertEquals(0, emptyAggregate.path("reviewCount").asInt());
        assertTrue(emptyAggregate.path("averageRating").isNull());
    }

    @Test
    void jwtReadsAccountAndRoleFromDatabaseAndLocalDocsStayPublic() throws Exception {
        UserSession admin = createAdmin();
        UserSession player = registerAndLogin();

        ResponseEntity<String> swagger = api(HttpMethod.GET, "/v3/api-docs", null, null);
        assertEquals(200, swagger.getStatusCode().value());
        JsonNode openApi = json(swagger);
        assertTrue(openApi.path("components").path("securitySchemes").has("bearerAuth"));
        assertTrue(openApi.path("paths").path("/api/v1/games").path("get").path("security").isArray());
        assertTrue(api(HttpMethod.GET, "/swagger-ui/index.html", null, null)
                .getStatusCode().is2xxSuccessful());

        assertUniformError(api(HttpMethod.GET, "/api/v1/me", null, null), 401, "/api/v1/me");
        assertUniformError(api(HttpMethod.GET, "/api/v1/me", "not-a-jwt", null), 401, "/api/v1/me");
        assertUniformError(api(HttpMethod.POST, "/api/v1/categories", player.token(), Map.of("name", "Denied")),
                403, "/api/v1/categories");

        assertEquals(204, api(HttpMethod.DELETE, "/api/v1/users/" + player.id(), admin.token(), null)
                .getStatusCode().value());
        assertUniformError(api(HttpMethod.GET, "/api/v1/me", player.token(), null), 401, "/api/v1/me");

        jdbc.update("UPDATE usuario SET papel = 'USER' WHERE id = ?", admin.id());
        assertUniformError(api(HttpMethod.GET, "/api/v1/users", admin.token(), null), 403, "/api/v1/users");
    }

    @Test
    void bootstrapIsLocalExplicitIdempotentAndNeverReplacesAnExistingPassword() throws Exception {
        String username = "bootstrap-" + suffix();
        String email = username + "@example.test";
        String originalPassword = "Bootstrap-original-123!";
        StandardEnvironment local = new StandardEnvironment();
        local.setActiveProfiles("local");

        new BootstrapAdminRunner(true, username, email, originalPassword, local, validator, authService).run(null);
        String originalHash = jdbc.queryForObject(
                "SELECT senha_hash FROM usuario WHERE email = ?", String.class, email);
        new BootstrapAdminRunner(true, username, email, "Bootstrap-replacement-456!", local, validator, authService)
                .run(null);
        assertEquals(originalHash, jdbc.queryForObject(
                "SELECT senha_hash FROM usuario WHERE email = ?", String.class, email));
        assertNotNull(login(email, originalPassword));
        assertUniformError(api(HttpMethod.POST, "/api/v1/auth/login", null,
                Map.of("email", email, "password", "Bootstrap-replacement-456!")),
                401, "/api/v1/auth/login");

        assertThrows(IllegalStateException.class, () -> new BootstrapAdminRunner(
                true, "outside", "outside@example.test", "not-stored", new StandardEnvironment(), validator, authService)
                .run(null));
        assertThrows(IllegalStateException.class, () -> new BootstrapAdminRunner(
                true, "", "", "", local, validator, authService).run(null));
    }

    private UserSession registerAndLogin() throws Exception {
        String username = "player-" + suffix();
        String email = username + "@example.test";
        String password = "Player-password-123!";
        ResponseEntity<String> created = api(HttpMethod.POST, "/api/v1/auth/register", null,
                Map.of("username", username, "email", email, "password", password));
        assertEquals(201, created.getStatusCode().value());
        long id = json(created).path("id").asLong();
        return new UserSession(id, username, email, password, login(email, password));
    }

    private UserSession createAdmin() throws Exception {
        String username = "admin-" + suffix();
        String email = username + "@example.test";
        String password = "Admin-password-123!";
        UserAccount account = authService.provisionAdmin(username, email, password);
        return new UserSession(account.id(), username, email, password, login(email, password));
    }

    private String login(String email, String password) throws Exception {
        ResponseEntity<String> result = api(HttpMethod.POST, "/api/v1/auth/login", null,
                Map.of("email", email, "password", password));
        assertEquals(200, result.getStatusCode().value());
        JsonNode token = json(result);
        assertEquals("Bearer", token.path("tokenType").asText());
        assertNotNull(token.path("expiresAt").textValue());
        return token.path("accessToken").asText();
    }

    private long createDeveloper(String token, String name) throws Exception {
        ResponseEntity<String> result = api(HttpMethod.POST, "/api/v1/developers", token,
                Map.of("name", name, "country", "US", "foundationDate", "2010-01-01"));
        assertEquals(201, result.getStatusCode().value());
        return json(result).path("id").asLong();
    }

    private long createCategory(String token, String name) throws Exception {
        ResponseEntity<String> result = api(HttpMethod.POST, "/api/v1/categories", token, Map.of("name", name));
        assertEquals(201, result.getStatusCode().value());
        return json(result).path("id").asLong();
    }

    private long createGame(String token, String title, long developer, List<Long> categories, String price)
            throws Exception {
        ResponseEntity<String> result = api(HttpMethod.POST, "/api/v1/games", token,
                gameRequest(title, developer, categories, price));
        assertEquals(201, result.getStatusCode().value());
        return json(result).path("id").asLong();
    }

    private long createAchievement(String token, long gameId, String name) throws Exception {
        ResponseEntity<String> result = api(HttpMethod.POST, "/api/v1/achievements", token,
                Map.of("gameId", gameId, "name", name, "description", "Complete an in-game milestone"));
        assertEquals(201, result.getStatusCode().value());
        return json(result).path("id").asLong();
    }

    private static Map<String, Object> gameRequest(String title, long developer, List<Long> categories, String price) {
        return Map.of(
                "title", title,
                "description", "Integration test game",
                "price", new BigDecimal(price),
                "releaseDate", "2025-01-15",
                "developerId", developer,
                "categoryIds", categories);
    }

    private ResponseEntity<String> api(HttpMethod method, String path, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return client.exchange(
                "http://localhost:" + port + path,
                method,
                new HttpEntity<>(body, headers),
                String.class);
    }

    private JsonNode json(ResponseEntity<String> response) throws Exception {
        assertNotNull(response.getBody());
        return objectMapper.readTree(response.getBody());
    }

    private void assertUniformError(ResponseEntity<String> response, int status, String path) throws Exception {
        assertEquals(status, response.getStatusCode().value());
        JsonNode error = json(response);
        assertTrue(error.hasNonNull("timestamp"));
        assertEquals(status, error.path("status").asInt());
        assertTrue(error.path("message").isTextual());
        assertEquals(path, error.path("path").asText());
        assertTrue(error.path("errors").isObject());
    }

    private static String suffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private record UserSession(long id, String username, String email, String password, String token) {
    }
}
