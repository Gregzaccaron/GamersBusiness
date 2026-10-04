## Context

O repositório contém um Spring Boot inicial sem domínio implementado. O POM atual declara Spring Boot 4.1.1 e Java 27, enquanto o trabalho exige Spring Boot 3, Java 17 ou superior, PostgreSQL, migrations Flyway, Spring Security, Lombok, DTOs e Swagger. O PDF define 16 requisitos funcionais, e `DiagramaER.png` define nove tabelas relacionais e suas ligações. A mudança entrega uma API REST de aplicação acadêmica, sem compatibilidade com clientes já publicados.

## Goals / Non-Goals

**Goals:**
- Cobrir os requisitos funcionais e não funcionais do PDF e preservar as relações e regras de integridade do diagrama ER.
- Implementar Clean Architecture sem expor entidades JPA, separar requests/responses e limitar dependências entre domínio, aplicação e infraestrutura.
- Fazer o projeto funcionar tanto pela aplicação empacotada quanto por Docker Compose com PostgreSQL persistente.
- Tornar autenticação, autorização, validação, erros e documentação observáveis e testáveis por API.

**Non-Goals:**
- Interface web, pagamentos reais, integração com lojas externas ou OAuth de terceiros.
- Sessões, refresh tokens, autenticação por cookie ou CRUD anônimo.
- Migração de dados existentes; o repositório não tem banco nem API publicados.
- Orquestração de produção, Kubernetes, alta disponibilidade ou gestão externa de segredos.

## Decisions

1. **Versões e dependências:** alinhar o POM a Spring Boot 3.x e Java 17 ou superior, conforme o enunciado; manter Spring Web, Security e Lombok e adicionar Spring Data JPA, driver PostgreSQL, Flyway, Bean Validation, springdoc OpenAPI e suporte OAuth2 Resource Server/JWT. O projeto está em estágio inicial, portanto alinhar ao requisito é mais simples que manter Boot 4. Alternativa descartada: preservar Boot 4/Java 27, que diverge explicitamente do PDF e dificulta a compatibilidade ensinada em aula.

2. **Limites da Clean Architecture:** organizar sob `br.com.gregfabio.gamersbusiness` em `domain` (modelos/regras e erros), `application` (casos de uso, serviços e portas), `infrastructure` (persistência JPA, adapters e configuração) e `presentation` (controllers REST, DTOs, mapeadores HTTP e advice). Casos de uso dependem de portas; adapters JPA implementam portas de repositório. Transações ficam nos serviços/casos de uso da aplicação. DTOs são records validados e convertidos explicitamente; entidades JPA nunca saem da infraestrutura. Alternativa descartada: camadas controller-service-repository com dependência direta entre regra de negócio e JPA, mais curta inicialmente, mas acopla domínio à persistência.

3. **Modelo de persistência:** representar as nove tabelas do diagrama (`desenvolvedora`, `jogo`, `categoria`, `jogo_categoria`, `usuario`, `conquista`, `biblioteca`, `avaliacao`, `usuario_conquista`) com IDs bigint, nomes de coluna snake_case e FKs explícitas. `jogo_categoria`, `biblioteca` e `avaliacao` terão unicidade do par jogo/usuário ou jogo/categoria; `usuario_conquista` usa chave composta usuário/conquista. Nome de usuário, e-mail e nome de categoria são únicos. Preço e preço pago usam `NUMERIC`/`BigDecimal`; notas ficam entre 1 e 5; horas jogadas não podem ser negativas. `biblioteca.preco_pago` é snapshot no ato da aquisição. Remover uma aquisição apaga somente a entrada da biblioteca: avaliações e conquistas desbloqueadas continuam como histórico, e readquirir cria aquisição nova sem recriar avaliações/desbloqueios únicos. FKs históricas impedem apagar jogos com conquistas, avaliações ou biblioteca e apagar conquistas desbloqueadas; referências impeditivas resultam em 409. Alternativa descartada: exclusão em cascata ou `double` para preço, que perdem histórico ou precisão.

4. **Migrations como fonte do schema:** criar migrations SQL versionadas em `src/main/resources/db/migration`; configurar Hibernate com `ddl-auto=validate` (ou `none` em execução de migrations) e Flyway como único criador/evoluidor do schema. Restrições únicas e FKs no PostgreSQL são a última barreira contra duplicidade e exclusão inconsistente. Alternativa descartada: `ddl-auto=update`, proibido pelo RNF03 e não reproduzível entre ambientes.

5. **Autenticação e autorização:** API stateless com `SecurityFilterChain` explícita, CSRF desabilitado para o fluxo exclusivamente Bearer-token e sessões em `STATELESS`. Somente cadastro/login são públicos dentro de `/api/v1/**`; Swagger UI/OpenAPI é servido sem token exclusivamente no perfil `local`, fora desse prefixo, e desativado nos demais perfis. Usar Spring Security OAuth2 Resource Server para validar JWT HMAC assinado com chave externa e expiração de duas horas, evitando filtro JWT artesanal. Token contém subject igual ao ID estável e papel informativo; cada requisição autenticada recarrega a conta e suas authorities atuais no banco, rejeitando conta excluída com 401 e perda de permissão com 403. Passwords passam por BCrypt. Alternativa descartada: confiar apenas no papel do token ou criar sessão, que mantém acesso de contas excluídas até expirar ou diverge do requisito stateless.

6. **Regras de acesso e consistência:** obter o usuário atual do principal autenticado, nunca de um `usuarioId` confiado no body para operações próprias. USER só altera perfil, biblioteca, avaliações e conquistas próprios; ADMIN gerencia catálogo e pode administrar contas. Criar avaliação e desbloquear conquista exigem aquisição ativa do jogo; após remover a aquisição, avaliação e desbloqueios antigos permanecem consultáveis e únicos, mas editar avaliação ou desbloquear outra conquista exige readquirir o jogo (exclusão da avaliação própria continua permitida). Operações relacionadas usam transação e traduzem conflitos de unicidade/FK para 409. Alternativa descartada: aceitar IDs de usuário do cliente ou apagar histórico junto com a biblioteca, que causa IDOR ou perda de registros.

7. **Contrato HTTP:** seguir a tabela de rotas/DTOs da spec `api-contract-and-operations` sob `/api/v1`; JSON usa camelCase inglês, datas ISO-8601 e timestamps UTC. Controllers traduzem DTOs e não chamam repositórios. Validar requests com Bean Validation; campos desconhecidos e JSON malformado resultam em 400. `@RestControllerAdvice` e handlers de segurança devolvem JSON uniforme com timestamp, status, mensagem, path e erros por campo quando aplicável. Usar 201 ao criar, 204 ao remover, 400 para entrada inválida, 401/403 para segurança, 404 para referência inexistente e 409 para duplicidade/restrição. Coleções usam paginação; filtros de jogos por título/categoria/desenvolvedora aplicam AND e retornam ordem estável por ID; avaliações expõem média calculada sobre todos os registros. Alternativa descartada: envelopes e semântica de erros distintos por controller, que tornam clientes e Swagger inconsistentes.

8. **Docker e configuração local:** colocar em `gamersbusiness/` um `Dockerfile` multi-stage, `.dockerignore` e `compose.yaml` com serviços `api` e `postgres` usando a imagem oficial `postgres:17`, healthcheck, rede interna, volume nomeado e dependência saudável antes da API. Compose ativa perfil `local`; variáveis de ambiente fornecem conexão, segredo JWT e provisionamento ADMIN opcional, desabilitado por padrão. Quando ativado, requer usuário, e-mail e senha externos, cria ADMIN com BCrypt em transação antes de aceitar requisições e, em reinícios, não altera senha/papel existentes; dados incompletos ou colisão com outra conta falham na inicialização sem promover USER. Fora do perfil `local`, bootstrap é proibido. `.env.example` documenta placeholders sem segredos e `.env` é ignorado. Usar PostgreSQL 17 também no Testcontainers. Não criar imagem PostgreSQL própria: a oficial reduz manutenção. O Compose permite subir ambos ou apenas o banco para executar a API via Maven.

9. **Documentação e testes:** no perfil `local`, expor Swagger UI/OpenAPI fora de `/api/v1/**` com esquemas DTO e Bearer JWT; qualquer chamada de negócio pelo UI continua protegida. Desabilitar docs fora de `local`. README documenta configuração do perfil, segredos, bootstrap, versão do banco, migrations, rotas e fluxo de teste. Cobrir regras com testes unitários e fluxos REST com PostgreSQL 17 via Testcontainers: 401/403 para token inválido/conta excluída/permissão perdida, criação idempotente de ADMIN, exceção local de docs, filtros/paginação, erros 400/404/409, histórico após remoção e readquisição, unicidade e propriedade. Alternativa descartada: confiar apenas em H2/mocks, que não validam o schema PostgreSQL.

## Risks / Trade-offs

- [Redução de Spring Boot 4 para Boot 3] → atende o enunciado, mas exige alinhar dependências e APIs disponíveis; selecionar uma versão Boot 3 compatível com Java instalado e validar o build completo.
- [Clean Architecture aumenta número de classes e mapeamentos] → manter portas apenas nas fronteiras de I/O e casos de uso coesos, sem criar abstrações para operações triviais.
- [Chave JWT e credenciais no Compose] → `.env` ignorado, `.env.example` apenas com placeholders sem senhas, bootstrap ADMIN desabilitado por padrão e restrito ao perfil `local`.
- [Testcontainers e execução Docker] → os testes de integração exigem Docker disponível; manter testes puros de domínio sem dependência do daemon.
- [A média das avaliações varia conforme alterações] → calcular por consulta agregada, sem armazenar cache derivado que possa ficar desatualizado.
- [Exclusões podem conflitar com histórico] → usar FKs restritivas e traduzir a violação em 409, evitando apagamentos em cascata de compras e avaliações.
- [Documentação local exposta caso perfil `local` seja usado em servidor público] → não habilitar perfil `local` fora do desenvolvimento; desabilitar endpoints Swagger e bootstrap nos demais perfis.
- [Consulta ao banco em toda requisição autenticada] → custo adicional previsível para rejeitar imediatamente tokens de contas removidas e refletir papel atual; consultas por chave primária/indexada.

## Migration Plan

1. Ajustar a versão Java/Spring e adicionar dependências/configuração de ambiente.
2. Criar migrations Flyway, constraints e índices conforme o diagrama; iniciar banco vazio com Compose.
3. Implementar domínio e casos de uso, adapters JPA, segurança e endpoints; manter o schema sob Flyway.
4. Construir a imagem da API e iniciar `postgres` e `api` com `docker compose up --build`; executar testes unitários e de integração.
5. Validar Swagger e os fluxos de usuário/admin; documentar os comandos e configurações no README.

Não há dados nem consumidores existentes para rollback. Em desenvolvimento, interromper os serviços; se necessário apagar o volume local, fazê-lo conscientemente porque isso remove os dados de teste. Depois de uma migration aplicada, corrigir schema com nova migration compensatória, sem editar migration já aplicada.

## Open Questions

Nenhuma decisão bloqueante: entidades, papéis, regras funcionais, versões mínimas e banco são definidos no PDF/diagrama. A implementação deve escolher versões patch de dependências compatíveis entre si e confirmar disponibilidade de Docker/PostgreSQL na máquina de execução.
