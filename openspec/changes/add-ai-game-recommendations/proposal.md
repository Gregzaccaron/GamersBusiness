## Why

O catálogo de 48 jogos e as bibliotecas existentes permitem personalizar a descoberta, mas hoje a loja oferece apenas busca, filtros e ordenação. Recomendações explicadas após uma aquisição acrescentam uso concreto de IA ao trabalho sem transformar a plataforma em um chatbot.

## What Changes

- Criar uma seção “Recomendados para você” com até cinco jogos do catálogo e justificativas curtas em português.
- Gerar recomendações com OpenAI usando biblioteca, categorias, desenvolvedoras, horas e notas do próprio usuário; excluir jogos já adquiridos.
- Gerar em segundo plano após uma aquisição confirmada e preparar uma primeira geração para bibliotecas existentes quando a integração for habilitada.
- Persistir resultados e estado de processamento no PostgreSQL; não chamar a OpenAI a cada abertura da loja nem aceitar resultados de versões antigas da biblioteca.
- Manter aquisição e demais funções disponíveis quando a IA estiver desabilitada, sem chave ou com falha; nunca apresentar sugestões simuladas como resposta da OpenAI.
- Documentar configuração exclusiva do backend por `AI_RECOMMENDATIONS_ENABLED`, `OPENAI_API_KEY` e `OPENAI_MODEL`; não publicar chaves reais.
- Este registro entrega a proposta e o exemplo de ambiente. O código da integração, migrations, endpoints e interface serão implementados nas tarefas da mudança.

## Capabilities

### New Capabilities

- `ai-game-recommendations`: recomendações privadas, limitadas ao catálogo, com geração assíncrona, carga inicial de bibliotecas existentes, persistência, validação da resposta e configuração segura.

### Modified Capabilities

Nenhuma. Os contratos atuais de aquisição, autenticação, catálogo e biblioteca permanecem válidos; os efeitos adicionais são especificados na nova capability.

## Impact

- Backend Java/Spring Boot: serviços e portas de recomendação, adapter OpenAI, processamento de tarefas e gatilho no fluxo de aquisição.
- PostgreSQL/Flyway: nova migration para estado de geração e resultados por usuário, sem alterar migrations aplicadas ou dados existentes.
- API: leitura autenticada em `/api/v1/me/recommendations`; identidade derivada do JWT.
- Frontend React: cards de recomendações e estados de carregamento, indisponibilidade e ausência de biblioteca.
- Docker/documentação: variáveis somente no serviço da API, placeholders em `.env.example`, instruções de habilitação e custo.
- Dependência externa: OpenAI Responses API com Structured Outputs; chamadas pagas exigem chave e habilitação explícita.
