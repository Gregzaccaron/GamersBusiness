package br.com.gregfabio.gamersbusiness.infrastructure.seed;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.gregfabio.gamersbusiness.application.port.PasswordHasher;

@Component
public class DemoSeedRunner implements ApplicationRunner {
    private static final String SEED_KEY = "initial-demo-catalogue-v1";
    private static final String RICH_SEED_KEY = "rich-demo-catalogue-v2";

    private final boolean enabled;
    private final String password;
    private final Environment environment;
    private final JdbcTemplate jdbc;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public DemoSeedRunner(
            @Value("${DEMO_SEED_ENABLED:false}") boolean enabled,
            @Value("${DEMO_USER_PASSWORD:}") String password,
            Environment environment,
            JdbcTemplate jdbc,
            PasswordHasher passwordHasher,
            Clock clock) {
        this.enabled = enabled;
        this.password = password;
        this.environment = environment;
        this.jdbc = jdbc;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (!environment.acceptsProfiles(Profiles.of("local"))) {
            throw new IllegalStateException("Demo seed is supported only with the local profile");
        }
        if (password.isBlank() || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("DEMO_USER_PASSWORD must be provided and contain at most 72 UTF-8 bytes");
        }

        // The catalogue has no unique title constraint; serialize seed runs across local API instances.
        jdbc.execute("SELECT pg_advisory_xact_lock(17320, 1)");
        Instant now = clock.instant();
        if (!exists("SELECT count(*) FROM demo_seed_state WHERE seed_key = ?", SEED_KEY)) {
            seedInitial(now);
        }
        if (!exists("SELECT count(*) FROM demo_seed_state WHERE seed_key = ?", RICH_SEED_KEY)) {
            seedRich(now);
        }
        // Completed phases are never reconciled: later edits and deletions belong to the user.
    }

    private void seedInitial(Instant now) {
        Map<String, Long> users = Map.of(
                "player", seedUser("player", "player@gamersbusiness.local", now),
                "community", seedUser("community", "community@gamersbusiness.local", now));
        Map<String, Long> developers = seedDevelopers(DemoSeedData.DEVELOPERS);

        Map<String, Long> games = new HashMap<>();
        Map<String, List<Long>> achievements = new HashMap<>();
        for (DemoSeedData.Game game : DemoSeedData.GAMES) {
            long gameId = seedGame(game, developers.get(game.developer()));
            games.put(game.title(), gameId);
            achievements.put(game.title(), game.achievements().stream()
                    .map(achievement -> seedAchievement(gameId, achievement))
                    .toList());
        }

        for (int index = 0; index < DemoSeedData.PLAYS.size(); index++) {
            DemoSeedData.Play play = DemoSeedData.PLAYS.get(index);
            long userId = users.get(play.username());
            long gameId = games.get(play.game());
            seedPlay(userId, gameId, play, now.minus(45L - index, ChronoUnit.DAYS),
                    now.minus(20L - index, ChronoUnit.DAYS));
            for (int unlocked = 0; unlocked < play.unlocked(); unlocked++) {
                seedUnlock(userId, achievements.get(play.game()).get(unlocked),
                        now.minus(25L - index, ChronoUnit.DAYS));
            }
        }
        complete(SEED_KEY, now);
    }

    private Map<String, Long> seedDevelopers(List<DemoSeedData.Developer> catalogue) {
        Map<String, Long> developers = new HashMap<>();
        for (DemoSeedData.Developer developer : catalogue) {
            Long id = findId("SELECT id FROM desenvolvedora WHERE nome = ?", developer.name());
            if (id == null) {
                id = jdbc.queryForObject("""
                        INSERT INTO desenvolvedora (nome, pais, data_fundacao)
                        VALUES (?, ?, ?) RETURNING id
                        """, Long.class, developer.name(), developer.country(),
                        Date.valueOf(LocalDate.parse(developer.foundedAt())));
            }
            developers.put(developer.name(), id);
        }
        return developers;
    }

    private void seedRich(Instant now) {
        // Refuse even an unambiguous title collision: adding categories or achievements would change user data.
        for (RichDemoSeedData.Entry entry : RichDemoSeedData.GAMES) {
            if (exists("SELECT count(*) FROM jogo WHERE titulo = ?", entry.game().title())) {
                throw new IllegalStateException("Rich demo title collides with an existing game: " + entry.game().title());
            }
        }
        Map<String, Long> developers = seedDevelopers(RichDemoSeedData.DEVELOPERS);
        // These developers already belong to V1; reuse their current rows without changing any metadata.
        for (String name : List.of("Valve", "FromSoftware")) {
            Long id = findId("SELECT id FROM desenvolvedora WHERE nome = ?", name);
            if (id == null) {
                DemoSeedData.Developer baseline = DemoSeedData.DEVELOPERS.stream()
                        .filter(developer -> name.equals(developer.name())).findFirst().orElseThrow();
                id = seedDevelopers(List.of(baseline)).get(name);
            }
            developers.put(name, id);
        }
        Map<String, Long> games = new HashMap<>();
        Map<String, List<Long>> achievements = new HashMap<>();
        for (RichDemoSeedData.Entry entry : RichDemoSeedData.GAMES) {
            DemoSeedData.Game game = entry.game();
            long gameId = seedGame(game, developers.get(game.developer()));
            games.put(game.title(), gameId);
            achievements.put(game.title(), game.achievements().stream()
                    .map(achievement -> seedAchievement(gameId, achievement)).toList());
        }
        for (int playerIndex = 0; playerIndex < RichDemoSeedData.PLAYERS.size(); playerIndex++) {
            RichDemoSeedData.Player player = RichDemoSeedData.PLAYERS.get(playerIndex);
            long userId = seedUser(player.username(), player.username() + "@gamersbusiness.local",
                    now.minus(365L + playerIndex * 5L, ChronoUnit.DAYS), false);
            for (int index = 0; index < player.games().size(); index++) {
                int catalogueIndex = player.games().get(index);
                RichDemoSeedData.Entry entry = RichDemoSeedData.GAMES.get(catalogueIndex);
                long gameId = games.get(entry.game().title());
                int hours = playerIndex == 0 && index == 0 ? 0 : 1 + (playerIndex * 43 + index * 29) % 400;
                int rating = 1 + (playerIndex + catalogueIndex) % 5;
                DemoSeedData.Play play = new DemoSeedData.Play(player.username(), entry.game().title(), hours,
                        rating, entry.review(rating), hours == 0 ? 0 : (playerIndex + index) % 5);
                seedPlay(userId, gameId, play,
                        now.minus(270L - index * 6L - playerIndex * 3L, ChronoUnit.DAYS),
                        now.minus(90L - index * 3L - playerIndex, ChronoUnit.DAYS));
                for (int unlocked = 0; unlocked < play.unlocked(); unlocked++) {
                    seedUnlock(userId, achievements.get(play.game()).get(unlocked),
                            now.minus(120L - index * 3L - playerIndex * 2L - unlocked, ChronoUnit.DAYS));
                }
            }
        }
        complete(RICH_SEED_KEY, now);
    }

    private void seedUnlock(long userId, long achievementId, Instant unlockedAt) {
        jdbc.update("""
                INSERT INTO usuario_conquista (usuario_id, conquista_id, data_desbloqueio)
                VALUES (?, ?, ?) ON CONFLICT (usuario_id, conquista_id) DO NOTHING
                """, userId, achievementId, Timestamp.from(unlockedAt));
    }

    private void complete(String key, Instant now) {
        jdbc.update("INSERT INTO demo_seed_state (seed_key, completed_at) VALUES (?, ?)", key, Timestamp.from(now));
    }

    private long seedUser(String username, String email, Instant now) {
        return seedUser(username, email, now.minus(60, ChronoUnit.DAYS), true);
    }

    private long seedUser(String username, String email, Instant registeredAt, boolean reuseExisting) {
        List<Account> accounts = jdbc.query("""
                SELECT id, nome_usuario, email, senha_hash, papel FROM usuario
                WHERE nome_usuario = ? OR email = ?
                """, (row, index) -> new Account(row.getLong("id"), row.getString("nome_usuario"),
                        row.getString("email"), row.getString("senha_hash"), row.getString("papel")),
                username, email);
        if (!accounts.isEmpty()) {
            if (reuseExisting && accounts.size() == 1) {
                Account account = accounts.get(0);
                if (username.equals(account.username()) && email.equals(account.email())
                        && "USER".equals(account.role()) && passwordHasher.matches(password, account.passwordHash())) {
                    return account.id();
                }
            }
            throw new IllegalStateException("Demo identity collides with an existing account: " + email);
        }
        return jdbc.queryForObject("""
                INSERT INTO usuario (nome_usuario, email, senha_hash, papel, data_cadastro)
                VALUES (?, ?, ?, 'USER', ?) RETURNING id
                """, Long.class, username, email, passwordHasher.hash(password),
                Timestamp.from(registeredAt));
    }

    private long seedGame(DemoSeedData.Game game, long developerId) {
        Long id = findId("SELECT id FROM jogo WHERE titulo = ?", game.title());
        if (id == null) {
            id = jdbc.queryForObject("""
                    INSERT INTO jogo (titulo, descricao, preco, data_lancamento, desenvolvedora_id)
                    VALUES (?, ?, ?, ?, ?) RETURNING id
                    """, Long.class, game.title(), game.description(), game.price(),
                    Date.valueOf(game.releasedAt()), developerId);
        }
        for (String category : game.categories()) {
            Long categoryId = findId("SELECT id FROM categoria WHERE nome = ?", category);
            if (categoryId == null) {
                categoryId = jdbc.queryForObject("INSERT INTO categoria (nome) VALUES (?) RETURNING id",
                        Long.class, category);
            }
            jdbc.update("""
                    INSERT INTO jogo_categoria (jogo_id, categoria_id) VALUES (?, ?)
                    ON CONFLICT (jogo_id, categoria_id) DO NOTHING
                    """, id, categoryId);
        }
        return id;
    }

    private long seedAchievement(long gameId, DemoSeedData.Achievement achievement) {
        Long id = findId("SELECT id FROM conquista WHERE jogo_id = ? AND nome = ?", gameId, achievement.name());
        if (id == null) {
            id = jdbc.queryForObject("""
                    INSERT INTO conquista (jogo_id, nome, descricao) VALUES (?, ?, ?) RETURNING id
                    """, Long.class, gameId, achievement.name(), achievement.description());
        }
        return id;
    }

    private void seedPlay(long userId, long gameId, DemoSeedData.Play play, Instant acquiredAt, Instant reviewedAt) {
        jdbc.update("""
                INSERT INTO biblioteca (usuario_id, jogo_id, data_aquisicao, preco_pago, horas_jogadas)
                SELECT ?, id, ?, preco, ? FROM jogo WHERE id = ?
                ON CONFLICT (usuario_id, jogo_id) DO NOTHING
                """, userId, Timestamp.from(acquiredAt), play.hours(), gameId);
        if (play.hours() == 0) {
            return;
        }
        jdbc.update("""
                INSERT INTO avaliacao (jogo_id, usuario_id, nota, comentario, data_avaliacao)
                VALUES (?, ?, ?, ?, ?) ON CONFLICT (usuario_id, jogo_id) DO NOTHING
                """, gameId, userId, play.rating(), play.review(),
                Timestamp.from(reviewedAt));
    }

    private Long findId(String sql, Object... parameters) {
        List<Long> ids = jdbc.queryForList(sql, Long.class, parameters);
        if (ids.size() > 1) {
            throw new IllegalStateException("Demo seed found an ambiguous existing catalogue identity");
        }
        return ids.isEmpty() ? null : ids.get(0);
    }

    private boolean exists(String sql, Object... parameters) {
        Long count = jdbc.queryForObject(sql, Long.class, parameters);
        return count != null && count > 0;
    }

    private record Account(long id, String username, String email, String passwordHash, String role) {
    }
}
