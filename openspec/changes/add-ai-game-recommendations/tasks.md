## 1. Persistência e domínio

- [ ] 1.1 Criar nova migration Flyway para tarefas/estado por usuário, versão da biblioteca, itens ordenados e data de geração, sem alterar migrations aplicadas.
- [ ] 1.2 Implementar modelos e portas de persistência com isolamento por usuário, estados documentados e publicação condicional por geração/fingerprint.
- [ ] 1.3 Implementar recuperação de tarefa interrompida e unicidade da geração pendente por usuário.

## 2. Contexto e adapter OpenAI

- [ ] 2.1 Construir contexto a partir da biblioteca completa, categorias, desenvolvedoras, horas e notas, excluindo dados pessoais e comentários livres.
- [ ] 2.2 Selecionar candidatos existentes não adquiridos e retornar estado vazio sem chamada externa quando não houver biblioteca ou candidatos.
- [ ] 2.3 Implementar adapter Responses API com modelo configurável, Structured Outputs, `store:false`, limite de saída e timeout finito, sem ferramentas.
- [ ] 2.4 Validar quantidade, IDs permitidos, unicidade e justificativas antes de persistir resultados; rejeitar respostas inválidas sem fallback fabricado.

## 3. Gatilhos e processamento

- [ ] 3.1 Agendar a geração desejada junto à aquisição confirmada, preservando resposta `201` e sem chamar OpenAI na transação de compra.
- [ ] 3.2 Implementar worker com concorrência limitada e chamada externa fora da transação; impedir que resultado antigo sobrescreva uma geração mais recente.
- [ ] 3.3 Agendar carga inicial idempotente para bibliotecas existentes quando habilitado e configurado, sem alterar seeds nem coleções.
- [ ] 3.4 Persistir falha externa e preservar aquisições e resultados anteriores elegíveis, sem repetição paga automática em cada navegação.

## 4. API, interface e configuração

- [ ] 4.1 Implementar `GET /api/v1/me/recommendations` protegido por JWT, com estados e DTOs documentados, filtrando jogos removidos ou agora adquiridos.
- [ ] 4.2 Implementar “Recomendados para você” na loja com cards, motivos, data e estados de pendência, vazio e indisponibilidade, incluindo atualização após aquisição.
- [ ] 4.3 Implementar consumo das três variáveis previstas e encaminhamento exclusivo ao serviço API no Compose; preservar aplicação normal com flag desabilitada ou chave ausente.

## 5. Verificação e documentação

- [ ] 5.1 Cobrir isolamento entre contas, compra duplicada/revertida, persistência, carga inicial idempotente, ausência de candidatos, concorrência e falha do fornecedor com testes determinísticos e PostgreSQL 17.
- [ ] 5.2 Verificar validação da resposta e ausência de dados pessoais no payload usando servidor HTTP local controlado; verificar que navegação não aumenta chamadas.
- [ ] 5.3 Executar a aplicação no Docker e exercitar os estados reais sem chave e desabilitado; com chave fornecida pelo usuário, comprovar uma geração real OpenAI e sua persistência após reinício.
- [ ] 5.4 Verificar os cards e estados no navegador desktop/mobile e atualizar README/contrato da API para refletir a funcionalidade implementada, removendo o aviso de planejamento apenas após a entrega completa.
