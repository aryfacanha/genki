# Genki — instruções para assistentes (GPT, Codex, Claude, Claude Code)

TCC de Ary Façanha de Sá Neto (TADS/IFRS Campus Canoas), orientador Prof. Dr. Sandro José Ribeiro da Silva.
App Android para gerenciamento de medicamentos com cadastro assistido por voz. Trabalhe em português brasileiro.

Assistentes não compartilham conversas nem memória. **Os arquivos deste repositório e do Drive são o único meio de
passagem de contexto.** Onde está cada coisa: `docs/00_INDICE.md`.

## Diretriz do autor (21/10/2026)

Fazer **o mínimo suficiente** para atender os RF/RNF e o que a proposta prevê, com desenvolvimento rápido e fluido.
Desenvolvimento orientado a IA (Codex / Claude Code). Na dúvida, escolha a solução mais simples e registre a limitação.

## Ao iniciar uma tarefa

1. Ler `docs/ESTADO.md` e as decisões pertinentes em `docs/DECISOES.md`.
2. Abrir os originais necessários (proposta, planilha de requisitos, especificação) na versão atual.
3. `git pull`; registrar branch e commit de partida.
4. Apontar conflitos ou lacunas antes de depender deles. Se não conseguir abrir algo, diga qual recurso e qual
   permissão faltam. Não simule leitura.

## Ao concluir trabalho relevante

- Acrescentar um bloco em `docs/ENTREGAS.md` (modelo no próprio arquivo), identificando o assistente.
- Atualizar `docs/ESTADO.md` (o que mudou, pendências, próximo passo) e, se for o caso, `docs/DECISOES.md`.
- Acrescentar uma linha em `docs/REGISTRO_IA.md`.
- Sem acesso de escrita: entregar ao autor o bloco de entrega pronto para colar.
- Se um arquivo mudou desde a sua leitura, releia e mescle. Nunca sobrescreva nem duplique registros de outro assistente.

## Categorias — sempre diferencie

compromisso da proposta | decisão aceita pelo autor | sugestão pendente | implementado | verificado.
Sugestão de qualquer assistente só vira "aceita" com mensagem explícita do autor (registre data).
Código gerado ≠ compilado ≠ testado no aparelho. Texto proposto ≠ revisado pelo autor ≠ integrado à monografia.

## Regras do produto

- Especificação técnica vigente: `docs/ESPEC_ARQUITETURA.md`. Requisitos: planilha oficial (texto) e `docs/REQUISITOS_MAPA.md`.
- Kotlin + Compose + Room. Sem Hilt, WorkManager, DataStore, rede, conta, nuvem, anúncios ou LLM no app.
- Toda gravação passa pelo `DraftValidator`. Não criar outro caminho até o banco.
- Concentração ≠ dose. OCR nunca preenche dose, horários, dias ou tratamento.
- Lembrete reconhecido não é ingestão: nunca "Tomado", nunca histórico de tomadas.
- Datas civis no fuso do aparelho (nunca UTC para data).
- Não prometer precisão clínica nem reconhecimento offline universal.
- A proposta é o marco acadêmico: não amplie o escopo. Divergência em relação a ela deve ser registrada.
- Não invente referência, página, participante, dado ou resultado.

## Código

- Uma tarefa = um RF (ou poucos) + testes unitários da lógica pura + commit pequeno.
- Antes de commitar: `./gradlew assembleDebug test`. Informe o que não foi verificado no aparelho.
- Mensagem de commit: `tipo: resumo (RFxx)`, tipos `feat`, `fix`, `test`, `docs`, `chore`.
- Autoria de commits feitos por assistentes: **padrão ainda a definir pelo autor** (ver `docs/ESTADO.md`).
  Até lá, o autor do commit é o autor do TCC e o assistente aparece em `Co-Authored-By`.

## Implementação e revisão (app Android, DEC-026)

- Retrato técnico do código, como rodar, checagem no emulador, roteiro de revisão e Diário: `docs/HANDOFF_APP.md`.
  Leia antes de mexer no app e atualize-o ao terminar (seções 2, 3, 7 e Diário).
- Papéis: **Claude Code implementa; Codex revisa.** Sem limite do Claude Code, o Codex implementa em "modo só Codex"
  e o Claude Code revisa esse trabalho antes de começar algo novo.
- Revisões ficam em `docs/revisoes/REVISAO_<marco>_<ASSISTENTE>.md`, com achados S1–S4. Quem revisa não altera código
  sem pedido do autor.
- Mudança de configuração (`.claude/`, skills, `AGENTS.md`, Gradle, versões, manifest) vai para o Diário do handoff.
  As skills do Claude Code (`.claude/skills/`) só apontam para o handoff; o roteiro vale igual para todos os assistentes.
