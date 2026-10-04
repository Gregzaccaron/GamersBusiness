## 1. Base do projeto e persistência

- [x] 1.1 Ajustar `pom.xml` para Spring Boot 3.x e Java 17 ou superior e adicionar JPA, PostgreSQL, Flyway, Validation, springdoc, OAuth2 Resource Server/JWT e dependências de teste necessárias.
- [x] 1.2 Criar os pacotes Clean Architecture `domain`, `application`, `infrastructure` e `presentation` sob o package base existente, com configuração externa por variáveis de ambiente.
- [x] 1.3 Configurar PostgreSQL/Flyway e Hibernate sem geração automática de schema (`ddl-auto=validate`), incluindo configuração de data/hora e segredos externos.
- [x] 1.4 Criar migration inicial para as nove tabelas do diagrama, FKs sem cascata sobre histórico, restrições de unicidade para aquisições ativas/avaliações/desbloqueios, verificações de valores e índices; validar banco vazio via Flyway.
- [x] 1.5 Implementar modelos de domínio, portas de repositório, exceções de domínio e casos de uso sem dependência de Spring Data/JPA.
- [x] 1.6 Implementar entidades JPA, repositories/adapters e mapeamentos explícitos entre domínio e persistência, mantendo entidades dentro da infraestrutura.

## 2. Contrato HTTP e configuração transversal

- [x] 2.1 Implementar DTOs request/response conforme a tabela de rotas de `api-contract-and-operations` (JSON camelCase, datas ISO, perfil sem senha), Bean Validation, PATCH com campos opcionais e rejeição de campos JSON desconhecidos.
- [x] 2.2 Implementar `@RestControllerAdvice` e handlers Spring Security com erro uniforme (timestamp, status, mensagem, path e erros por campo), distinguindo 400/401/403/404/409 e sem expor exceções internas.
- [x] 2.3 Implementar verbos/rotas sob `/api/v1` definidos na spec, lista paginada padrão `page=0,size=20` (máximo 100), filtros AND de jogos, ordenação estável por ID, respostas 200/201/204 e OpenAPI com Bearer JWT.

## 3. Identidade, autenticação e perfil

- [x] 3.1 Implementar cadastro e autenticação por e-mail, unicidade de nome/e-mail, BCrypt e papel `USER` obrigatório no cadastro.
- [x] 3.2 Configurar `SecurityFilterChain` stateless com CSRF desabilitado, permitindo somente cadastro/login sem token dentro de `/api/v1/**` e Swagger/OpenAPI sem token apenas no perfil local fora da API.
- [x] 3.3 Emitir JWT HMAC com subject ID, chave externa e expiração de duas horas; em toda requisição protegida verificar existência da conta e carregar papel atual do banco, respondendo 401 após exclusão e 403 após perda do papel ADMIN.
- [x] 3.4 Implementar leitura/edição do próprio perfil e listagem/remoção administrativa de usuários sem retornar hash ou permitir alteração de papel pelo usuário.

## 4. Catálogo administrativo

- [x] 4.1 Implementar CRUD de desenvolvedoras e categorias, unicidade de nomes e conflito 409 ao remover referências ainda utilizadas.
- [x] 4.2 Implementar CRUD de jogos com desenvolvedora obrigatória, uma ou mais categorias, preço decimal e consulta paginada filtrável por título/categoria/desenvolvedora.
- [x] 4.3 Implementar CRUD de conquistas administrativas vinculadas a jogo e proteger todas as escritas de catálogo para ADMIN.
- [x] 4.4 Impedir excluir jogos com qualquer conquista/biblioteca/avaliação e conquistas desbloqueadas, mantendo FKs restritivas e resposta 409; retornar 400 para dados inválidos e 404 para referências inexistentes.

## 5. Biblioteca, avaliações e conquistas de usuário

- [x] 5.1 Implementar aquisição única ativa com usuário do JWT, data/hora e snapshot do preço, lista paginada, horas não negativas, remoção de aquisição preservando avaliações/conquistas e readquisição com novo preço/data.
- [x] 5.2 Implementar avaliação única para jogo possuído, edição apenas com aquisição ativa, exclusão pelo autor mesmo depois da remoção da biblioteca; nota 1–5 e comentário opcional com `null` para limpeza via PATCH.
- [x] 5.3 Implementar consulta autenticada das avaliações e média agregada, incluindo resultado sem avaliações.
- [x] 5.4 Implementar desbloqueio único de conquista somente para jogo possuído e pertencente à conquista; listar conquistas próprias com filtro opcional por jogo.

## 6. Docker, testes e documentação

- [x] 6.1 Criar `Dockerfile` multi-stage da API, `.dockerignore` e `.env.example` somente com placeholders; ignorar `.env` e nunca versionar segredos.
- [x] 6.2 Criar `compose.yaml` usando `postgres:17`, healthcheck, volume persistente e serviço API no perfil `local` dependente do banco saudável, configurados por variáveis de ambiente.
- [x] 6.3 Implementar bootstrap ADMIN somente no perfil `local` e com ativação explícita; exigir nome/e-mail/senha externos, usar BCrypt, não atualizar senha em reinícios e falhar em configuração incompleta, colisões ou ativação fora do perfil local sem promover USER.
- [x] 6.4 Criar testes unitários e de integração com `postgres:17` via Testcontainers cobrindo Flyway, contrato 400/404/409, filtros/paginação, JWT de conta excluída/perda de papel, docs locais, bootstrap idempotente, posse, histórico após remoção/readquisição e unicidade.
- [x] 6.5 Atualizar README com arquitetura, variáveis, perfil local/exceção Swagger, comandos Docker/Maven, PostgreSQL 17, migrations, bootstrap ADMIN seguro e exemplos do contrato HTTP.
- [ ] 6.6 Executar testes/build, subir a aplicação e o PostgreSQL com Docker Compose e exercitar Swagger, cadastro/login, fluxo admin de catálogo, aquisição, remoção/readquisição, avaliação e desbloqueio via API.
