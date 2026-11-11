## ADDED Requirements

### Requirement: Recomendações são privadas e restritas ao catálogo
O sistema MUST disponibilizar `GET /api/v1/me/recommendations` apenas para usuário autenticado, com identidade obtida do JWT. A resposta MUST conter `{status,generatedAt,items}` e no máximo cinco itens distintos `{game,reason}`, em ordem de recomendação. `game` MUST usar os campos atuais de `GameResponse`; cada jogo MUST existir no catálogo e MUST NOT pertencer à biblioteca atual do usuário. `reason` MUST ser uma justificativa não vazia em português. O sistema MUST NOT aceitar um ID de outro usuário para selecionar as recomendações.

#### Scenario: Leitura própria autenticada
- **WHEN** um usuário autenticado consulta suas recomendações prontas
- **THEN** recebe `200` com apenas sugestões elegíveis de sua própria conta, justificativas e data de geração

#### Scenario: Requisição sem autenticação
- **WHEN** um cliente consulta recomendações sem token válido
- **THEN** a API responde `401` no formato uniforme existente, sem expor biblioteca ou recomendações

#### Scenario: Jogo adquirido ou removido depois da geração
- **WHEN** um item anteriormente recomendado já foi adquirido ou deixou de existir no catálogo
- **THEN** esse item não aparece na leitura atual, sem substituir seu ID por um jogo inventado

### Requirement: Aquisição confirmada agenda geração assíncrona
Quando a IA estiver habilitada e configurada, uma aquisição confirmada MUST agendar a atualização das recomendações para a versão mais recente da biblioteca. A chamada externa MUST ocorrer somente após o commit e fora da transação de aquisição. O endpoint de aquisição MUST preservar seu contrato `201` e MUST NOT esperar pela OpenAI. Aquisições rejeitadas ou transações revertidas MUST NOT gerar chamadas ao fornecedor.

#### Scenario: Compra seguida de processamento
- **WHEN** o usuário adquire um novo jogo com sucesso
- **THEN** a compra é confirmada antes da geração e as recomendações ficam pendentes até o resultado ser persistido

#### Scenario: Aquisição duplicada
- **WHEN** o usuário tenta adquirir um jogo que já possui
- **THEN** recebe o conflito existente e não gera uma nova chamada paga de recomendação

#### Scenario: Fornecedor indisponível
- **WHEN** a OpenAI falha ou excede o timeout durante uma geração
- **THEN** a aquisição confirmada permanece intacta e a recomendação passa a um estado de falha explícito

### Requirement: Bibliotecas existentes recebem geração inicial idempotente
Ao inicializar com IA habilitada e chave configurada, o sistema MUST agendar uma primeira geração para cada biblioteca existente não vazia que não tenha geração correspondente à sua versão atual. O sistema MUST NOT modificar bibliotecas, credenciais, avaliações ou conquistas para fazer essa carga. Bibliotecas vazias MUST NOT provocar chamadas OpenAI.

#### Scenario: Usuário anterior à integração
- **WHEN** a integração é habilitada para um usuário que já possui jogos e ainda não tem recomendações
- **THEN** sua biblioteca é considerada na geração inicial sem exigir uma nova aquisição

#### Scenario: Reinício sem mudança de biblioteca
- **WHEN** a aplicação reinicia e já há tarefa pendente ou resultado correspondente à biblioteca atual
- **THEN** não cria uma tarefa duplicada nem regenera o resultado pronto apenas pelo reinício

### Requirement: Resultados persistidos respeitam a versão da biblioteca
O sistema MUST persistir tarefas/estado, itens ordenados e data de geração no PostgreSQL por nova migration Flyway. MUST associar cada geração à versão/fingerprint da biblioteca e MUST publicar somente resultados ainda correspondentes à versão desejada. Tarefas confirmadas MUST sobreviver a reinícios. A leitura repetida da loja MUST NOT executar uma chamada OpenAI por visualização.

#### Scenario: Aquisições consecutivas durante geração
- **WHEN** uma segunda aquisição muda a biblioteca enquanto a primeira recomendação está sendo calculada
- **THEN** a resposta antiga não substitui as recomendações da versão mais nova e o processamento atende à versão atual

#### Scenario: Navegação repetida
- **WHEN** o usuário abre a loja repetidamente sem alterar a biblioteca
- **THEN** o sistema reutiliza os resultados persistidos sem novas chamadas pagas por abertura

### Requirement: A resposta OpenAI é estruturada e validada
O adapter MUST usar a OpenAI Responses API com Structured Outputs e lista de candidatos do catálogo excluindo os jogos adquiridos. O contexto MUST considerar os jogos da biblioteca, categorias, desenvolvedoras, horas e notas disponíveis. O backend MUST rejeitar IDs fora da lista enviada, duplicatas, mais de cinco itens ou justificativas vazias, mesmo se o JSON for válido. MUST NOT apresentar um fallback fabricado como resultado da OpenAI.

#### Scenario: Resposta com ID não permitido
- **WHEN** o fornecedor retorna um jogo fora dos candidatos ou IDs repetidos
- **THEN** o sistema rejeita a resposta e sinaliza falha sem persistir uma lista pronta inválida

#### Scenario: Não há candidatos
- **WHEN** o usuário já possui todos os jogos do catálogo ou tem biblioteca vazia
- **THEN** a API indica estado `empty`, não chama a OpenAI e a interface explica a ausência de recomendações

### Requirement: Credenciais e dados pessoais permanecem fora do cliente e do prompt
O sistema MUST usar configuração exclusiva do backend: `AI_RECOMMENDATIONS_ENABLED` desabilitada por padrão, `OPENAI_API_KEY` externa e `OPENAI_MODEL` configurável. A chave MUST NOT aparecer no React, responses, logs ou arquivos versionados. O prompt MUST NOT incluir nome, e-mail, senha, hash, JWT ou chave do usuário, e MUST NOT incluir comentários livres de avaliações. Metadados enviados MUST ser tratados como dados, não como instruções; a requisição MUST usar `store:false` e timeout finito.

#### Scenario: IA desabilitada ou sem chave
- **WHEN** a flag está desabilitada ou a chave está ausente
- **THEN** não há chamadas OpenAI, as funções normais continuam disponíveis e a API retorna estado `disabled` ou `unavailable`, respectivamente

#### Scenario: Configuração compartilhada no Git
- **WHEN** o exemplo de ambiente é publicado
- **THEN** contém somente os nomes das variáveis, flag desabilitada, chave vazia e modelo de exemplo, sem credenciais reais

### Requirement: A loja comunica a origem e o estado das recomendações
A interface MUST apresentar “Recomendados para você” com capas/preços atuais e justificativas quando houver resultados elegíveis. MUST distinguir `disabled`, `unavailable`, `empty`, `pending`, `ready` e `failed`; MUST NOT esconder uma compra bem-sucedida por falha da IA. Se exibir sugestões anteriores durante uma falha, MUST indicar que são anteriores e informar a data de geração, mantendo a filtragem de elegibilidade.

#### Scenario: Geração pendente e posterior sucesso
- **WHEN** a geração passa de pendente para pronta
- **THEN** a interface deixa o indicador de processamento e exibe os cards e motivos correspondentes à conta autenticada

#### Scenario: Erro sem resultado anterior
- **WHEN** a geração falha e não existe recomendação anterior válida
- **THEN** a interface apresenta indisponibilidade explícita sem cards simulados
