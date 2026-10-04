# Purpose

Define eligibility, persistence, uniqueness, and account-scoped listing of user achievement unlocks.

# Requirements

### Requirement: Usuário desbloqueia conquistas de jogos possuídos
A API MUST permitir que usuário autenticado desbloqueie uma conquista somente quando possuir ativamente o jogo ao qual ela pertence. O sistema MUST registrar data/hora e MUST impedir que a mesma conquista seja desbloqueada mais de uma vez pela mesma conta, inclusive após remoção e readquisição do jogo. Desbloqueios históricos permanecem mesmo depois de remover o jogo da biblioteca.

#### Scenario: Desbloqueio elegível
- **WHEN** usuário possui o jogo da conquista e ainda não desbloqueou a conquista
- **THEN** a API registra usuário, conquista e timestamp e responde `201`

#### Scenario: Usuário não possui o jogo
- **WHEN** usuário tenta desbloquear conquista de jogo ausente da sua biblioteca
- **THEN** a API responde `403` sem criar desbloqueio

#### Scenario: Desbloqueio repetido
- **WHEN** usuário tenta desbloquear conquista já registrada para sua conta
- **THEN** a API responde `409` e mantém a data original

#### Scenario: Conquista inexistente
- **WHEN** usuário solicita desbloqueio de conquista inexistente
- **THEN** a API responde `404` sem criar registro

#### Scenario: Desbloqueio após perda da aquisição
- **WHEN** usuário tenta desbloquear nova conquista após remover o jogo correspondente da biblioteca
- **THEN** a API responde `403`, mas conserva e lista conquistas desbloqueadas anteriormente

#### Scenario: Readquisição de jogo com conquista prévia
- **WHEN** usuário readquire jogo e tenta desbloquear uma conquista registrada antes de removê-lo
- **THEN** a API responde `409` e preserva a data do primeiro desbloqueio

### Requirement: Usuário lista conquistas desbloqueadas próprias
A API MUST listar somente conquistas da conta autenticada e MUST permitir filtro opcional por jogo.

#### Scenario: Listagem completa ou filtrada
- **WHEN** usuário consulta suas conquistas sem filtro ou informa um jogo
- **THEN** a API retorna somente desbloqueios da própria conta, aplicando o filtro por jogo quando fornecido

#### Scenario: Usuário tenta consultar dados de outra conta
- **WHEN** usuário tenta fornecer ID de outra conta para listar conquistas
- **THEN** a API ignora o ID ou rejeita a entrada e nunca retorna conquistas de terceiros
