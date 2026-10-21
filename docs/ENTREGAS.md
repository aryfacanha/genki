# Genki — log de entregas

Mais recente no topo. Um bloco por entrega relevante.

```text
ENTREGA — <tema> — <data> — <assistente (modelo exibido)>
Partida: <documentos/commit>
Realizado e arquivos alterados: <...>
Decisões aceitas pelo autor nesta sessão: <DEC-xxx ou "nenhuma">
Sugestões pendentes: <...>
Verificado: <...> | Não verificado: <...> | Limitações: <...>
Próxima tarefa e bloqueios: <...>
Links/commits: <...>
```

---

ENTREGA — Protótipo final (Genki(22)) versionado, ambiente local e regressão com Playwright — 21/10/2026 — Claude Code (Opus 5.5)
Partida: commit b3d98a4; export Genki(22) do Figma Make (`D:\TCC\Prototipo\figma-versions\`).
Realizado e arquivos alterados: repositório separado github.com/aryfacanha/genki-prototype (`src/` idêntico ao export Genki(22),
`AGENTS.md`, `docs/HANDOFF.md`, `vite.local.config.ts`, scripts `*:local`); Playwright MCP (Edge, @playwright/mcp 0.0.83) registrado no Claude Code;
`.gitignore` ignora `.playwright-mcp/`. Neste repositório: `docs/ESTADO.md`, `docs/ENTREGAS.md`, `docs/REGISTRO_IA.md`.
Decisões aceitas pelo autor nesta sessão: repositório separado para o protótipo (21/10).
Sugestões pendentes: 9 pendências de UX S1/S2 (`docs/HANDOFF.md` §5 do genki-prototype); lista Hoje sem "Adiado para 12:20" após avançar o alerta adiado (S3).
Verificado: `pnpm check:local` (typecheck + build) no Windows; regressão completa (9 testes rápidos do HANDOFF §7) no `pnpm dev:local` com Playwright MCP:
Home derivada dos medicamentos, excluir, desligar lembretes + Desfazer, "Visto às 12:00" sem implicar ingestão, voz até salvar, correção por toque,
câmera → voz (OCR só nome e concentração), validação na edição e proteção de alterações, adiamento e alerta sem resposta.
| Não verificado: Codex e Playwright MCP no Codex; `scripts/import-make-export.ps1` (nunca executado). | Limitações: protótipo web, não é o app Android.
Próxima tarefa e bloqueios: autor confirmar o Genki(22) como referência de UX; M0 (projeto Android).
Links/commits: genki-prototype 6f99287; este commit.

ENTREGA — Planilha de requisitos atualizada; protótipo em revisão registrado — 21/10/2026 — Claude (Opus 5.5)
Partida: commit 14cf308; planilha "Requisitos" (última alteração anterior: 04/09/2026).
Realizado e arquivos alterados: planilha — RF03 (texto), RF08 (observação), RF24 (texto), nova linha RNF11, conforme DEC-002 a 005.
`docs/REQUISITOS_MAPA.md`, `docs/DECISOES.md` (DEC-022), `docs/ESTADO.md`, `docs/REGISTRO_IA.md`.
Decisões aceitas pelo autor nesta sessão: autorização para aplicar DEC-002 a 005; DEC-022 (protótipo reaberto, informado pelo autor).
Sugestões pendentes: nenhuma nova.
Verificado: releitura das células alteradas na planilha após a escrita. | Não verificado: protótipo revisado (não acessível ao Claude).
Próxima tarefa e bloqueios: M0; exportação do protótipo revisado; celular de teste; e-mail ao orientador.
Links/commits: este commit.

ENTREGA — Decisões de arquitetura e metodologia; base compartilhada — 21/10/2026 — Claude (Opus 5.5)
Partida: commit 91a4c0a; Projeto do Claude (`03` v1.1, `claude/10_ESPEC_ARQUITETURA_MVP.md` v1.0, `claude/10_AVALIACAO_ETICA.md` v0.1); Drive (proposta, planilha).
Realizado e arquivos alterados: `AGENTS.md`, `CLAUDE.md`, `docs/00_INDICE.md`, `docs/ESTADO.md`, `docs/DECISOES.md`,
`docs/ESPEC_ARQUITETURA.md` (v2.0, com simplificações S1–S5), `docs/REQUISITOS_MAPA.md`, `docs/ENTREGAS.md`, `docs/REGISTRO_IA.md`.
No Drive: pasta `Avaliação/` e documento "Genki — Metodologia de avaliação e pauta com o orientador".
Decisões aceitas pelo autor nesta sessão: DEC-008 a DEC-018 e DEC-021 (recomendações aceitas com a diretriz "mínimo suficiente").
DEC-019 e DEC-020 registradas a partir das mensagens do autor de 20/10.
Sugestões pendentes: S1–S5 foram aplicadas pelo Claude sob a diretriz e podem ser vetadas.
Verificado: leitura da proposta e da planilha no Drive; planilha sem DEC-002 a 005; documento de avaliação criado e relido no Drive.
Não verificado: nada de código (não existe). Lista de heurísticas e itens da SUS não conferidos nos artigos.
Próxima tarefa e bloqueios: M0 (projeto Android). Bloqueios: celular de teste; reunião com o orientador; autorização para editar a planilha.
Links/commits: este commit.

ENTREGA — Acesso a Drive e GitHub; prompt de alinhamento para o GPT — 20/10/2026 — Claude (Opus 5.5)
Realizado: conectores Drive/Sheets/Docs verificados (leitura); repositório criado pelo autor; push testado (commit 91a4c0a, README).
Prompt de instruções permanentes e de atualização de estado entregue ao autor para o GPT.

ENTREGA — Diagnóstico de contexto e correções de requisitos — 19/10/2026 — Claude (Opus 5.5)
Realizado: leitura da proposta, planilha (CSV), protótipo v1 e documentos de contexto; conflitos identificados; DEC-001 a 007
propostas e aceitas; `03`, `06` e `09` atualizados no Projeto do Claude; especificação v1.0 e rascunho de avaliação v0.1 (conversas 1 e 2).
