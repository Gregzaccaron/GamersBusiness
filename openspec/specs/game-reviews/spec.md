# Purpose

Define creation, ownership-aware maintenance, listing, and aggregation of game reviews.

# Requirements

### Requirement: Avaliar jogo adquirido
A API MUST permitir que usuário autenticado crie no máximo uma avaliação por jogo que possua em sua biblioteca. Nota MUST ser inteiro de 1 a 5; comentário é opcional. A identidade do avaliador vem do token. Duplicidade MUST resultar em `409`.

#### Scenario: Avaliação válida de jogo possuído
- **WHEN** usuário cria avaliação de jogo em sua biblioteca com nota entre 1 e 5
- **THEN** a API registra avaliador, jogo, nota, comentário e data/hora e responde `201`

#### Scenario: Avaliar jogo não adquirido
- **WHEN** usuário tenta avaliar jogo que não possui
- **THEN** a API responde `403` e não cria avaliação

#### Scenario: Nota fora do intervalo ou avaliação duplicada
- **WHEN** nota é menor que 1 ou maior que 5, ou usuário já avaliou o jogo
- **THEN** a API responde `400` para nota inválida ou `409` para duplicidade sem alterar avaliação existente

### Requirement: Usuário altera somente a própria avaliação de jogo ainda possuído
A API MUST permitir que usuário edite avaliação própria apenas se ainda possuir o jogo, e exclua avaliação própria mesmo após remover o jogo da biblioteca. Avaliações históricas MUST continuar disponíveis para consulta e cálculo da média após a remoção da aquisição. PATCH MUST exigir ao menos `rating` ou `comment`; `comment:null` remove o comentário, e nota informada MUST ficar entre 1 e 5.

#### Scenario: Edição ou exclusão da própria avaliação com aquisição ativa
- **WHEN** usuário altera avaliação própria de jogo em sua biblioteca ou a remove
- **THEN** a API responde `200` após editar ou `204` após excluir

#### Scenario: Edição após remoção da biblioteca
- **WHEN** usuário tenta alterar avaliação própria de jogo que não está mais na sua biblioteca
- **THEN** a API responde `403` e mantém avaliação e média inalteradas

#### Scenario: Exclusão de avaliação histórica própria
- **WHEN** usuário exclui sua avaliação depois de remover o jogo da biblioteca
- **THEN** a API remove a avaliação e responde `204`; a média é recalculada

#### Scenario: Alteração de avaliação alheia
- **WHEN** usuário tenta editar ou excluir avaliação de outro usuário
- **THEN** a API responde `403` e mantém a avaliação intacta

### Requirement: Usuários consultam avaliações e média
Qualquer usuário autenticado MUST poder consultar avaliações de um jogo e sua média. A média MUST ser calculada sobre as avaliações persistidas e, quando não houver avaliações, MUST ser `null` ou indicar ausência, nunca uma nota artificial.

#### Scenario: Consulta de avaliações com média
- **WHEN** usuário autenticado consulta avaliações de jogo existente
- **THEN** a resposta contém avaliações e média aritmética das notas persistidas

#### Scenario: Jogo sem avaliações
- **WHEN** usuário consulta avaliação agregada de jogo sem avaliações
- **THEN** a resposta indica zero avaliações e média ausente (`null`)

#### Scenario: Consulta de jogo inexistente
- **WHEN** usuário consulta avaliações de jogo inexistente
- **THEN** a API responde `404`
