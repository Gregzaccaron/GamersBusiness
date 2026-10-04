# Purpose

Define administration and authenticated discovery of developers, categories, games, and catalog achievements.

# Requirements

### Requirement: ADMIN gerencia desenvolvedoras
A API MUST oferecer criação, listagem, consulta, edição e remoção de desenvolvedoras com nome único, país e data de fundação. Uma desenvolvedora com jogos vinculados MUST NOT ser removida.

#### Scenario: CRUD de desenvolvedora por ADMIN
- **WHEN** ADMIN cria, lista, consulta ou edita uma desenvolvedora com dados válidos
- **THEN** a API persiste e retorna a desenvolvedora sem expor entidade JPA

#### Scenario: Nome de desenvolvedora duplicado
- **WHEN** ADMIN tenta criar ou renomear desenvolvedora para nome existente
- **THEN** a API responde `409` sem duplicar o registro

#### Scenario: Desenvolvedora possui jogos
- **WHEN** ADMIN tenta remover desenvolvedora que ainda possui jogos vinculados
- **THEN** a API mantém desenvolvedora e jogos e responde `409`

### Requirement: ADMIN gerencia categorias
A API MUST oferecer CRUD de categorias e MUST manter o nome da categoria único.

#### Scenario: CRUD de categoria por ADMIN
- **WHEN** ADMIN cria, lista, consulta ou edita categoria com nome válido
- **THEN** a API retorna o resultado persistido

#### Scenario: Categoria duplicada ou ainda usada
- **WHEN** ADMIN cria ou renomeia categoria com nome existente, ou tenta remover categoria associada a jogos
- **THEN** a API responde `409` e mantém os vínculos existentes

### Requirement: ADMIN gerencia jogos e suas categorias
A API MUST oferecer CRUD administrativo de jogos; cada jogo MUST referenciar uma desenvolvedora existente e ao menos uma categoria existente e distinta. Preço MUST ser decimal não negativo e o jogo MUST manter título, descrição, data de lançamento e desenvolvedora conforme o diagrama. Dados ausentes, preço negativo ou IDs de categoria repetidos recebem `400`; referências positivas inexistentes recebem `404`. Um jogo com biblioteca, avaliação ou qualquer conquista vinculada MUST NOT ser excluído.

#### Scenario: ADMIN cadastra jogo com desenvolvedora e categorias
- **WHEN** ADMIN cria jogo com dados válidos, uma desenvolvedora existente e uma ou mais categorias distintas existentes
- **THEN** a API persiste jogo e vínculos de categoria e responde `201`

#### Scenario: Jogo sem categorias ou com dados inválidos
- **WHEN** ADMIN tenta criar ou alterar jogo sem categoria, com categorias repetidas ou com preço negativo
- **THEN** a API responde `400` sem modificar o jogo

#### Scenario: Jogo com referência inexistente
- **WHEN** ADMIN tenta criar ou alterar jogo com desenvolvedora ou categoria de ID válido e inexistente
- **THEN** a API responde `404` sem persistir relações inválidas

#### Scenario: ADMIN remove jogo com dependências vinculadas
- **WHEN** ADMIN tenta remover jogo que consta em biblioteca, avaliações ou tem qualquer conquista vinculada
- **THEN** a API preserva o jogo e suas dependências e responde `409`

### Requirement: Usuário autenticado consulta catálogo paginado
Qualquer usuário autenticado MUST poder listar e consultar jogos. A listagem MUST suportar paginação e filtros combináveis por título, categoria e desenvolvedora, sem permitir alteração do catálogo por papel USER.

#### Scenario: Pesquisa paginada com filtros
- **WHEN** usuário autenticado lista jogos com paginação e um ou mais filtros válidos
- **THEN** a API retorna somente resultados correspondentes, além dos metadados da página

#### Scenario: Consulta sem filtro
- **WHEN** usuário autenticado lista jogos sem filtros
- **THEN** a API retorna catálogo paginado

### Requirement: ADMIN gerencia conquistas do catálogo
A API MUST oferecer CRUD administrativo de conquistas e cada conquista MUST pertencer a um jogo existente, mantendo nome e descrição conforme o diagrama. Conquista já desbloqueada MUST NOT ser excluída enquanto houver desbloqueios registrados.

#### Scenario: ADMIN cadastra conquista vinculada a jogo
- **WHEN** ADMIN cria conquista com nome, descrição e jogo existente
- **THEN** a API persiste conquista associada ao jogo e responde `201`

#### Scenario: Conquista referencia jogo inexistente
- **WHEN** ADMIN tenta criar ou alterar conquista com ID de jogo positivo e inexistente
- **THEN** a API responde `404` sem criar vínculo órfão

#### Scenario: Conquista com desbloqueios
- **WHEN** ADMIN tenta remover conquista com desbloqueios registrados
- **THEN** a API responde `409` e preserva os desbloqueios

#### Scenario: USER tenta alterar catálogo
- **WHEN** USER chama operação de escrita de desenvolvedora, categoria, jogo ou conquista
- **THEN** a API responde `403` sem modificar catálogo
