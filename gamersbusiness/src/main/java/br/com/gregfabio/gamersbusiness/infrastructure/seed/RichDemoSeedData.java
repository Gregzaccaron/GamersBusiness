package br.com.gregfabio.gamersbusiness.infrastructure.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Academic BRL prices, reviews and achievements, not a mirror of Steam's live data. */
final class RichDemoSeedData {
    private RichDemoSeedData() {
    }

    static final List<DemoSeedData.Developer> DEVELOPERS = List.of(
            developer("Respawn Entertainment", "Estados Unidos", "2010-04-12"),
            developer("Digital Extremes", "Canadá", "1993-01-01"),
            developer("Grinding Gear Games", "Nova Zelândia", "2006-01-01"),
            developer("Bungie", "Estados Unidos", "1991-05-07"),
            developer("PUBG Studios", "Coreia do Sul", "2009-06-01"),
            developer("Hello Games", "Reino Unido", "2008-02-01"),
            developer("Unknown Worlds Entertainment", "Estados Unidos", "2001-01-01"),
            developer("Re-Logic", "Estados Unidos", "2011-01-01"),
            developer("Iron Gate Studio", "Suécia", "2019-04-01"),
            developer("Coffee Stain Studios", "Suécia", "2010-04-01"),
            developer("Wube Software", "República Tcheca", "2014-09-01"),
            developer("Ludeon Studios", "Canadá", "2013-06-01"),
            developer("Firaxis Games", "Estados Unidos", "1996-05-01"),
            developer("Forgotten Empires", "Estados Unidos", "2012-01-01"),
            developer("Colossal Order", "Finlândia", "2009-06-01"),
            developer("SCS Software", "República Tcheca", "1997-01-01"),
            developer("Kunos Simulazioni", "Itália", "2005-01-01"),
            developer("id Software", "Estados Unidos", "1991-02-01"),
            developer("Capcom", "Japão", "1983-06-11"),
            developer("Motive Studio", "Canadá", "2015-07-01"),
            developer("Round8 Studio", "Coreia do Sul", "2019-01-01"),
            developer("Moon Studios", "Áustria", "2010-01-01"),
            developer("Studio MDHR", "Canadá", "2010-01-01"),
            developer("Motion Twin", "França", "2001-08-01"),
            developer("Mega Crit", "Estados Unidos", "2017-01-01"),
            developer("LocalThunk", "Canadá", "2022-01-01"),
            developer("ZA/UM", "Reino Unido", "2016-01-01"),
            developer("Mobius Digital", "Estados Unidos", "2013-01-01"),
            developer("Hazelight Studios", "Suécia", "2014-11-01"),
            developer("Ghost Town Games", "Reino Unido", "2015-01-01"),
            developer("poncle", "Reino Unido", "2021-01-01"));

    // Foundation dates without a publicly established day use the first day of the known month/year.
    static final List<Entry> GAMES = List.of(
            game("Counter-Strike 2", "Dispute rodadas entre equipes que precisam plantar ou desarmar uma bomba. "
                    + "Economia, posicionamento e comunicação importam tanto quanto a precisão dos disparos.",
                    "0.00", "2023-09-27", "Valve", List.of("FPS", "Competitivo", "Multijogador"),
                    "Primeiro desarme|Desarme uma bomba durante uma rodada.",
                    "Economia de equipe|Vença uma rodada após economizar recursos.",
                    "Mira disciplinada|Elimine dez adversários com disparos precisos.",
                    "Mapa dominado|Conclua partidas em cinco mapas diferentes.",
                    "As trocas de tiros são boas, mas a pressão das partidas competitivas me afastou.",
                    "Aprender a economia das rodadas foi interessante; ainda sofro com a exigência de mira.",
                    "Coordenar utilitários e retomar o bombsite com amigos tornou cada rodada emocionante."),
            game("Dota 2", "Escolha um herói e trabalhe com sua equipe para destruir a base adversária. "
                    + "Rotas, visão do mapa e combinações de habilidades criam partidas de estratégia em tempo real.",
                    "0.00", "2013-07-09", "Valve", List.of("MOBA", "Estratégia", "Competitivo"),
                    "Primeira torre|Ajude sua equipe a destruir uma torre.", "Visão do rio|Posicione dez sentinelas.",
                    "Sinergia de heróis|Participe de uma eliminação combinando habilidades.",
                    "Ancestral protegido|Vença uma partida defendendo sua base.",
                    "A quantidade de itens e regras das rotas me pareceu excessiva para começar.",
                    "Gostei das combinações de heróis, embora as partidas longas exijam bastante dedicação.",
                    "Controlar a visão e virar uma luta com uma habilidade bem usada é muito recompensador."),
            game("Team Fortress 2", "Participe de confrontos entre equipes com nove classes de funções distintas. "
                    + "Escolte cargas, capture pontos e combine estilos de jogo em cenários de humor cartunesco.",
                    "0.00", "2007-10-10", "Valve", List.of("FPS", "Cooperativo", "Multijogador"),
                    "Escolha sua classe|Conclua uma partida com uma classe.", "Carga em movimento|Ajude a avançar a carga.",
                    "Médico de plantão|Cure companheiros durante uma partida.", "Equipe versátil|Jogue com as nove classes.",
                    "A confusão visual das batalhas me impediu de aproveitar as diferenças entre classes.",
                    "As classes são divertidas, mas precisei de tempo para entender os objetivos dos mapas.",
                    "Alternar entre Medic e Engineer me deu ótimas maneiras de ajudar a equipe sem só mirar."),
            game("Apex Legends", "Forme um esquadrão de lendas com habilidades complementares em uma arena de sobrevivência. "
                    + "Movimente-se pelo terreno, encontre equipamentos e escolha quando entrar em combate.",
                    "0.00", "2019-02-04", "Respawn Entertainment", List.of("FPS", "Battle royale", "Competitivo"),
                    "Queda coordenada|Pouse com seu esquadrão na mesma região.", "Resgate de lenda|Reviva um aliado.",
                    "Anel seguro|Alcance a última zona de uma partida.", "Esquadrão campeão|Vença uma partida com sua equipe.",
                    "Passar tanto tempo procurando equipamento antes de cair no primeiro confronto me frustrou.",
                    "A movimentação é ótima, mas depender do ritmo do esquadrão nem sempre combina comigo.",
                    "Deslizar pelo mapa e combinar as habilidades do trio deixou as disputas muito dinâmicas."),
            game("Warframe", "Controle guerreiros biomecânicos em missões pelo Sistema de Origem. "
                    + "Colete materiais, fabrique equipamentos e experimente habilidades em combates cooperativos velozes.",
                    "0.00", "2013-03-25", "Digital Extremes", List.of("Ação", "Cooperativo", "RPG"),
                    "Despertar Tenno|Conclua sua primeira missão.", "Forja da órbita|Fabrique um equipamento na forja.",
                    "Parkour orbital|Atravesse uma missão usando saltos e deslizamentos.", "Esquadrão do vazio|Conclua dez missões cooperativas.",
                    "A repetição para obter materiais pesou mais que a diversão do combate para mim.",
                    "O parkour é excelente, mas entender a forja e os modificadores leva algum tempo.",
                    "Montar um Warframe e atravessar as fases com o esquadrão rende sessões muito divertidas."),
            game("Path of Exile", "Sobreviva em Wraeclast criando um exilado especializado em habilidades e equipamentos. "
                    + "Explore masmorras, combine gemas e planeje caminhos em uma extensa árvore de passivas.",
                    "0.00", "2013-10-23", "Grinding Gear Games", List.of("RPG", "Ação", "Hack and slash"),
                    "Exílio iniciado|Conclua a primeira missão em Wraeclast.", "Gemas conectadas|Combine uma habilidade e uma gema de suporte.",
                    "Caminho traçado|Distribua vinte pontos na árvore de passivas.", "Cartógrafo exilado|Complete seu primeiro mapa do atlas.",
                    "As muitas moedas e possibilidades de construção tornaram minha primeira campanha cansativa.",
                    "Experimentar gemas é interessante, mas senti falta de uma explicação mais gradual da progressão.",
                    "Planejar a árvore e ver a combinação de gemas funcionar nos mapas foi o ponto alto."),
            game("Destiny 2", "Torne-se um Guardião e explore destinos de ficção científica em defesa da humanidade. "
                    + "Combine armas e poderes em atividades cooperativas e confrontos entre jogadores.",
                    "0.00", "2017-10-24", "Bungie", List.of("FPS", "Cooperativo", "RPG"),
                    "Luz desperta|Conclua uma atividade como Guardião.", "Arsenal de luz|Equipe uma arma em cada espaço disponível.",
                    "Esquadrão de assalto|Conclua um assalto com aliados.", "Órbita explorada|Visite três destinos diferentes.",
                    "A divisão do conteúdo e a progressão dos equipamentos dificultaram acompanhar a aventura.",
                    "Os disparos são agradáveis, mas ainda achei confuso escolher as próximas atividades.",
                    "Os assaltos em trio misturam bons tiroteios e poderes que se complementam muito bem."),
            game("PUBG: BATTLEGROUNDS", "Salte em um campo de batalha e procure recursos enquanto a área segura diminui. "
                    + "A sobrevivência depende de observação, posicionamento e decisões cuidadosas a cada encontro.",
                    "0.00", "2017-12-21", "PUBG Studios", List.of("Battle royale", "FPS", "Competitivo"),
                    "Pouso calculado|Encontre equipamento após o primeiro salto.", "Abrigo na zona|Alcance três zonas seguras na mesma partida.",
                    "Últimos sobreviventes|Termine uma partida entre os dez primeiros.", "Jantar conquistado|Vença uma partida de sobrevivência.",
                    "Os intervalos longos sem ação e as eliminações repentinas não funcionaram para mim.",
                    "A tensão de atravessar campos abertos é boa, mas o ritmo das partidas oscila bastante.",
                    "Escolher a rota até a zona final com o grupo criou momentos de muita tensão e estratégia."),
            game("No Man's Sky", "Viaje por sistemas estelares, descubra planetas e registre formas de vida alienígenas. "
                    + "Melhore sua nave, construa bases e escolha seu próprio ritmo de exploração espacial.",
                    "119.90", "2016-08-12", "Hello Games", List.of("Exploração", "Sobrevivência", "Mundo aberto"),
                    "Novo horizonte|Pouse em um planeta desconhecido.", "Catálogo estelar|Registre dez espécies alienígenas.",
                    "Lar entre estrelas|Construa uma base habitável.", "Salto interestelar|Visite cinco sistemas estelares.",
                    "A coleta constante de recursos acabou interrompendo a exploração que eu queria fazer.",
                    "Descobrir planetas é relaxante, embora algumas tarefas de manutenção se repitam demais.",
                    "Construir uma base em um planeta descoberto por mim deu um sentido especial à viagem."),
            game("Subnautica", "Explore um oceano alienígena após a queda de sua nave em um planeta remoto. "
                    + "Administre oxigênio, construa equipamentos e investigue os mistérios escondidos nas profundezas.",
                    "79.90", "2018-01-23", "Unknown Worlds Entertainment", List.of("Sobrevivência", "Exploração", "Crafting"),
                    "Primeiro mergulho|Deixe a cápsula e explore o oceano.", "Oxigênio extra|Fabrique um tanque de oxigênio.",
                    "Base submersa|Construa seu primeiro habitat aquático.", "Além da luz|Explore uma região de águas profundas.",
                    "Controlar oxigênio e recursos me deixou mais ansioso do que interessado na exploração.",
                    "O oceano é fascinante, mas encontrar certos materiais exige paciência e orientação.",
                    "Descer cada vez mais fundo e voltar para minha base foi uma experiência inesquecível."),
            game("Terraria", "Escave, construa e enfrente criaturas em um mundo de blocos bidimensional. "
                    + "Descubra biomas, fabrique ferramentas e prepare seu abrigo para desafios cada vez maiores.",
                    "19.99", "2011-05-16", "Re-Logic", List.of("Sandbox", "Crafting", "Cooperativo"),
                    "Abrigo de madeira|Construa uma casa adequada para um morador.", "Veio descoberto|Extraia seu primeiro minério.",
                    "Olhar enfrentado|Derrote o Olho de Cthulhu.", "Biomas conectados|Visite cinco biomas do mundo.",
                    "A falta de direção inicial me deixou perdido entre receitas e túneis.",
                    "Construir e minerar é bom, mas precisei consultar receitas para avançar nos chefes.",
                    "Cada escavação revelou equipamento novo e construir a vila com amigos foi viciante."),
            game("Valheim", "Desperte em um mundo inspirado na mitologia nórdica e prove seu valor aos deuses. "
                    + "Construa abrigos, navegue entre biomas e prepare equipamentos para caçadas cooperativas.",
                    "37.99", "2021-02-02", "Iron Gate Studio", List.of("Sobrevivência", "Cooperativo", "Crafting"),
                    "Fogueira viking|Construa uma fogueira e um abrigo.", "Caçada de Eikthyr|Derrote o primeiro guardião.",
                    "Mar aberto|Navegue até uma nova ilha.", "Salão dos companheiros|Construa um salão com sua equipe.",
                    "Recuperar equipamentos depois das mortes tornou a coleta de recursos desgastante para mim.",
                    "A construção é agradável, mas as viagens para buscar minério podem ser demoradas.",
                    "Preparar comida e zarpar com amigos para enfrentar um chefe rendeu ótimas histórias."),
            game("Satisfactory", "Construa uma fábrica em um planeta alienígena cheio de recursos e paisagens a explorar. "
                    + "Organize esteiras, energia e linhas de produção para automatizar componentes cada vez mais complexos.",
                    "99.90", "2024-09-10", "Coffee Stain Studios", List.of("Automação", "Crafting", "Cooperativo"),
                    "Primeira esteira|Transporte recursos por uma esteira automática.", "Energia estável|Alimente uma linha de produção sem interrupções.",
                    "Elevador abastecido|Entregue componentes ao elevador espacial.", "Fábrica integrada|Automatize quatro componentes diferentes.",
                    "Refazer esteiras e calcular energia virou trabalho demais para o meu gosto.",
                    "Ver as máquinas funcionando é satisfatório, mas reorganizar a fábrica toma bastante tempo.",
                    "Equilibrar as linhas de produção e eliminar gargalos fez as horas passarem sem perceber."),
            game("Factorio", "Transforme recursos de um planeta hostil em uma rede industrial automatizada. "
                    + "Pesquise tecnologias, projete ferrovias e proteja sua produção enquanto prepara um lançamento espacial.",
                    "101.99", "2020-08-14", "Wube Software", List.of("Automação", "Estratégia", "Gestão"),
                    "Mineração automática|Instale uma mineradora alimentando uma fornalha.", "Ciência em série|Automatize a produção de pacotes científicos.",
                    "Trilhos conectados|Transporte recursos por trem.", "Rumo ao espaço|Lance um foguete de sua fábrica.",
                    "Administrar os gargalos junto com os ataques me trouxe mais cansaço do que diversão.",
                    "O ciclo de pesquisa é envolvente, mas desenhar ferrovias eficientes ainda é um desafio.",
                    "Conectar trens, circuitos e esteiras em uma fábrica estável foi extremamente recompensador."),
            game("RimWorld", "Administre uma colônia de sobreviventes em um planeta distante e imprevisível. "
                    + "Cuide de necessidades, relações e defesas enquanto eventos transformam o cotidiano em histórias emergentes.",
                    "79.99", "2018-10-17", "Ludeon Studios", List.of("Gestão", "Sobrevivência", "Estratégia"),
                    "Primeiro refúgio|Construa dormitórios para seus colonos.", "Colheita da colônia|Armazene alimentos cultivados na colônia.",
                    "Inverno preparado|Mantenha reservas durante uma estação fria.", "História compartilhada|Mantenha uma colônia por um ano.",
                    "Perder colonos por uma sequência de incidentes aleatórios foi frustrante demais para mim.",
                    "As histórias da colônia surpreendem, mas gerenciar necessidades exige atenção constante.",
                    "As relações entre colonos e os imprevistos fizeram minha base parecer uma história viva."),
            game("Sid Meier's Civilization VI", "Conduza uma civilização da antiguidade à era moderna em partidas por turnos. "
                    + "Planeje distritos, pesquise tecnologias e negocie com líderes para buscar diferentes formas de vitória.",
                    "79.90", "2016-10-21", "Firaxis Games", List.of("Estratégia", "Turnos", "Gestão"),
                    "Cidade fundada|Estabeleça sua primeira cidade.", "Distritos planejados|Construa três distritos especializados.",
                    "Mesa diplomática|Conclua um acordo com outra civilização.", "Legado duradouro|Alcance uma condição de vitória.",
                    "A duração das partidas e a quantidade de decisões por turno não me prenderam.",
                    "Planejar distritos é interessante, mas o fim da partida ficou mais lento do que eu esperava.",
                    "Combinar ciência, cultura e diplomacia me fez querer jogar sempre mais um turno."),
            game("Age of Empires II: Definitive Edition", "Comande civilizações históricas em campanhas e batalhas de estratégia em tempo real. "
                    + "Organize aldeões, avance pelas eras e combine unidades para conquistar território.",
                    "49.99", "2019-11-14", "Forgotten Empires", List.of("Estratégia", "RTS", "Multijogador"),
                    "Aldeia em expansão|Treine dez aldeões.", "Nova era|Avance para a Idade dos Castelos.",
                    "Muralhas erguidas|Proteja seu assentamento com fortificações.", "General de campanha|Conclua uma campanha histórica.",
                    "Controlar aldeões e batalhas ao mesmo tempo foi estressante e não combinou comigo.",
                    "As campanhas são boas, mas ainda me atrapalho para equilibrar economia e exército.",
                    "Preparar a economia e vencer uma batalha com a composição certa continua excelente."),
            game("Cities: Skylines", "Planeje uma cidade desde as primeiras ruas até bairros interligados por transporte público. "
                    + "Equilibre serviços, orçamento e tráfego para acompanhar o crescimento da população.",
                    "59.99", "2015-03-10", "Colossal Order", List.of("Gestão", "Simulação", "Construção"),
                    "Primeiro bairro|Crie uma zona residencial com serviços básicos.", "Água e energia|Conecte os moradores às redes essenciais.",
                    "Linha inaugurada|Implante uma linha de transporte público.", "Trânsito organizado|Conecte três bairros por rotas alternativas.",
                    "Os congestionamentos dominaram minha cidade e resolver o trânsito deixou de ser divertido.",
                    "Construir bairros é relaxante, mas equilibrar o orçamento e os serviços demanda cuidado.",
                    "Ver os moradores usando a rede de transporte que planejei foi a melhor parte da cidade."),
            game("Euro Truck Simulator 2", "Viaje por estradas europeias transportando cargas entre cidades. "
                    + "Administre contratos, melhore seu caminhão e desenvolva uma empresa de transporte no seu ritmo.",
                    "39.99", "2012-10-19", "SCS Software", List.of("Simulação", "Condução", "Gestão"),
                    "Primeira entrega|Conclua um frete dentro do prazo.", "Garagem própria|Compre seu primeiro caminhão.",
                    "Fronteiras cruzadas|Entregue cargas em três países.", "Frota em crescimento|Contrate um motorista para sua empresa.",
                    "As viagens longas pareceram repetitivas e senti falta de objetivos mais variados.",
                    "Dirigir pelas estradas é relaxante, mas estacionar reboques ainda me dá trabalho.",
                    "Planejar rotas e fazer entregas ouvindo música virou meu jeito favorito de desacelerar."),
            game("Assetto Corsa", "Dirija carros de diferentes categorias em circuitos com foco na simulação automobilística. "
                    + "Ajuste seu veículo, aprenda pontos de frenagem e busque consistência a cada volta.",
                    "37.99", "2014-12-19", "Kunos Simulazioni", List.of("Corrida", "Simulação", "Competitivo"),
                    "Volta limpa|Complete uma volta sem sair da pista.", "Ponto de frenagem|Melhore seu tempo em um circuito.",
                    "Garagem variada|Dirija veículos de três categorias.", "Bandeirada final|Conclua uma corrida de dez voltas.",
                    "A exigência da pilotagem e dos ajustes tornou o começo pouco acessível para mim.",
                    "A física é convincente, mas preciso praticar bastante para manter voltas consistentes.",
                    "Aprender cada ponto de frenagem e tirar décimos do cronômetro é recompensador demais."),
            game("DOOM Eternal", "Enfrente hordas demoníacas alternando armas e movimentação constante em arenas intensas. "
                    + "Gerencie munição, armadura e vida explorando as fraquezas de cada inimigo.",
                    "99.90", "2020-03-20", "id Software", List.of("FPS", "Ação", "Campanha"),
                    "Arsenal desperto|Conclua o primeiro confronto da campanha.", "Combustível de batalha|Recupere munição usando a motosserra.",
                    "Ponto fraco|Destrua os pontos fracos de cinco demônios.", "Caçador incansável|Conclua uma arena sem parar de se mover.",
                    "Trocar de recurso e arma sem descanso deixou o combate cansativo para o meu ritmo.",
                    "As arenas são empolgantes, mas conciliar plataforma e gerenciamento de munição exige adaptação.",
                    "A dança entre motosserra, lança-chamas e armas faz cada arena parecer um quebra-cabeça veloz."),
            game("Resident Evil 4", "Acompanhe Leon em uma missão de resgate em uma região dominada por ameaças misteriosas. "
                    + "Administre munição, explore vilarejos e enfrente confrontos que misturam ação e tensão.",
                    "169.90", "2023-03-24", "Capcom", List.of("Terror", "Ação", "Sobrevivência"),
                    "Sobrevivente do vilarejo|Supere o primeiro ataque no vilarejo.", "Maleta organizada|Aprimore o espaço de seu inventário.",
                    "Defesa oportuna|Apare um ataque com a faca.", "Resgate em curso|Conclua uma etapa da missão com Ashley.",
                    "A tensão constante e a escassez de munição me impediram de aproveitar a campanha.",
                    "O combate é bom, mas algumas sequências de escolta quebraram meu ritmo de exploração.",
                    "Organizar a maleta e alternar disparos com defesas de faca renovou a aventura de Leon."),
            game("Dead Space", "Investigue uma nave de mineração silenciosa ao lado do engenheiro Isaac Clarke. "
                    + "Repare sistemas, administre recursos e use ferramentas industriais contra criaturas aterrorizantes.",
                    "149.90", "2023-01-27", "Motive Studio", List.of("Terror", "Sobrevivência", "Ficção científica"),
                    "Sinal da Ishimura|Entre na nave e restabeleça uma comunicação.", "Ferramenta de engenheiro|Aprimore o cortador de plasma.",
                    "Gravidade suspensa|Atravesse uma área em gravidade zero.", "Sistemas restaurados|Repare três sistemas essenciais da nave.",
                    "Os corredores escuros e sustos frequentes acabaram me afastando da história.",
                    "A ambientação da nave é forte, embora o retorno a alguns corredores me cansasse.",
                    "Usar ferramentas de engenharia para sobreviver deixou a exploração da Ishimura muito tensa."),
            game("Sekiro: Shadows Die Twice", "Siga a jornada de um shinobi em busca de seu senhor no Japão da era Sengoku. "
                    + "Domine aparos, use ferramentas de prótese e explore rotas verticais em combates de precisão.",
                    "179.90", "2019-03-22", "FromSoftware", List.of("Soulslike", "Ação", "Aventura"),
                    "Lâmina desperta|Vença seu primeiro duelo como shinobi.", "Postura quebrada|Derrote um inimigo rompendo sua postura.",
                    "Ferramenta do lobo|Instale uma ferramenta na prótese.", "Ritmo de aço|Execute dez aparos em combates.",
                    "A exigência de acertar o tempo dos aparos me travou antes de aproveitar a aventura.",
                    "A mobilidade é excelente, mas alguns duelos pediram mais repetição do que eu gostaria.",
                    "Quando entendi o ritmo dos aparos, cada duelo virou uma troca de golpes fantástica."),
            game("DARK SOULS III", "Atravesse um reino em ruínas na busca pelos Senhores das Cinzas. "
                    + "Explore atalhos, escolha equipamentos e aprenda os padrões de inimigos em combates exigentes.",
                    "159.90", "2016-04-12", "FromSoftware", List.of("Soulslike", "RPG", "Ação"),
                    "Cinzas despertas|Alcance o Santuário do Elo de Fogo.", "Chama reforçada|Aprimore sua reserva de estus.",
                    "Atalho aberto|Abra uma passagem de retorno à fogueira.", "Senhor enfrentado|Derrote um Senhor das Cinzas.",
                    "Repetir os trajetos após as mortes tirou minha vontade de continuar explorando.",
                    "Descobrir atalhos é ótimo, mas o ritmo de alguns chefes exigiu muita tentativa.",
                    "A arquitetura conectada e a sensação de superar cada chefe tornaram a jornada marcante."),
            game("Lies of P", "Explore a cidade de Krat como uma marionete envolvida em uma história de mentiras e humanidade. "
                    + "Combine peças de armas e domine defesas precisas em confrontos inspirados no gênero soulslike.",
                    "179.90", "2023-09-19", "Round8 Studio", List.of("Soulslike", "RPG", "Ação"),
                    "Despertar em Krat|Chegue ao Hotel Krat.", "Arma reinventada|Combine uma lâmina e um cabo diferentes.",
                    "Guarda perfeita|Execute cinco defesas perfeitas.", "Fios da verdade|Tome uma decisão importante sobre uma mentira.",
                    "As janelas de defesa me pareceram rígidas demais e interromperam meu avanço.",
                    "Gostei de montar armas, mas alguns chefes pedem bastante insistência nos aparos.",
                    "A atmosfera de Krat e a liberdade de combinar cabos e lâminas me conquistaram."),
            game("Ori and the Will of the Wisps", "Viaje por uma floresta ameaçada ao lado de Ori em busca de novos laços. "
                    + "Combine movimentos ágeis, habilidades e exploração em cenários desenhados com grande cuidado visual.",
                    "59.90", "2020-03-11", "Moon Studios", List.of("Metroidvania", "Plataforma", "Aventura"),
                    "Luz na floresta|Descubra a primeira região de Niwen.", "Salto encadeado|Combine duas habilidades de movimento.",
                    "Sementes de esperança|Contribua para a recuperação da clareira.", "Mapa iluminado|Explore cinco regiões da floresta.",
                    "Algumas fugas exigiram precisão demais e acabaram ofuscando a beleza da exploração.",
                    "O movimento é agradável, mas certos trechos de plataforma me fizeram repetir bastante.",
                    "Encadear os movimentos de Ori pela floresta, com aquela trilha sonora, foi maravilhoso."),
            game("Cuphead", "Enfrente chefes e fases de ação em uma aventura inspirada em desenhos animados antigos. "
                    + "Aprenda padrões, equipe habilidades e compartilhe os desafios em cooperação local.",
                    "36.99", "2017-09-29", "Studio MDHR", List.of("Plataforma", "Cooperativo", "Ação"),
                    "Contrato inicial|Derrote seu primeiro chefe.", "Aparo cor-de-rosa|Execute cinco aparos em objetos rosados.",
                    "Dupla de xícaras|Conclua uma fase em cooperação.", "Ilha atravessada|Conclua os desafios de uma ilha.",
                    "O visual me atraiu, mas repetir chefes tantas vezes não foi divertido para mim.",
                    "A animação é incrível; o nível de dificuldade pede mais paciência do que eu esperava.",
                    "Aprender cada fase do chefe com um parceiro foi difícil e muito gratificante."),
            game("Dead Cells", "Explore um castelo mutável combinando combate ágil e progressão entre tentativas. "
                    + "Descubra armas, desbloqueie melhorias e escolha rotas diferentes a cada nova fuga.",
                    "47.49", "2018-08-07", "Motion Twin", List.of("Roguelike", "Metroidvania", "Ação"),
                    "Cela aberta|Saia dos aposentos iniciais.", "Projeto recuperado|Entregue seu primeiro projeto ao coletor.",
                    "Rota alternativa|Explore uma saída diferente de um bioma.", "Célula conquistada|Derrote o chefe final de uma tentativa.",
                    "Perder a construção de equipamentos a cada morte tornou o progresso frustrante.",
                    "As armas são variadas, mas repetir as primeiras áreas me cansou em sessões longas.",
                    "Testar combinações de armas e descobrir novas rotas deixa cada tentativa empolgante."),
            game("Slay the Spire", "Suba uma torre de encontros imprevisíveis construindo um baralho durante a jornada. "
                    + "Escolha cartas, relíquias e rotas para adaptar sua estratégia aos inimigos de cada andar.",
                    "37.99", "2019-01-23", "Mega Crit", List.of("Cartas", "Roguelike", "Estratégia"),
                    "Primeira carta|Adicione uma carta ao baralho durante uma subida.", "Relíquia descoberta|Encontre três relíquias em uma tentativa.",
                    "Guardião vencido|Derrote o chefe do primeiro ato.", "Torre escalada|Conclua os três atos de uma subida.",
                    "Depender das cartas oferecidas me frustrou quando a estratégia não encaixava.",
                    "Escolher rotas e cartas é interessante, mas ainda erro bastante na defesa contra chefes.",
                    "Montar um baralho enxuto e encontrar sinergias entre cartas e relíquias é viciante."),
            game("Balatro", "Monte combinações de pôquer em um jogo de cartas com progressão de roguelike. "
                    + "Adquira curingas, transforme seu baralho e multiplique pontos para superar apostas crescentes.",
                    "44.99", "2024-02-20", "LocalThunk", List.of("Cartas", "Roguelike", "Estratégia"),
                    "Primeira aposta|Supere o primeiro blind.", "Curinga na manga|Compre seu primeiro curinga.",
                    "Baralho transformado|Use uma carta de tarô para modificar o baralho.", "Aposta vencida|Conclua a oitava aposta de uma tentativa.",
                    "O peso da sorte nas compras me desanimou quando a pontuação não acompanhou as apostas.",
                    "As combinações de curingas são boas, mas nem sempre consigo ajustar o baralho a tempo.",
                    "Descobrir como multiplicar uma mão simples com curingas certos me prendeu por horas."),
            game("Disco Elysium", "Investigue um assassinato como um detetive marcado por lembranças fragmentadas. "
                    + "Converse com moradores, confronte suas próprias ideias e construa sua identidade por meio de escolhas.",
                    "75.99", "2019-10-15", "ZA/UM", List.of("RPG", "Narrativa", "Investigação"),
                    "Distintivo perdido|Encontre uma pista sobre sua identidade.", "Vozes interiores|Conclua um diálogo com uma habilidade interna.",
                    "Pista em Revachol|Reúna três pistas da investigação.", "Pensamento elaborado|Internalize um pensamento no gabinete mental.",
                    "O volume de leitura e o ritmo lento dos diálogos não combinaram com o que eu buscava.",
                    "A escrita é forte, mas acompanhar tantas ideias políticas e vozes exige concentração.",
                    "As conversas e os conflitos internos do detetive deram personalidade a cada escolha."),
            game("Outer Wilds", "Explore um pequeno sistema solar em uma jornada de descobertas e arqueologia espacial. "
                    + "Observe fenômenos, reúna pistas e entenda os ciclos que conectam planetas e antigas ruínas.",
                    "59.99", "2019-05-28", "Mobius Digital", List.of("Exploração", "Quebra-cabeça", "Aventura"),
                    "Primeiro lançamento|Decole da aldeia com sua nave.", "Diário de bordo|Registre uma descoberta no computador da nave.",
                    "Vestígios Nomai|Traduza inscrições em três locais.", "Órbitas conhecidas|Pouse em quatro corpos celestes diferentes.",
                    "O controle da nave e a pressão do ciclo me impediram de explorar no ritmo que queria.",
                    "Ligar as pistas é interessante, mas às vezes fiquei sem saber qual lugar investigar.",
                    "Cada descoberta mudou minha compreensão do sistema solar; vale jogar sem saber os segredos."),
            game("It Takes Two", "Guie uma dupla transformada em bonecos por uma aventura feita para dois jogadores. "
                    + "Resolva desafios de cooperação usando habilidades diferentes que mudam a cada cenário.",
                    "99.90", "2021-03-26", "Hazelight Studios", List.of("Cooperativo", "Plataforma", "Quebra-cabeça"),
                    "Dupla reunida|Conclua o primeiro desafio em parceria.", "Ferramentas combinadas|Resolva um obstáculo usando as habilidades dos dois jogadores.",
                    "Pausa para jogar|Encontre um minijogo opcional.", "Laços reconstruídos|Conclua um capítulo da aventura com seu parceiro.",
                    "Depender de um parceiro disponível e repetir saltos em dupla tornou a experiência difícil para mim.",
                    "As ideias cooperativas são criativas, mas alguns trechos de plataforma quebraram nosso ritmo.",
                    "A troca constante de habilidades fez eu e meu parceiro conversarmos e rirmos a cada fase."),
            game("Overcooked! 2", "Trabalhe com outros cozinheiros para preparar pedidos em cozinhas cheias de obstáculos. "
                    + "Divida tarefas, arremesse ingredientes e mantenha a comunicação para servir tudo antes do prazo.",
                    "47.99", "2018-08-07", "Ghost Town Games", List.of("Cooperativo", "Gestão", "Casual"),
                    "Pedido servido|Entregue seu primeiro prato correto.", "Ingrediente no ar|Arremesse um ingrediente para um colega.",
                    "Cozinha organizada|Conclua uma fase sem deixar pratos queimarem.", "Serviço três estrelas|Obtenha três estrelas em uma cozinha.",
                    "A correria dos pedidos virou estresse e a confusão da cozinha não me divertiu.",
                    "Cozinhar em grupo rende risadas, mas o tempo apertado de algumas fases pesa bastante.",
                    "Dividir bancadas e jogar ingredientes para os amigos criou uma bagunça cooperativa excelente."),
            game("Vampire Survivors", "Resista a ondas de criaturas enquanto reúne experiência e melhora seu arsenal automático. "
                    + "Combine armas e acessórios para criar estratégias capazes de atravessar multidões cada vez maiores.",
                    "13.99", "2022-10-20", "poncle", List.of("Roguelike", "Ação", "Casual"),
                    "Primeira noite|Sobreviva por cinco minutos.", "Arma evoluída|Evolua uma arma usando seu acessório compatível.",
                    "Chuva de experiência|Alcance o nível cinquenta em uma partida.", "Meia hora de resistência|Sobreviva por trinta minutos em um estágio.",
                    "O combate automático e a repetição das ondas me deram pouca sensação de participação.",
                    "Descobrir evoluções é divertido, mas as partidas ficam visualmente confusas no fim.",
                    "Combinar armas até atravessar multidões virou minha escolha para sessões rápidas e empolgantes."));

    static final List<Player> PLAYERS = List.of(
            player("ana_explora", 8, 9, 10, 11, 24, 31, 32, 4, 5, 6, 12, 14, 17, 21, 25, 27, 33, 35),
            player("bruno_rpg", 5, 4, 6, 23, 24, 1, 31, 8, 11, 14, 15, 21, 22, 27, 28, 29, 32, 33),
            player("carla_indie", 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 9, 10, 12, 13, 14, 17),
            player("diego_corridas", 18, 19, 0, 2, 3, 7, 8, 11, 12, 17, 20, 21, 23, 26, 27, 33, 34, 35),
            player("elisa_estrategia", 1, 5, 10, 12, 13, 14, 15, 16, 17, 18, 28, 29, 30, 31, 32, 34, 8, 11),
            player("felipe_coop", 2, 3, 4, 6, 7, 8, 10, 11, 12, 16, 20, 21, 24, 26, 27, 33, 34, 35),
            player("gabi_souls", 23, 24, 25, 20, 21, 22, 26, 27, 5, 4, 9, 11, 14, 28, 29, 31, 32, 35),
            player("henrique_builder", 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 4, 5, 28, 29, 30, 32, 34),
            player("iris_arcade", 2, 3, 19, 20, 24, 25, 26, 27, 28, 29, 30, 33, 34, 35, 0, 4, 10, 12),
            player("joao_tatico", 0, 1, 2, 3, 6, 7, 13, 14, 15, 16, 20, 21, 22, 23, 28, 29, 30, 31),
            player("luiza_fazenda", 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 24, 28, 29, 31, 32, 33, 34, 35),
            player("marcos_retro", 0, 2, 10, 13, 16, 18, 19, 20, 23, 24, 1, 26, 27, 28, 29, 30, 34, 35));

    private static DemoSeedData.Developer developer(String name, String country, String foundedAt) {
        return new DemoSeedData.Developer(name, country, foundedAt);
    }

    private static Entry game(String title, String description, String price, String releasedAt, String developer,
            List<String> categories, String first, String second, String third, String fourth,
            String negativeReview, String mixedReview, String positiveReview) {
        List<DemoSeedData.Achievement> achievements = List.of(first, second, third, fourth).stream()
                .map(value -> value.split("\\|", 2))
                .map(parts -> new DemoSeedData.Achievement(parts[0], parts[1])).toList();
        return new Entry(new DemoSeedData.Game(title, description, new BigDecimal(price), LocalDate.parse(releasedAt),
                developer, categories, achievements), negativeReview, mixedReview, positiveReview);
    }

    private static Player player(String username, Integer... games) {
        return new Player(username, List.of(games));
    }

    record Entry(DemoSeedData.Game game, String negativeReview, String mixedReview, String positiveReview) {
        String review(int rating) {
            return rating <= 2 ? negativeReview : rating == 3 ? mixedReview : positiveReview;
        }
    }

    record Player(String username, List<Integer> games) {
    }
}
