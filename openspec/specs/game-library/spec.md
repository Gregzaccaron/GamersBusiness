# Purpose

Define acquisition, ownership, play-time, removal, and reacquisition behavior for each user's game library.

# Requirements

### Requirement: Usuário mantém no máximo uma aquisição ativa por jogo
A API MUST permitir que usuário autenticado adicione jogo existente à própria biblioteca. Deve registrar data/hora da aquisição e snapshot do preço atual do catálogo; o cliente MUST NOT escolher usuário ou preço pago. Uma mesma conta MUST NOT manter duas aquisições ativas do mesmo jogo; uma readquisição após a remoção é permitida e não restaura o registro antigo.

#### Scenario: Aquisição válida
- **WHEN** usuário adquire jogo existente que ainda não consta na sua biblioteca
- **THEN** a API cria registro de biblioteca com usuário autenticado, timestamp e preço atual do jogo e responde `201`

#### Scenario: Aquisição repetida
- **WHEN** usuário tenta adquirir jogo já presente em sua biblioteca
- **THEN** a API responde `409` e preserva o registro de aquisição existente

#### Scenario: Aquisição de jogo inexistente
- **WHEN** usuário tenta adquirir ID de jogo inexistente
- **THEN** a API responde `404` e não cria registro

### Requirement: Usuário gerencia sua biblioteca
A API MUST listar somente a biblioteca do usuário autenticado, permitir atualização de horas jogadas para inteiro não negativo e permitir remover uma aquisição própria. A API MUST identificar o proprietário pelo token, não por ID de usuário enviado no request. Remover da biblioteca MUST apagar somente a aquisição atual e MUST preservar avaliações e conquistas já registradas como histórico; sem aquisição ativa não é permitido avaliar, editar avaliação ou desbloquear nova conquista. Uma futura aquisição do mesmo jogo cria novo registro com o preço vigente, mas não apaga avaliação nem desbloqueios anteriores.

#### Scenario: Listagem da biblioteca própria
- **WHEN** usuário consulta `GET /api/v1/me/library`
- **THEN** a API retorna somente jogos e dados de aquisição pertencentes à conta autenticada

#### Scenario: Atualização de horas válida
- **WHEN** usuário atualiza horas jogadas de item próprio para inteiro não negativo
- **THEN** a API persiste o total atualizado

#### Scenario: Horas inválidas ou item alheio
- **WHEN** usuário informa horas negativas ou tenta alterar/remover item de outra conta
- **THEN** a API rejeita a entrada inválida com `400` ou responde `404` para item não pertencente à biblioteca própria, sem modificar dados

#### Scenario: Remoção da própria aquisição
- **WHEN** usuário remove item existente de sua biblioteca
- **THEN** a API remove somente o vínculo de biblioteca e responde `204`

#### Scenario: Remoção após avaliação e desbloqueio
- **WHEN** usuário remove da biblioteca jogo já avaliado ou com conquistas desbloqueadas
- **THEN** a aquisição é excluída com `204`, mas avaliação, média do jogo e desbloqueios históricos permanecem consultáveis

#### Scenario: Nova aquisição após remoção
- **WHEN** usuário readquire jogo previamente removido da biblioteca
- **THEN** cria nova aquisição com preço e data atuais; avaliação e desbloqueios anteriores continuam únicos, sem reset
