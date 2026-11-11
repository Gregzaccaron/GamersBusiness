## Context

A aplicação usa Java 17/Spring Boot 3, PostgreSQL 17/Flyway, JWT e React/Vite. `LibraryService.acquire` confirma aquisições em transação e a interface atualiza a biblioteca depois do `201`. Há 48 jogos, notas e horas que podem contextualizar recomendações; hoje não existe integração OpenAI, armazenamento de recomendações ou endpoint correspondente.

Este commit registra somente o planejamento e as variáveis previstas. Elas não habilitam uma funcionalidade no código atual.

## Goals / Non-Goals

**Goals:**
- Recomendar até cinco jogos existentes, não adquiridos, com motivos em português.
- Atualizar após aquisições e atender bibliotecas anteriores à implementação.
- Preservar os contratos e dados atuais; isolar indisponibilidade e latência da OpenAI.
- Evitar chamadas por navegação, resultados obsoletos e exposição de credenciais/dados pessoais.

**Non-Goals:**
- Chatbot, compra automática, promoções inventadas, preços externos ou busca na internet.
- Embeddings, banco vetorial, Redis, RabbitMQ ou retreinamento de modelos para este catálogo pequeno.
- Recomendações aleatórias apresentadas como IA ou geração paga sem habilitação explícita.
- Regenerar automaticamente a cada edição de horas, avaliação ou abertura de página.

## Decisions

1. **Contexto e fornecedor.** Uma porta de aplicação recebe biblioteca, horas, notas, categorias e desenvolvedoras e uma lista limitada ao catálogo não adquirido. O adapter usa OpenAI Responses API e Structured Outputs com IDs e justificativas, sem ferramentas ou acesso do modelo ao banco. Não enviar usuário, e-mail, senha, JWT, chave nem comentários livres de avaliações. Descrições do catálogo são dados não confiáveis, não instruções. Configurar `store:false`; isso não é uma promessa de retenção zero pelo fornecedor. Alternativa descartada: um chatbot com ferramentas, que amplia escopo e superfície de risco sem necessidade.

2. **Seleção verificável.** Solicitar no máximo cinco sugestões distintas, ou menos quando houver poucos candidatos. Usar JSON Schema estrito e validar novamente IDs, duplicatas, quantidade e motivos não vazios no backend. O modelo não retorna preços, imagens ou URLs: a API obtém esses campos do catálogo atual. Rejeitar uma resposta inválida inteira em vez de preencher com dados fabricados. Sem biblioteca ou candidatos, não chamar a OpenAI.

3. **Geração persistente fora da requisição de compra.** Uma nova migration cria estado/tarefa por usuário e itens ordenados de recomendação. Aquisição bem-sucedida marca a geração desejada dentro da transação local; o worker só lê tarefas confirmadas e chama a OpenAI fora de transações de banco. Não alterar o corpo/status da aquisição e não esperar pela geração na requisição HTTP. Alternativa descartada: HTTP síncrono dentro de `LibraryService.acquire`, que seguraria a transação e acoplaria compras ao fornecedor externo.

4. **Coalescência e concorrência.** Identificar a geração por usuário e fingerprint da biblioteca, incluindo identidades das aquisições. Manter no máximo uma tarefa pendente para a versão mais recente por usuário. Processar com concorrência limitada e só publicar resultado se geração e fingerprint ainda corresponderem à biblioteca atual. Aquisições consecutivas não permitem que a resposta antiga substitua a nova. Tarefas confirmadas sobrevivem a reinícios; uma tarefa interrompida pode voltar a pendente na recuperação do worker. Uma falha conhecida termina em erro visível, sem laço de chamadas pagas automático.

5. **Bibliotecas existentes.** Quando habilitado e com chave, a reconciliação inicial agenda usuários com biblioteca não vazia e sem geração para sua versão atual. Reexecutar a inicialização não duplica tarefas nem refaz resultados prontos. Conta sem jogos, inclusive ADMIN, não gera custo. Não alterar os seeds para simular respostas OpenAI. Alternativa descartada: executar chamadas em uma migration Flyway, tornando inicialização/schema dependentes da rede e credenciais.

6. **Leitura privada e interface.** `GET /api/v1/me/recommendations` usa o usuário do JWT e retorna `{status,generatedAt,items}`, com estados `disabled`, `unavailable`, `empty`, `pending`, `ready` e `failed`. Cada item contém `{game,reason}`, enriquecido com os campos atuais de `GameResponse`. A leitura não provoca uma chamada OpenAI por navegação; filtra imediatamente jogos removidos ou agora adquiridos, mesmo em um resultado anterior. A loja apresenta a seção “Recomendados para você”, inclusive carregamento e indisponibilidade explícitos. Durante erro, sugestões anteriores elegíveis podem permanecer com data e indicação de desatualização; nunca alegar geração nova.

7. **Configuração.** `AI_RECOMMENDATIONS_ENABLED=false` por padrão; `OPENAI_API_KEY` vazia e `OPENAI_MODEL=gpt-5.6-terra` como exemplo configurável. Docker deve encaminhar variáveis somente ao serviço API quando a implementação estiver pronta; nunca usar prefixo `VITE_` ou chave no bundle. Não registrar credenciais ou payloads com dados privados. Habilitação sem chave resulta em indisponibilidade de IA, não falha da plataforma. A chave real exige uma conta com acesso e cobrança na OpenAI; o limite de gastos deve ser configurado no projeto do fornecedor. Limitar saída e aplicar timeout finito às chamadas.

## Risks / Trade-offs

- [Custos e limites do fornecedor] → habilitação explícita, resultados persistidos, processamento limitado e ausência de chamadas por visualização.
- [Compra durante geração ou remoção de jogo] → fingerprint, publicação condicional e filtro no momento da leitura.
- [Resposta plausível mas equivocada] → catálogo fechado, schema estrito, validação local e justificativas limitadas ao contexto fornecido.
- [Falha/reinício do worker] → tarefa persistente, estado recuperável e geração idempotente; não depender somente de evento em memória.
- [Metadados do catálogo usados como instruções maliciosas] → delimitar como dados, não conceder ferramentas ao modelo e validar a saída.
- [Conta sem histórico suficiente] → estado vazio honesto, sem inventar personalização.

## Migration Plan

1. Adicionar nova migration, modelos, porta, adapter e testes sem alterar migrations aplicadas.
2. Implementar geração assíncrona, carga inicial, endpoint e seção React preservando as aquisições atuais.
3. Encaminhar as variáveis no Compose e documentar habilitação; manter desabilitado por padrão.
4. Verificar estados desabilitado/sem chave sem chamadas pagas. Com chave fornecida pelo usuário, fazer smoke real controlado e verificar IDs, persistência e UI.
5. Habilitar localmente para processar bibliotecas existentes. Para interromper chamadas, desabilitar a flag; não apagar bibliotecas ou resultados. Correções de schema usam migration nova, sem reescrever histórico.

## Open Questions

Nenhuma decisão funcional bloqueante. O teste real do fornecedor depende de o usuário preencher uma chave com acesso ao modelo escolhido; essa chave nunca será incluída no Git.

## References

- https://developers.openai.com/api/docs/guides/structured-outputs
- https://developers.openai.com/api/docs/models/gpt-5.6-terra
- https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety
