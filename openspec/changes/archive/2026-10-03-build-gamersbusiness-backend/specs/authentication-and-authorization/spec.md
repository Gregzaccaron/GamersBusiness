## ADDED Requirements

### Requirement: Cadastro público de usuário
A API MUST aceitar cadastro público com nome de usuário, e-mail e senha, armazenar apenas o hash BCrypt e criar a conta com papel `USER`. Nome de usuário e e-mail MUST ser únicos. Respostas MUST NOT incluir senha nem hash.

#### Scenario: Cadastro válido
- **WHEN** uma pessoa envia dados válidos e não utilizados para `POST /api/v1/auth/register`
- **THEN** o sistema cria conta `USER`, armazena hash BCrypt e responde `201` sem incluir senha ou hash

#### Scenario: E-mail ou nome já cadastrado
- **WHEN** o cadastro repete um nome de usuário ou e-mail existente
- **THEN** o sistema rejeita a operação com `409` e não cria outra conta

### Requirement: Login emite JWT
A API MUST autenticar por e-mail e senha em `POST /api/v1/auth/login` e emitir JWT assinado com subject igual ao ID imutável da conta (não e-mail ou nome de usuário), papel informativo e expiração definida de duas horas. Credenciais inválidas MUST receber `401` sem revelar qual campo falhou.

#### Scenario: Login válido
- **WHEN** um usuário informa e-mail e senha correspondentes à conta
- **THEN** a API responde `200` com token Bearer e instante de expiração

#### Scenario: Credenciais inválidas
- **WHEN** e-mail ou senha não corresponde a uma conta
- **THEN** a API responde `401` com mensagem genérica e não emite token

### Requirement: Recursos de negócio protegidos exigem token e conta ativa
Toda operação de negócio sob `/api/v1/**`, exceto `POST /api/v1/auth/register` e `POST /api/v1/auth/login`, MUST exigir JWT assinado válido e não expirado cujo subject identifique conta ainda existente. A API MUST operar sem sessão de servidor; a cada requisição autenticada MUST consultar a conta para obter seu papel atual em vez de confiar apenas no papel embutido no JWT. Documentação de desenvolvimento fora de `/api/v1/**` segue exceção própria definida em `api-contract-and-operations`.

#### Scenario: Requisição sem autenticação
- **WHEN** cliente chama um recurso de negócio protegido sem token
- **THEN** a API responde `401` e não executa a operação

#### Scenario: Token inválido ou expirado
- **WHEN** cliente envia bearer token cuja assinatura é inválida ou cuja expiração já ocorreu
- **THEN** a API responde `401` e não executa a operação

#### Scenario: Token de conta excluída
- **WHEN** cliente apresenta token ainda não expirado cujo subject corresponde a uma conta removida
- **THEN** a API responde `401` e não executa a operação

#### Scenario: Papel alterado após emissão
- **WHEN** conta com JWT vigente deixa de ter permissão ADMIN
- **THEN** operações administrativas recebem `403` com base no papel atual da conta

#### Scenario: Token válido de conta existente
- **WHEN** cliente chama recurso protegido com token vigente de conta existente
- **THEN** a API autentica a identidade e aplica as permissões atuais antes da operação

### Requirement: Papel ADMIN controla operações administrativas
Operações de administração de usuários e catálogo MUST estar disponíveis somente ao papel `ADMIN`; contas recém-cadastradas MUST receber `USER` e não podem selecionar seu próprio papel. Falha de papel MUST responder `403`.

#### Scenario: USER tenta ação administrativa
- **WHEN** usuário autenticado com papel `USER` chama operação administrativa
- **THEN** a API responde `403` sem alterar dados

#### Scenario: ADMIN executa ação administrativa
- **WHEN** usuário autenticado com papel `ADMIN` chama operação administrativa
- **THEN** a API permite a operação sujeita às regras de validação e negócio

#### Scenario: Cadastro tenta definir papel privilegiado
- **WHEN** request de cadastro contém campo `role` ou tenta se declarar `ADMIN`
- **THEN** a API responde `400`, não cria a conta e nunca promove usuário cadastrado publicamente

### Requirement: Primeiro ADMIN é provisionado apenas no ambiente local
No perfil local, provisionamento explícito por `BOOTSTRAP_ADMIN_ENABLED=true` e `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` MUST criar um ADMIN antes de aceitar requisições, usando BCrypt; MUST ser desabilitado por padrão. Fora do perfil local, ativar o bootstrap MUST fazer a aplicação falhar ao iniciar. O provisionamento MUST ser idempotente: se a mesma conta ADMIN já existir, não altera sua senha nem seu papel; se os dados colidirem com outra conta ou faltarem credenciais válidas, a inicialização MUST falhar sem promover usuários existentes. Senhas MUST NOT ser versionadas, registradas em logs ou devolvidas por respostas.

#### Scenario: Primeiro início local configurado
- **WHEN** aplicação local inicia com provisionamento ativado, credenciais válidas e nenhuma conta com nome/e-mail informados
- **THEN** cria exatamente um ADMIN com senha BCrypt que pode autenticar por login normal

#### Scenario: Reinício com ADMIN existente
- **WHEN** aplicação local reinicia com provisionamento ativado e a mesma conta ADMIN já existe
- **THEN** não cria outra conta nem substitui a senha armazenada

#### Scenario: Configuração ausente ou colisão de usuário
- **WHEN** provisionamento ativado não informa credenciais válidas ou nome/e-mail pertence a outra conta
- **THEN** a inicialização falha sem promover a conta existente nem expor a senha

#### Scenario: Provisionamento desligado
- **WHEN** `BOOTSTRAP_ADMIN_ENABLED` não está ativado
- **THEN** nenhuma conta administrativa é criada automaticamente

#### Scenario: Bootstrap ativado fora do perfil local
- **WHEN** a aplicação inicia fora do perfil local com `BOOTSTRAP_ADMIN_ENABLED=true`
- **THEN** a inicialização falha sem criar/promover uma conta administrativa
