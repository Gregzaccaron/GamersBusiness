package br.com.gregfabio.gamersbusiness.infrastructure.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.gregfabio.gamersbusiness.application.port.PasswordHasher;

@Testcontainers
@SpringBootTest
@ActiveProfiles("local")
class DemoSeedIntegrationTest {
    private static final String PASSWORD = "demo-seed-integration-only";
    private static final List<String> TABLES = List.of("usuario_conquista", "avaliacao", "biblioteca",
            "jogo_categoria", "conquista", "jogo", "desenvolvedora", "categoria", "usuario", "demo_seed_state");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("gamersbusiness_demo_test")
            .withUsername("integration")
            .withPassword("integration-test-password");

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private Clock clock;

    @Autowired
    private DemoSeedRunner runner;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("DATABASE_URL", POSTGRES::getJdbcUrl);
        registry.add("DATABASE_USERNAME", POSTGRES::getUsername);
        registry.add("DATABASE_PASSWORD", POSTGRES::getPassword);
        registry.add("JWT_SECRET", () -> "demo-integration-secret-with-more-than-thirty-two-bytes");
        registry.add("BOOTSTRAP_ADMIN_ENABLED", () -> "false");
        registry.add("DEMO_SEED_ENABLED", () -> "true");
        registry.add("DEMO_USER_PASSWORD", () -> PASSWORD);
    }

    @BeforeEach
    void clearData() {
        for (String table : TABLES) {
            jdbc.update("DELETE FROM " + table);
        }
    }

    @Test
    void seedsThePersistedCatalogueAndOwnedCommunityActivity() {
        seed();

        assertCount("usuario", 14);
        assertCount("desenvolvedora", 42);
        assertCount("categoria", 37);
        assertCount("jogo", 48);
        assertCount("jogo_categoria", 142);
        assertCount("conquista", 168);
        assertCount("biblioteca", 230);
        assertCount("avaliacao", 229);
        assertCount("usuario_conquista", 453);
        assertCount("demo_seed_state", 2);
        assertEquals(14L, jdbc.queryForObject("SELECT count(*) FROM usuario WHERE papel = 'USER'", Long.class));
        assertEquals(List.of("initial-demo-catalogue-v1", "rich-demo-catalogue-v2"),
                jdbc.queryForList("SELECT seed_key FROM demo_seed_state ORDER BY seed_key", String.class));
        for (String hash : jdbc.queryForList("SELECT senha_hash FROM usuario", String.class)) {
            assertTrue(passwordHasher.matches(PASSWORD, hash));
        }
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM avaliacao a
                LEFT JOIN biblioteca b ON b.usuario_id = a.usuario_id AND b.jogo_id = a.jogo_id
                WHERE b.id IS NULL
                """, Long.class));
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM usuario_conquista uc
                JOIN conquista c ON c.id = uc.conquista_id
                LEFT JOIN biblioteca b ON b.usuario_id = uc.usuario_id AND b.jogo_id = c.jogo_id
                WHERE b.id IS NULL
                """, Long.class));
        assertRichActivityIntegrity();
        Map<String, List<Map<String, Object>>> original = snapshot();
        seed();
        assertEquals(original, snapshot());
    }

    @Test
    void completedSeedPreservesChangedCredentialsCatalogueHoursReviewsAndDeletions() {
        seed();
        jdbc.update("""
                UPDATE usuario SET nome_usuario = 'renamed-player', email = 'changed@example.test', senha_hash = ?
                WHERE nome_usuario = 'player'
                """, passwordHasher.hash("changed-by-user"));
        jdbc.update("UPDATE jogo SET titulo = 'Meu Cyberpunk', preco = 1.23 WHERE titulo = 'Cyberpunk 2077'");
        jdbc.update("UPDATE categoria SET nome = 'Categoria editada' WHERE nome = 'RPG'");
        jdbc.update("UPDATE desenvolvedora SET pais = 'País editado' WHERE nome = 'CD PROJEKT RED'");
        jdbc.update("UPDATE conquista SET nome = 'Conquista editada' WHERE nome = 'Uma nova tentativa'");
        jdbc.update("UPDATE biblioteca SET horas_jogadas = 999 WHERE id = (SELECT min(id) FROM biblioteca)");
        jdbc.update("UPDATE avaliacao SET nota = 1, comentario = 'Minha opinião mudou' WHERE id = (SELECT min(id) FROM avaliacao)");
        jdbc.update("UPDATE jogo SET titulo = 'Meu Dota', descricao = 'Texto próprio', preco = 7.89 WHERE titulo = 'Dota 2'");
        jdbc.update("""
                UPDATE usuario SET nome_usuario = 'ana_editada', email = 'ana@example.test', senha_hash = ?
                WHERE nome_usuario = 'ana_explora'
                """, passwordHasher.hash("senha-alterada"));
        deleteGame("Dead Space");
        jdbc.update("DELETE FROM usuario_conquista WHERE conquista_id = (SELECT min(conquista_id) FROM usuario_conquista)");
        jdbc.update("DELETE FROM avaliacao WHERE id = (SELECT max(id) FROM avaliacao)");

        long portal = jdbc.queryForObject("SELECT id FROM jogo WHERE titulo = 'Portal 2'", Long.class);
        jdbc.update("DELETE FROM usuario_conquista WHERE conquista_id IN (SELECT id FROM conquista WHERE jogo_id = ?)", portal);
        jdbc.update("DELETE FROM avaliacao WHERE jogo_id = ?", portal);
        jdbc.update("DELETE FROM biblioteca WHERE jogo_id = ?", portal);
        jdbc.update("DELETE FROM jogo_categoria WHERE jogo_id = ?", portal);
        jdbc.update("DELETE FROM conquista WHERE jogo_id = ?", portal);
        jdbc.update("DELETE FROM jogo WHERE id = ?", portal);

        Map<String, List<Map<String, Object>>> edited = snapshot();
        seed();
        assertEquals(edited, snapshot());
    }

    @Test
    void matchingExistingUserIsReusedWithoutReplacingItsHashOrRegistrationDate() {
        String originalHash = passwordHasher.hash(PASSWORD);
        long id = jdbc.queryForObject("""
                INSERT INTO usuario (nome_usuario, email, senha_hash, papel, data_cadastro)
                VALUES ('player', 'player@gamersbusiness.local', ?, 'USER', CURRENT_TIMESTAMP) RETURNING id
                """, Long.class, originalHash);
        Map<String, Object> original = jdbc.queryForMap("SELECT * FROM usuario WHERE id = ?", id);

        seed();

        assertEquals(original, jdbc.queryForMap("SELECT * FROM usuario WHERE id = ?", id));
        assertCount("usuario", 14);
        assertCount("biblioteca", 230);
    }

    @Test
    void ambiguousExistingGameTitlesFailAtomicallyInsteadOfChoosingAnArbitraryGame() {
        seed();
        jdbc.update("DELETE FROM demo_seed_state");
        jdbc.update("""
                INSERT INTO jogo (titulo, descricao, preco, data_lancamento, desenvolvedora_id)
                SELECT titulo, descricao, preco, data_lancamento, desenvolvedora_id
                FROM jogo WHERE titulo = 'Cyberpunk 2077'
                """);
        Map<String, List<Map<String, Object>>> original = snapshot();

        assertThrows(IllegalStateException.class, this::seed);
        assertEquals(original, snapshot());
    }

    @Test
    void identityCollisionRollsBackTheWholeSeedWithoutPromotingOrOverwritingAccounts() {
        jdbc.update("""
                INSERT INTO usuario (nome_usuario, email, senha_hash, papel, data_cadastro)
                VALUES ('someone-else', 'community@gamersbusiness.local', ?, 'ADMIN', CURRENT_TIMESTAMP)
                """, passwordHasher.hash("existing-password"));
        Map<String, List<Map<String, Object>>> original = snapshot();

        assertThrows(IllegalStateException.class, this::seed);
        assertEquals(original, snapshot());
    }

    @Test
    void existingDemoIdentityWithDifferentPasswordIsNotClaimed() {
        jdbc.update("""
                INSERT INTO usuario (nome_usuario, email, senha_hash, papel, data_cadastro)
                VALUES ('player', 'player@gamersbusiness.local', ?, 'USER', CURRENT_TIMESTAMP)
                """, passwordHasher.hash("existing-password"));
        Map<String, List<Map<String, Object>>> original = snapshot();

        assertThrows(IllegalStateException.class, this::seed);
        assertEquals(original, snapshot());
    }

    @Test
    void disabledSeedDoesNothingAndUnsafeEnabledConfigurationFailsWithoutWrites() {
        StandardEnvironment production = new StandardEnvironment();
        production.setActiveProfiles("production");
        new DemoSeedRunner(false, "", production, jdbc, passwordHasher, clock)
                .run(new DefaultApplicationArguments());
        assertCount("usuario", 0);
        assertThrows(IllegalStateException.class,
                () -> new DemoSeedRunner(true, PASSWORD, production, jdbc, passwordHasher, clock)
                        .run(new DefaultApplicationArguments()));

        StandardEnvironment local = new StandardEnvironment();
        local.setActiveProfiles("local");
        for (String invalid : List.of("", "   ", "é".repeat(37))) {
            assertThrows(IllegalStateException.class,
                    () -> new DemoSeedRunner(true, invalid, local, jdbc, passwordHasher, clock)
                            .run(new DefaultApplicationArguments()));
        }
        assertCount("usuario", 0);
        assertCount("demo_seed_state", 0);
    }

    @Test
    void originalPhaseStillSeedsItsUnchangedBaseline() {
        seedInitialOnly();
        assertCount("usuario", 2);
        assertCount("desenvolvedora", 11);
        assertCount("categoria", 12);
        assertCount("jogo", 12);
        assertCount("jogo_categoria", 34);
        assertCount("conquista", 24);
        assertCount("biblioteca", 14);
        assertCount("avaliacao", 14);
        assertCount("usuario_conquista", 24);
        assertCount("demo_seed_state", 1);
    }

    @Test
    void upgradesInPlaceWithoutReclaimingChangedOrDeletedOriginalAccountsAndCatalogue() {
        seedInitialOnly();
        long playerId = jdbc.queryForObject("SELECT id FROM usuario WHERE nome_usuario = 'player'", Long.class);
        long communityId = jdbc.queryForObject("SELECT id FROM usuario WHERE nome_usuario = 'community'", Long.class);
        jdbc.update("""
                UPDATE usuario SET nome_usuario = 'meu-player', email = 'meu@example.test',
                    senha_hash = ?, papel = 'ADMIN' WHERE id = ?
                """, passwordHasher.hash("senha-do-dono"), playerId);
        jdbc.update("DELETE FROM usuario_conquista WHERE usuario_id = ?", communityId);
        jdbc.update("DELETE FROM avaliacao WHERE usuario_id = ?", communityId);
        jdbc.update("DELETE FROM biblioteca WHERE usuario_id = ?", communityId);
        jdbc.update("DELETE FROM usuario WHERE id = ?", communityId);
        jdbc.update("UPDATE jogo SET titulo = 'Meu Cyberpunk', descricao = 'Minha descrição', preco = 1.23 WHERE titulo = 'Cyberpunk 2077'");
        jdbc.update("UPDATE desenvolvedora SET pais = 'País escolhido' WHERE nome = 'Valve'");
        jdbc.update("UPDATE categoria SET nome = 'Meu RPG' WHERE nome = 'RPG'");
        jdbc.update("UPDATE conquista SET descricao = 'Minha conquista' WHERE nome = 'Uma nova tentativa'");
        jdbc.update("UPDATE biblioteca SET horas_jogadas = 999 WHERE usuario_id = ?", playerId);
        jdbc.update("UPDATE avaliacao SET nota = 2, comentario = 'Minha avaliação' WHERE usuario_id = ?", playerId);
        deleteGame("Portal 2");
        Map<String, List<Map<String, Object>>> before = snapshot();

        seed();

        Map<String, List<Map<String, Object>>> after = snapshot();
        for (String table : TABLES) {
            assertTrue(after.get(table).containsAll(before.get(table)), "Existing rows changed in " + table);
        }
        assertCount("jogo", 47);
        assertCount("usuario", 13);
        assertCount("demo_seed_state", 2);
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM usuario WHERE nome_usuario IN ('player', 'community')
                """, Long.class));
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM jogo WHERE titulo IN ('Cyberpunk 2077', 'Portal 2')
                """, Long.class));
        assertEquals((long) before.get("biblioteca").size(), jdbc.queryForObject(
                "SELECT count(*) FROM biblioteca WHERE usuario_id = ?", Long.class, playerId));
        assertEquals((long) before.get("avaliacao").size(), jdbc.queryForObject(
                "SELECT count(*) FROM avaliacao WHERE usuario_id = ?", Long.class, playerId));
        seed();
        assertEquals(after, snapshot());
    }

    @Test
    void expansionAccountCollisionRollsBackAllNewGamesAndActivity() {
        seedInitialOnly();
        jdbc.update("""
                INSERT INTO usuario (nome_usuario, email, senha_hash, papel, data_cadastro)
                VALUES ('marcos_retro', 'other@example.test', ?, 'USER', CURRENT_TIMESTAMP)
                """, passwordHasher.hash(PASSWORD));
        Map<String, List<Map<String, Object>>> before = snapshot();

        assertThrows(IllegalStateException.class, this::seed);

        assertEquals(before, snapshot());
        assertCount("jogo", 12);
        assertCount("demo_seed_state", 1);
    }

    @Test
    void expansionEmailCollisionAlsoRollsBackTheInitialPhaseOnAFreshDatabase() {
        jdbc.update("""
                INSERT INTO usuario (nome_usuario, email, senha_hash, papel, data_cadastro)
                VALUES ('dono', 'ana_explora@gamersbusiness.local', ?, 'ADMIN', CURRENT_TIMESTAMP)
                """, passwordHasher.hash("senha-do-dono"));
        Map<String, List<Map<String, Object>>> before = snapshot();

        assertThrows(IllegalStateException.class, this::seed);

        assertEquals(before, snapshot());
        assertCount("demo_seed_state", 0);
    }

    @Test
    void existingExpansionTitleFailsSafelyWithoutChangingItsMetadataOrCategories() {
        seedInitialOnly();
        jdbc.update("""
                INSERT INTO jogo (titulo, descricao, preco, data_lancamento, desenvolvedora_id)
                SELECT 'Balatro', 'Catálogo do usuário', 12.34, data_lancamento, desenvolvedora_id
                FROM jogo WHERE titulo = 'Portal 2'
                """);
        Map<String, List<Map<String, Object>>> before = snapshot();

        assertThrows(IllegalStateException.class, this::seed);

        assertEquals(before, snapshot());
    }

    private void assertRichActivityIntegrity() {
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM biblioteca b
                JOIN usuario u ON u.id = b.usuario_id JOIN jogo j ON j.id = b.jogo_id
                WHERE b.data_aquisicao < u.data_cadastro OR b.data_aquisicao > CURRENT_TIMESTAMP
                   OR b.data_aquisicao::date < j.data_lancamento
                """, Long.class));
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM avaliacao a
                JOIN biblioteca b ON b.usuario_id = a.usuario_id AND b.jogo_id = a.jogo_id
                WHERE a.data_avaliacao < b.data_aquisicao OR a.data_avaliacao > CURRENT_TIMESTAMP
                """, Long.class));
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM usuario_conquista uc JOIN conquista c ON c.id = uc.conquista_id
                JOIN biblioteca b ON b.usuario_id = uc.usuario_id AND b.jogo_id = c.jogo_id
                WHERE uc.data_desbloqueio < b.data_aquisicao OR uc.data_desbloqueio > CURRENT_TIMESTAMP
                """, Long.class));
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM usuario_conquista uc JOIN conquista c ON c.id = uc.conquista_id
                JOIN avaliacao a ON a.usuario_id = uc.usuario_id AND a.jogo_id = c.jogo_id
                WHERE uc.data_desbloqueio > a.data_avaliacao
                """, Long.class));
        assertEquals(0L, jdbc.queryForObject("""
                SELECT count(*) FROM (
                    SELECT u.id FROM usuario u LEFT JOIN biblioteca b ON b.usuario_id = u.id
                    WHERE u.nome_usuario NOT IN ('player', 'community')
                    GROUP BY u.id HAVING count(b.id) < 12
                ) insufficient_collections
                """, Long.class));
        assertEquals(5L, jdbc.queryForObject("""
                SELECT count(DISTINCT a.nota) FROM avaliacao a JOIN usuario u ON u.id = a.usuario_id
                WHERE u.nome_usuario NOT IN ('player', 'community')
                """, Long.class));
        for (RichDemoSeedData.Entry entry : RichDemoSeedData.GAMES) {
            assertEquals(4L, jdbc.queryForObject("""
                    SELECT count(*) FROM conquista c JOIN jogo j ON j.id = c.jogo_id WHERE j.titulo = ?
                    """, Long.class, entry.game().title()));
            assertTrue(jdbc.queryForObject("""
                    SELECT count(*) FROM avaliacao a JOIN jogo j ON j.id = a.jogo_id WHERE j.titulo = ?
                    """, Long.class, entry.game().title()) >= 3, entry.game().title());
        }
        assertEquals(8L, jdbc.queryForObject("SELECT count(*) FROM jogo WHERE preco = 0", Long.class));
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM biblioteca WHERE horas_jogadas = 0", Long.class) > 0);
        assertTrue(jdbc.queryForObject("SELECT max(horas_jogadas) FROM biblioteca", Integer.class) > 300);
        assertTrue(jdbc.queryForObject("""
                SELECT count(*) FROM biblioteca b JOIN conquista c ON c.jogo_id = b.jogo_id
                LEFT JOIN usuario_conquista uc ON uc.conquista_id = c.id AND uc.usuario_id = b.usuario_id
                WHERE uc.conquista_id IS NULL
                """, Long.class) > 0);
    }

    private void seedInitialOnly() {
        jdbc.update("""
                INSERT INTO demo_seed_state (seed_key, completed_at)
                VALUES ('rich-demo-catalogue-v2', CURRENT_TIMESTAMP)
                """);
        seed();
        jdbc.update("DELETE FROM demo_seed_state WHERE seed_key = 'rich-demo-catalogue-v2'");
    }

    private void deleteGame(String title) {
        long id = jdbc.queryForObject("SELECT id FROM jogo WHERE titulo = ?", Long.class, title);
        jdbc.update("DELETE FROM usuario_conquista WHERE conquista_id IN (SELECT id FROM conquista WHERE jogo_id = ?)", id);
        jdbc.update("DELETE FROM avaliacao WHERE jogo_id = ?", id);
        jdbc.update("DELETE FROM biblioteca WHERE jogo_id = ?", id);
        jdbc.update("DELETE FROM jogo_categoria WHERE jogo_id = ?", id);
        jdbc.update("DELETE FROM conquista WHERE jogo_id = ?", id);
        jdbc.update("DELETE FROM jogo WHERE id = ?", id);
    }

    private void seed() {
        runner.run(new DefaultApplicationArguments());
    }

    private void assertCount(String table, long expected) {
        assertEquals(expected, jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class));
    }

    private Map<String, List<Map<String, Object>>> snapshot() {
        Map<String, List<Map<String, Object>>> data = new LinkedHashMap<>();
        for (String table : TABLES) {
            data.put(table, jdbc.queryForList("SELECT * FROM " + table + " ORDER BY 1, 2"));
        }
        return data;
    }
}
