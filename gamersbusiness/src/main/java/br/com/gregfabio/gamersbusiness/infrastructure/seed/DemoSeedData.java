package br.com.gregfabio.gamersbusiness.infrastructure.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

final class DemoSeedData {
    private DemoSeedData() {
    }

    static final List<Developer> DEVELOPERS = List.of(
            new Developer("CD PROJEKT RED", "Polônia", "2002-02-01"),
            new Developer("Rockstar Games", "Estados Unidos", "1998-12-01"),
            new Developer("FromSoftware", "Japão", "1986-11-01"),
            new Developer("Team Cherry", "Austrália", "2014-11-19"),
            new Developer("ConcernedApe", "Estados Unidos", "2012-01-01"),
            new Developer("Supergiant Games", "Estados Unidos", "2009-09-01"),
            new Developer("Larian Studios", "Bélgica", "1996-01-01"),
            new Developer("Playground Games", "Reino Unido", "2010-01-01"),
            new Developer("Santa Monica Studio", "Estados Unidos", "1999-01-01"),
            new Developer("Valve", "Estados Unidos", "1996-08-24"),
            new Developer("Maddy Makes Games", "Canadá", "2012-01-01"));

    static final List<Game> GAMES = List.of(
            game("Cyberpunk 2077", "Explore Night City como um mercenário em busca de uma segunda chance. "
                    + "Uma aventura de RPG com escolhas, implantes e histórias em uma metrópole futurista.",
                    "199.90", "2020-12-10", "CD PROJEKT RED", List.of("RPG", "Mundo aberto", "Ação"),
                    achievement("Primeiros passos em Night City", "Conclua a primeira missão da campanha."),
                    achievement("Lenda do asfalto", "Complete dez atividades pelas ruas de Night City.")),
            game("Red Dead Redemption 2", "Acompanhe Arthur Morgan e sua gangue no fim da era dos fora da lei. "
                    + "Cavalgue por um mundo aberto de fronteiras, cidades e encontros inesperados.",
                    "249.90", "2019-12-05", "Rockstar Games", List.of("Aventura", "Mundo aberto", "Ação"),
                    achievement("Vida de fora da lei", "Conclua o primeiro capítulo da história."),
                    achievement("Caminhos da fronteira", "Explore cinco regiões a cavalo.")),
            game("ELDEN RING", "Viaje pelas Terras Intermédias em busca dos fragmentos do Anel Prístino. "
                    + "Enfrente adversários desafiadores e crie seu próprio estilo de combate e exploração.",
                    "229.90", "2022-02-25", "FromSoftware", List.of("RPG", "Mundo aberto", "Ação"),
                    achievement("A primeira graça", "Encontre seu primeiro local de graça."),
                    achievement("Além da névoa", "Derrote seu primeiro chefe principal.")),
            game("Hollow Knight", "Desça às ruínas de Hallownest em uma aventura desenhada à mão. "
                    + "Descubra passagens, aprimore habilidades e enfrente criaturas de um reino esquecido.",
                    "46.99", "2017-02-24", "Team Cherry", List.of("Indie", "Metroidvania", "Aventura"),
                    achievement("Sob a superfície", "Chegue à primeira área de Hallownest."),
                    achievement("Explorador de Hallownest", "Encontre cinco novas áreas do reino.")),
            game("Stardew Valley", "Transforme uma fazenda herdada em um novo lar no campo. "
                    + "Cultive alimentos, faça amizades e descubra os segredos de uma pequena comunidade.",
                    "24.99", "2016-02-26", "ConcernedApe", List.of("Indie", "Simulação", "RPG"),
                    achievement("Primeira colheita", "Colha sua primeira plantação."),
                    achievement("Laços no vale", "Alcance dois corações de amizade com um morador.")),
            game("Hades", "Tente escapar do submundo como Zagreus, filho de Hades. "
                    + "Combine bênçãos dos deuses, armas e novas estratégias a cada tentativa.",
                    "73.99", "2020-09-17", "Supergiant Games", List.of("Indie", "Roguelike", "Ação"),
                    achievement("Uma nova tentativa", "Complete sua primeira tentativa de fuga."),
                    achievement("Bênçãos do Olimpo", "Receba bênçãos de cinco deuses diferentes.")),
            game("Baldur's Gate 3", "Reúna seu grupo em uma aventura de escolhas inspirada em Dungeons & Dragons. "
                    + "Explore cenários cheios de possibilidades e enfrente combates táticos por turnos.",
                    "199.99", "2023-08-03", "Larian Studios", List.of("RPG", "Estratégia", "Aventura"),
                    achievement("Um grupo improvável", "Recrute seu primeiro companheiro."),
                    achievement("O destino é seu", "Resolva uma missão importante por meio do diálogo.")),
            game("Forza Horizon 5", "Dirija por estradas, desertos e cidades inspirados no México. "
                    + "Monte sua coleção de carros e participe de provas de um festival automobilístico.",
                    "249.00", "2021-11-09", "Playground Games", List.of("Corrida", "Mundo aberto", "Simulação"),
                    achievement("Bem-vindo ao festival", "Complete sua primeira corrida do festival."),
                    achievement("Garagem em expansão", "Adicione cinco veículos à sua coleção.")),
            game("God of War", "Acompanhe Kratos e Atreus em uma jornada pelos reinos da mitologia nórdica. "
                    + "Uma aventura de ação sobre família, legado e confrontos com criaturas lendárias.",
                    "199.90", "2022-01-14", "Santa Monica Studio", List.of("Ação", "Aventura"),
                    achievement("O início da jornada", "Parta com Atreus na primeira etapa da viagem."),
                    achievement("Força espartana", "Aprimore uma habilidade de combate.")),
            game("The Witcher 3: Wild Hunt", "Assuma o papel de Geralt de Rívia na busca por Ciri. "
                    + "Investigue contratos de monstros e faça escolhas em um mundo de fantasia e conflitos.",
                    "129.99", "2015-05-19", "CD PROJEKT RED", List.of("RPG", "Mundo aberto", "Aventura"),
                    achievement("Contrato de bruxo", "Conclua seu primeiro contrato de monstro."),
                    achievement("Pelas estradas de Velen", "Descubra dez pontos de interesse.")),
            game("Portal 2", "Resolva desafios com uma ferramenta capaz de criar portais. "
                    + "Explore os laboratórios da Aperture Science em uma aventura de raciocínio e humor.",
                    "32.99", "2011-04-19", "Valve", List.of("Quebra-cabeça", "Aventura"),
                    achievement("Pensando com portais", "Resolva sua primeira câmara de testes."),
                    achievement("Ciência em movimento", "Complete cinco câmaras de testes.")),
            game("Celeste", "Ajude Madeline a escalar a montanha Celeste e a enfrentar seus próprios desafios. "
                    + "Uma aventura de plataforma com controles precisos, segredos e uma história acolhedora.",
                    "36.99", "2018-01-25", "Maddy Makes Games", List.of("Indie", "Plataforma", "Aventura"),
                    achievement("Um passo de cada vez", "Conclua o primeiro capítulo da escalada."),
                    achievement("Colecionadora de morangos", "Encontre vinte morangos na montanha.")));

    static final List<Play> PLAYS = List.of(
            new Play("player", "Cyberpunk 2077", 42, 5,
                    "Gostei das missões secundárias e da ambientação de Night City.", 2),
            new Play("player", "Hollow Knight", 18, 5,
                    "Explorar o mapa e descobrir novas habilidades tornou a jornada muito especial.", 1),
            new Play("player", "Stardew Valley", 36, 5,
                    "Um ótimo lugar para desacelerar e cuidar da fazenda depois das aulas.", 2),
            new Play("player", "Hades", 14, 4,
                    "Combates ágeis e boas combinações de bênçãos a cada tentativa.", 1),
            new Play("player", "Portal 2", 9, 5,
                    "Os quebra-cabeças evoluem muito bem e os diálogos são divertidos.", 2),
            new Play("player", "Celeste", 7, 4,
                    "A escalada exige prática, mas cada nova tela concluída compensa.", 1),
            new Play("community", "Red Dead Redemption 2", 61, 5,
                    "A história de Arthur e os pequenos encontros pelo caminho me prenderam.", 2),
            new Play("community", "ELDEN RING", 48, 5,
                    "O mundo convida à exploração e vencer os chefes é muito gratificante.", 2),
            new Play("community", "Baldur's Gate 3", 53, 5,
                    "As decisões e as conversas com os companheiros deram personalidade à campanha.", 2),
            new Play("community", "Forza Horizon 5", 22, 4,
                    "Gostei de alternar entre explorar o mapa e disputar as provas.", 1),
            new Play("community", "God of War", 27, 5,
                    "A relação entre Kratos e Atreus é o ponto alto desta aventura.", 2),
            new Play("community", "The Witcher 3: Wild Hunt", 76, 5,
                    "Os contratos contam boas histórias e as escolhas fazem diferença.", 2),
            new Play("community", "Hollow Knight", 31, 5,
                    "A trilha sonora e o desenho das áreas deixam a exploração marcante.", 2),
            new Play("community", "Portal 2", 12, 5,
                    "Uma ótima mistura de lógica, descoberta e humor.", 2));

    private static Game game(String title, String description, String price, String releasedAt,
            String developer, List<String> categories, Achievement first, Achievement second) {
        return new Game(title, description, new BigDecimal(price), LocalDate.parse(releasedAt),
                developer, categories, List.of(first, second));
    }

    private static Achievement achievement(String name, String description) {
        return new Achievement(name, description);
    }

    record Developer(String name, String country, String foundedAt) {
    }

    record Achievement(String name, String description) {
    }

    record Game(String title, String description, BigDecimal price, LocalDate releasedAt,
            String developer, List<String> categories, List<Achievement> achievements) {
    }

    record Play(String username, String game, int hours, int rating, String review, int unlocked) {
    }
}
