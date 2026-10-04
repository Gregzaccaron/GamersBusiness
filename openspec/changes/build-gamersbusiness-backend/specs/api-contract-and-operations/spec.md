## ADDED Requirements

### Requirement: Requests e responses usam DTOs validados
Todos os endpoints MUST usar DTOs específicos para corpos de entrada e saída quando aplicável; entidades JPA MUST NOT ser expostas. Campos JSON seguem `camelCase` em inglês, datas usam ISO-8601 (`yyyy-MM-dd`) e timestamps ISO-8601 UTC. Requests com JSON malformado, campos desconhecidos, valores inválidos ou campos obrigatórios ausentes/nulos MUST receber `400` com indicação dos campos inválidos quando possível; `comment:null` em PATCH de avaliação é a única exceção intencional para limpar comentário opcional. IDs MUST ser inteiros positivos.

#### Scenario: Request inválido
- **WHEN** request viola campo obrigatório, formato de e-mail, limite de nota/preço, contém campo desconhecido ou JSON malformado
- **THEN** a API responde `400` sem persistir a operação e indica o campo rejeitado quando identificável

#### Scenario: Resposta de recurso
- **WHEN** API retorna usuário, jogo, aquisição, avaliação ou conquista
- **THEN** serializa apenas campos do DTO e nunca entidade, senha ou hash de senha

### Requirement: Erros seguem formato uniforme
Erros de API MUST retornar JSON uniforme com data/hora, status HTTP, mensagem e caminho da requisição; erros de validação MUST incluir detalhes por campo. `@RestControllerAdvice` MUST tratar erros de aplicação/validação e handlers de autenticação/autorização do Spring Security MUST aplicar o mesmo formato a respostas `401`/`403`.

#### Scenario: Exceção de domínio ou recurso inexistente
- **WHEN** operação resulta em erro de domínio ou recurso não encontrado
- **THEN** advice central responde no formato de erro uniforme com status apropriado

#### Scenario: Erro de validação
- **WHEN** Bean Validation rejeita um request
- **THEN** resposta uniforme inclui status `400` e mensagens associadas aos campos inválidos

#### Scenario: Erro de autenticação ou permissão
- **WHEN** requisição protegida falha por token ausente/inválido/conta excluída ou papel insuficiente
- **THEN** a API responde `401` ou `403`, respectivamente, no mesmo formato JSON de erro, sem revelar JWT, senha ou stack trace

### Requirement: API segue semântica REST e versão documentada
A API MUST usar prefixo `/api/v1` para operações de negócio e códigos `200`, `201`, `204`, `400`, `401`, `403`, `404` e `409` conforme resultado. Leituras e alterações com corpo MUST responder `200`; criações `201`; exclusões `204` sem corpo. Validação/JSON/ID inválidos MUST retornar `400`; IDs válidos de recursos/referências inexistentes MUST retornar `404`; falta de token `401`; falta de permissão `403`; duplicidade ou exclusão impedida por FK `409`. Recursos privados buscados fora do escopo da conta MUST retornar `404`, exceto avaliações alheias cuja edição/exclusão MUST retornar `403` conforme `game-reviews`.

#### Scenario: Criação e remoção bem-sucedidas
- **WHEN** cliente cria recurso válido ou remove recurso sem corpo de retorno
- **THEN** a API responde `201` na criação e `204` na remoção

#### Scenario: Referência inválida ou inexistente
- **WHEN** request usa identificador malformado/não positivo ou identificador válido de recurso ausente
- **THEN** a API responde `400` para ID inválido e `404` para recurso/referência inexistente

#### Scenario: Conflito de integridade
- **WHEN** operação viola unicidade ou restrição de chave estrangeira
- **THEN** a API responde `409` sem expor SQL, stack trace ou detalhes internos

### Requirement: Rotas e formatos HTTP são definidos por capacidade
Os métodos abaixo MUST ter os paths, autorização e DTOs declarados. GET de catálogo é permitido a qualquer usuário autenticado; POST/PUT/DELETE de catálogo exigem ADMIN. Um `id` em resposta é o identificador persistido. Criações retornam o DTO criado; `PUT` substitui campos editáveis, `PATCH` altera somente campos fornecidos. Identidade do usuário em recursos `/me` sempre vem do JWT.

| Recurso | Rotas e métodos | Corpo de entrada / resposta essencial |
| --- | --- | --- |
| Autenticação | `POST /api/v1/auth/register` (`201`); `POST /api/v1/auth/login` (`200`) | Cadastro `{username,email,password}` → perfil `{id,username,email,role,registeredAt}`; login `{email,password}` → `{accessToken,tokenType:\"Bearer\",expiresAt}`. |
| Perfil | `GET /api/v1/me` (`200`); `PATCH /api/v1/me` (`200`) | Perfil `{id,username,email,role,registeredAt}`. PATCH `{username?,email?,currentPassword?,newPassword?}`: ao menos uma alteração; campos omitidos permanecem; `null` é inválido; troca de senha exige senha atual correta e ambos os campos de senha. |
| Usuários | `GET /api/v1/users` (`200`); `DELETE /api/v1/users/{id}` (`204`) | Apenas ADMIN; lista paginada de perfis sem senha/hash; remoção com dependências retorna `409`. |
| Desenvolvedoras | `GET /api/v1/developers`, `GET /api/v1/developers/{id}` (`200`); `POST /api/v1/developers` (`201`); `PUT /api/v1/developers/{id}` (`200`); `DELETE /api/v1/developers/{id}` (`204`) | Escrita `{name,country,foundationDate}`; leitura `{id,name,country,foundationDate}`; nome duplicado ou exclusão vinculada retorna `409`. |
| Categorias | `GET /api/v1/categories`, `GET /api/v1/categories/{id}` (`200`); `POST /api/v1/categories` (`201`); `PUT /api/v1/categories/{id}` (`200`); `DELETE /api/v1/categories/{id}` (`204`) | Escrita `{name}`; leitura `{id,name}`; nome duplicado ou exclusão vinculada retorna `409`. |
| Jogos | `GET /api/v1/games`, `GET /api/v1/games/{id}` (`200`); `POST /api/v1/games` (`201`); `PUT /api/v1/games/{id}` (`200`); `DELETE /api/v1/games/{id}` (`204`) | Escrita `{title,description,price,releaseDate,developerId,categoryIds}`; leitura inclui `id` e estes campos; `categoryIds` exige pelo menos um ID, sem duplicatas. |
| Conquistas de catálogo | `GET /api/v1/achievements`, `GET /api/v1/achievements/{id}` (`200`); `POST /api/v1/achievements` (`201`); `PUT /api/v1/achievements/{id}` (`200`); `DELETE /api/v1/achievements/{id}` (`204`) | Escrita `{gameId,name,description}`; leitura inclui `id` e estes campos; remover conquista desbloqueada retorna `409`. |
| Biblioteca | `GET /api/v1/me/library` (`200`); `POST /api/v1/me/library` (`201`); `PATCH /api/v1/me/library/{id}` (`200`); `DELETE /api/v1/me/library/{id}` (`204`) | Aquisição `{gameId}`; alteração `{hoursPlayed}` inteiro >= 0; item `{id,gameId,acquiredAt,paidPrice,hoursPlayed}`. `id` identifica item da biblioteca da conta atual. |
| Avaliações | `GET /api/v1/games/{gameId}/reviews` (`200`); `POST /api/v1/games/{gameId}/reviews` (`201`); `PATCH /api/v1/reviews/{id}` (`200`); `DELETE /api/v1/reviews/{id}` (`204`) | Escrita `{rating,comment?}`; alteração `{rating?,comment?}` exige pelo menos um campo (`comment:null` limpa o comentário); resposta `{id,gameId,userId,rating,comment,reviewedAt}`; listagem inclui `items,averageRating,reviewCount` sobre todas as avaliações, além dos metadados de página. |
| Conquistas do usuário | `GET /api/v1/me/achievements` (`200`); `POST /api/v1/me/achievements` (`201`) | Desbloqueio `{achievementId}`; resposta `{achievementId,gameId,unlockedAt}`; GET suporta `gameId` opcional e só devolve conquistas da conta atual. |

Coleções de usuários, desenvolvedoras, categorias, jogos, conquistas de catálogo, biblioteca, avaliações e conquistas do usuário MUST ser paginadas por `page` (base zero, padrão 0) e `size` (padrão 20, intervalo 1–100), retornando `{items,page,size,totalElements,totalPages}`; valores inválidos recebem `400`. A listagem de jogos MUST aceitar `title` (substring sem diferenciar maiúsculas/minúsculas), `categoryId` e `developerId` (IDs exatos), combinados com AND; IDs positivos não encontrados em filtros retornam página vazia. A ordem padrão é `id` ascendente, inclusive com filtros; filtros não especificados não restringem resultados. A listagem de conquistas pessoais aceita apenas `gameId` positivo opcional. A média das avaliações MUST considerar todas as avaliações do jogo, não apenas a página retornada, e MUST ser `null` quando `reviewCount` for zero.

#### Scenario: Paginação e filtros combinados
- **WHEN** usuário autenticado solicita `GET /api/v1/games?page=0&size=20&title=game&categoryId=2&developerId=3`
- **THEN** a API retorna `200` com somente jogos que satisfazem todos os filtros, ordenados por ID e com totais de página coerentes

#### Scenario: IDs de referência não existentes
- **WHEN** ADMIN cadastra jogo com `developerId` ou `categoryIds` positivos mas inexistentes
- **THEN** a API retorna `404`, sem criar jogo nem seus vínculos

#### Scenario: Páginas e média de avaliações
- **WHEN** usuário consulta uma página de avaliações de um jogo que possui avaliações em outras páginas
- **THEN** `averageRating` e `reviewCount` consideram todas as avaliações do jogo

### Requirement: Persistência é versionada por Flyway
O schema PostgreSQL MUST ser criado e alterado por migrations Flyway versionadas. Hibernate MUST NOT criar nem atualizar tabelas automaticamente.

#### Scenario: Inicialização em banco vazio
- **WHEN** aplicação inicia contra PostgreSQL vazio
- **THEN** Flyway aplica migrations e Hibernate valida o mapeamento sem executar DDL automático

#### Scenario: Nova mudança de schema
- **WHEN** implementação adiciona ou altera estrutura relacional
- **THEN** a mudança é entregue em nova migration Flyway e migrations previamente aplicadas permanecem imutáveis

### Requirement: Docker Compose fornece ambiente executável
O projeto MUST incluir Dockerfile multi-stage da API, `.dockerignore` e `compose.yaml` usando a imagem oficial `postgres:17` e PostgreSQL 17 nos testes de integração, volume persistente, healthcheck e configuração por variáveis de ambiente. Compose MUST permitir iniciar banco e API sem instalar PostgreSQL localmente, ativar explicitamente o perfil `local` na API e aceitar opcionalmente as variáveis de provisionamento `BOOTSTRAP_ADMIN_ENABLED`, `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD`. Segredos locais MUST ficar fora do controle de versão; `.env.example` MUST conter somente placeholders/instruções, não credenciais reais.

#### Scenario: Subida de banco e API
- **WHEN** desenvolvedor configura `.env` com credenciais locais e executa `docker compose up --build`
- **THEN** Compose espera o healthcheck do PostgreSQL 17, inicia a API no perfil `local` conectada ao banco e mantém dados do PostgreSQL em volume nomeado

#### Scenario: Testes no mesmo major do banco local
- **WHEN** testes de integração iniciam um PostgreSQL pelo Testcontainers
- **THEN** o contêiner executa a imagem `postgres:17`, aplica migrations Flyway e verifica o mapeamento JPA

#### Scenario: Reinício preserva dados
- **WHEN** serviços são parados e reiniciados sem remover volume
- **THEN** dados já persistidos permanecem disponíveis no PostgreSQL

#### Scenario: Subida isolada do banco
- **WHEN** desenvolvedor solicita somente o serviço PostgreSQL pelo Compose
- **THEN** o banco fica disponível para execução da API via Maven no host

### Requirement: Swagger e instruções documentam uso
A API MUST publicar documentação Swagger/OpenAPI com operações, DTOs, respostas e autenticação Bearer JWT. Somente no perfil local, `GET /swagger-ui.html`, `GET /swagger-ui/**` e `GET /v3/api-docs/**` MUST ser acessíveis sem token para permitir abrir a interface no navegador; esses caminhos são de documentação, fora de `/api/v1/**`. Nos demais perfis, a documentação MUST estar desabilitada e os caminhos MUST NOT ser expostos. Essa exceção local não se estende a nenhum endpoint de negócio, inclusive escritas. README MUST explicar a exceção local e descrever requisitos, configuração de ambiente, inicialização por Docker/Maven, migrations e fluxo de cadastro/login/uso.

#### Scenario: Consulta da documentação local
- **WHEN** desenvolvedor no perfil local abre Swagger UI ou documento OpenAPI sem token
- **THEN** encontra operações da API, contratos request/response e esquema de autenticação Bearer JWT

#### Scenario: Executar chamada protegida no Swagger
- **WHEN** desenvolvedor abre o Swagger UI sem token e tenta chamar um recurso de negócio sob `/api/v1/**`
- **THEN** a API responde `401` até ser fornecido token Bearer válido na função Authorize

#### Scenario: Documentação fora do ambiente local
- **WHEN** cliente tenta abrir `/swagger-ui/**` ou `/v3/api-docs/**` em perfil diferente de local
- **THEN** a documentação não é servida

#### Scenario: Inicialização conforme README
- **WHEN** desenvolvedor segue instruções documentadas com variáveis válidas
- **THEN** consegue iniciar PostgreSQL, iniciar a API e executar um fluxo de autenticação e consulta
