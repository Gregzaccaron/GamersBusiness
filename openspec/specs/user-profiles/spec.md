# Purpose

Define self-service profile management and administrator-controlled account listing and removal.

# Requirements

### Requirement: Usuário consulta e altera apenas o próprio perfil
A API MUST permitir que usuário autenticado consulte e altere seu nome de usuário, e-mail e senha em `GET /api/v1/me` e `PATCH /api/v1/me`, identificando a conta pelo token e nunca por ID informado pelo cliente. PATCH MUST exigir ao menos um campo mutável; campos omitidos permanecem inalterados, `null` é inválido. Alteração de senha MUST exigir `currentPassword` correto e `newPassword` válido enviados juntos; senha atual incorreta recebe `403` e não altera nenhum campo. Alterações MUST preservar unicidade de nome e e-mail. Respostas MUST NOT revelar senha ou hash.

#### Scenario: Consulta do próprio perfil
- **WHEN** usuário autenticado consulta `GET /api/v1/me`
- **THEN** a API retorna seus dados de perfil sem senha, hash ou papel editável pelo cliente

#### Scenario: Atualização válida de perfil
- **WHEN** usuário envia `PATCH /api/v1/me` com `username` e/ou `email` válidos, ou com ambas as senhas necessárias para a troca
- **THEN** a API persiste somente os campos informados e retorna `200` com perfil sem credenciais

#### Scenario: Troca de senha inválida
- **WHEN** usuário informa senha atual incorreta, envia apenas um dos campos de senha ou envia PATCH vazio
- **THEN** a API responde `403` para senha atual incorreta ou `400` para request incompleto sem modificar o perfil

#### Scenario: Atualização com nome ou e-mail já usado
- **WHEN** usuário altera nome ou e-mail para valor pertencente a outra conta
- **THEN** a API responde `409` e mantém o perfil anterior

### Requirement: ADMIN administra contas
A API MUST permitir que ADMIN liste contas e remova uma conta por ID; a lista e as respostas MUST omitir senha e hash. Contas referenciadas por registros de negócio MUST NOT ser apagadas em cascata e a tentativa de remoção conflitante MUST resultar em `409`.

#### Scenario: ADMIN lista usuários
- **WHEN** ADMIN solicita a lista de usuários
- **THEN** a API retorna perfis sem hashes de senha

#### Scenario: ADMIN remove conta sem referências impeditivas
- **WHEN** ADMIN remove uma conta existente que não possui registros dependentes
- **THEN** a API remove a conta e responde `204`

#### Scenario: Remoção de conta com histórico
- **WHEN** ADMIN tenta remover conta referenciada por biblioteca, avaliação ou conquista desbloqueada
- **THEN** a API preserva o histórico e responde `409`

#### Scenario: USER tenta administrar outra conta
- **WHEN** USER tenta listar ou remover contas
- **THEN** a API responde `403` sem expor dados de outras contas
