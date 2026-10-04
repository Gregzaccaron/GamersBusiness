## Why

O projeto Spring atual contém apenas o inicializador e não implementa os requisitos acadêmicos nem o modelo relacional do GamesBusiness. É necessário entregar uma API REST executável, segura e documentada que cubra autenticação, catálogo, biblioteca, avaliações e conquistas, preservando o diagrama ER e as regras do PDF.

## What Changes

- Evoluir o projeto inicial para uma API Spring Boot 3, Java 17 ou superior, PostgreSQL 17 em Docker e migrations Flyway; desativar `ddl-auto`.
- Organizar o código em Clean Architecture, com domínio, casos de uso, portas e adaptadores, mantendo controllers REST, DTOs separados de entidades e mapeamento explícito.
- Implementar cadastro/login com JWT stateless, BCrypt, autorização USER/ADMIN, bootstrap ADMIN opcional e restrito ao perfil local, e configuração Spring Security por `SecurityFilterChain` com CSRF desativado.
- Implementar perfil e administração de usuários; CRUD administrativo de desenvolvedoras, categorias, jogos e conquistas; consulta paginada e filtrável do catálogo.
- Implementar aquisição e gerenciamento da biblioteca, avaliações com nota média e desbloqueio/listagem de conquistas, com propriedade e unicidade aplicadas no servidor e no banco e histórico preservado após remoção da aquisição.
- Padronizar contratos de DTO, rotas, validação, respostas HTTP, erros JSON centralizados e Swagger/OpenAPI acessível somente no perfil local, além de documentação de execução/configuração.
- Entregar migrations, configuração do PostgreSQL em Docker Compose, Dockerfile multi-stage para a API, testes das regras e dos fluxos de segurança, além de instruções e configuração segura para demonstração em desenvolvimento.

## Capabilities

### New Capabilities
- `authentication-and-authorization`: cadastro, login JWT, papéis USER/ADMIN, rotas públicas e protegidas e respostas 401/403.
- `user-profiles`: consulta/edição do próprio perfil e operações administrativas de listagem e remoção de usuários.
- `game-catalog`: CRUD administrativo de desenvolvedoras, categorias, jogos e conquistas, com consulta autenticada paginada e filtros.
- `game-library`: aquisição única por usuário, registro do preço/data da compra, consulta, atualização de horas jogadas e remoção.
- `game-reviews`: avaliações únicas por usuário/jogo, nota de 1 a 5, edição/exclusão próprias, consulta e média por jogo.
- `user-achievements`: desbloqueio único de conquistas de jogos possuídos e listagem filtrável por jogo.
- `api-contract-and-operations`: DTOs, validação, formato de erros, semântica REST, documentação OpenAPI e execução local.

### Modified Capabilities

Nenhuma. `openspec/specs/` não contém capacidades existentes.

## Impact

- Código atual em `gamersbusiness/`: `pom.xml`, `src/main/java`, `src/main/resources`, `src/test` e `HELP.md`/README.
- Novas dependências/configurações: Spring Data JPA, PostgreSQL, Flyway, Bean Validation, biblioteca JWT e springdoc OpenAPI, além das dependências Spring Web, Security e Lombok já presentes.
- Novas tabelas conforme o diagrama: `desenvolvedora`, `jogo`, `categoria`, `jogo_categoria`, `usuario`, `conquista`, `biblioteca`, `avaliacao` e `usuario_conquista`, com FKs, restrições de unicidade e índices.
- API HTTP versionada, documentação Swagger, migrations SQL, `Dockerfile`, `.dockerignore` e `compose.yaml` para subir PostgreSQL persistente e executar a API contra esse banco; nenhuma integração externa ou alteração de sistema remoto está prevista.
