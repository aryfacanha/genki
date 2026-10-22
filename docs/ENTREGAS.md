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

ENTREGA — Protótipo: RV-P4d-01 + P4e "adiado na lista Hoje" + revisões do Codex — 22/10/2026 — Claude Code (Opus 5.5) e Codex
Partida: genki-prototype `a46c0dc`; principal `4f1b31a`.
Realizado e arquivos alterados: RV-P4d-01 (`b9d14cb`, `shared.tsx`, `MedicamentosScreen.tsx`) — aviso de exclusão em faixa própria entre a lista e "Adicionar medicamento", sem cobrir cartões. P4e (`5de2ead`, `GenkiHome.tsx`, `GenkiFocused.css`) — quando o alerta adiado volta, a linha da lista Hoje mostra "Adiado para HH:MM" além de "Agora". Revisões do Codex (`codex exec`, Playwright local/Edge headless): `docs/REVISAO_RV_P4D_01_CODEX.md` e `docs/REVISAO_P4E_CODEX.md` (`0e549fd`). `docs/HANDOFF.md` no protótipo; aqui, os três registros.
Decisões aceitas pelo autor nesta sessão: aviso de exclusão em faixa própria; P4e como próximo pacote; próximo: "Desfazer" na exclusão.
Verificado: `pnpm.cmd check:local`; Playwright (Claude e Codex) 360×800: faixa do aviso com 4 e 6 medicamentos, overlays de Hoje/Detalhes iguais, adiar 10/20/30 min, avançar, sem resposta, Entendi + Desfazer; Home 615/615. Codex: sem regressão; S3 preexistente "exclusão sem Desfazer"; S4 linha alta na lista Hoje (sem ação).
Não verificado: Android, leitor de tela real, texto ampliado; §7 completa não repetida (voz, câmera e edição não alteradas).
Próxima tarefa e bloqueios: "Desfazer" na exclusão (Claude Code implementa, Codex revisa); Editar e voz com 2+ medicamentos ainda rolam.
Links/commits: https://github.com/aryfacanha/genki-prototype ; `b9d14cb`, `5de2ead`, `0e549fd`.

ENTREGA — Protótipo: P4d "Medicamentos e Detalhes sem rolar" + revisão do Codex — 21/10/2026 — Claude Code (Opus 5.5) e Codex
Partida: genki-prototype `6dc3514`; principal `efd99b1`.
Realizado e arquivos alterados: P4d (`352293e`, `MedicamentosScreen.tsx`, `DetalhesMedicamento.tsx`, `index.css`) — espaçamento menor e cartões sem ícone decorativo na lista; Detalhes com a lista em duas colunas da revisão ("Duração"). Revisão do Codex (`codex exec`, navegador via Playwright local): `docs/REVISAO_P4D_CODEX.md`. `docs/HANDOFF.md` no protótipo; aqui, os três registros.
Decisões aceitas pelo autor nesta sessão: tirar o ícone dos cartões da lista; Detalhes no mesmo formato de duas colunas da revisão.
Verificado: `pnpm.cmd check:local`; Medicamentos 575/575 e Detalhes 720/720 (inclusive período, "Ainda não sei" e lembretes desligados), lista com 6 rola com a pílula; §7 no Playwright (Claude e Codex); console sem erros.
Não verificado: Android, leitor de tela real, texto ampliado.
Próxima tarefa e bloqueios: decisão do autor sobre RV-P4d-01 (S3, aviso de exclusão cobre cartão em lista longa, preexistente); Editar e voz com 2+ medicamentos ainda rolam.
Links/commits: https://github.com/aryfacanha/genki-prototype ; `352293e`.

ENTREGA — Protótipo: P4c "Hoje: Agenda clara" + revisão do Codex — 21/10/2026 — Claude Code (Opus 5.5) e Codex
Partida: genki-prototype `679c96e`; principal `ee84bb9`.
Realizado e arquivos alterados: P4c (`741fbf1`, `GenkiHome.tsx`, `GenkiFocused.css`) — lista Hoje recolhida atrás de "Ver todos os horários de hoje (N)", cartão Agora/Próximo e linha "Depois" mantidos. Revisão do Codex (`codex exec`, navegador via Playwright local): um S3, `aria-controls` sem alvo com a lista recolhida → corrigido pelo Claude Code (`1d10d75`, seção montada com `hidden`). `docs/HANDOFF.md` no protótipo; aqui, os três registros.
Decisões aceitas pelo autor nesta sessão: Agenda clara sem estado "Aguardando resposta" e sem contagem de lembretes anteriores; linguagem do produto mantida.
Verificado: `pnpm.cmd check:local`; Home 615/615 sem pílula; expandir/recolher, `aria-expanded`, Losartana 08:00 e 20:00, Entendi/Desfazer, adiar → "Sem resposta… 12:40", desligar/Desfazer (Codex no navegador); após a correção, recolher/expandir e horários no Playwright MCP; console sem erros.
Não verificado: Android, leitor de tela real, texto ampliado; itens de voz/câmera/edição da §7 não repetidos (arquivos não alterados).
Próxima tarefa e bloqueios: telas que ainda rolam (Medicamentos, Detalhes, Editar, revisão da voz com 2+ medicamentos) — decisão do autor.
Links/commits: https://github.com/aryfacanha/genki-prototype ; `741fbf1`, `1d10d75`.

ENTREGA — Protótipo: revisão independente P4b no navegador — 21/10/2026 — Codex
Partida: genki-prototype `cf506ab` (produto `36e8fe5`); principal `9f46df4`.
Realizado e arquivos alterados: revisão do diff P4b-1/2/3 e verificação independente com Playwright MCP diretamente na sessão interativa, concluindo a pendência das tentativas de CLI que falharam. Sem achados novos e sem alteração em `src/`. Relatório `docs/REVISAO_P4B_CODEX.md` e atualização do HANDOFF no protótipo; aqui, os três registros de documentação.
Decisões aceitas pelo autor nesta sessão: executar a verificação sugerida pelo Claude; nenhuma nova decisão de produto.
Verificado: `pnpm.cmd check:local`; §7 9/9; etapas com validação/foco/preservação, Alterar/retorno/descartar/salvar, pílula até fim e oculta com diálogos, Período da voz corrigido com valores anteriores e salvo em Detalhes; screenshots inspecionadas. Etapas 639/639 e revisão manual 573/573 no frame 360×800. Console final sem erros retornados.
Não verificado: Android, leitor de tela real, voz pt-BR real, fallback digitado inacessível e participantes. Limitações: protótipo web; sem repetir todos os cenários históricos de texto ampliado/muitos horários.
Próxima tarefa e bloqueios: decisões do autor sobre UX/MyTherapy e início do Android; revisão no navegador do P4b já concluída. Registros enviados pelo Claude Code com autorização do autor.
Links/commits: https://github.com/aryfacanha/genki-prototype ; `docs/REVISAO_P4B_CODEX.md`.

ENTREGA — Protótipo: revisão de P2/P3, P4a e P4b (telas sem rolar) — 21/10/2026 — Claude Code (Opus 5.5)
Partida: genki-prototype `195dc15` (P3) + correções VC não commitadas do Codex; este repositório `a8a6d6b`.
Realizado e arquivos alterados: no genki-prototype — revisão independente de P2/P3/VC (achado RV-01 corrigido, `360535c`); P4a opções de dias empilhadas e TTS só em voz portuguesa (`6a3fdcc`); P4b-1 pílula "Mais abaixo" (`79658c2`); P4b-2 cadastro manual em 3 etapas com "Alterar" por seção na revisão (`a10ce5f`); P4b-3 revisão final em cartões de duas colunas, manual cabendo sem rolar (`36e8fe5`); `docs/HANDOFF.md`. Aqui: `docs/ESTADO.md`, `docs/ENTREGAS.md`, `docs/REGISTRO_IA.md`.
Decisões aceitas pelo autor nesta sessão: P4a/P4b pedidos pelo autor; cadastro manual em etapas autorizado (muda fluxo aprovado do Digitar); pílula cobrindo conteúdo aceita; edição e voz mantidas em tela única.
Sugestões pendentes: Hoje com "Ver todos" e compactar Medicamentos/Detalhes (decisão do autor); Período com "(antes: …)" fica longo (4 linhas) na revisão da voz (S4).
Verificado: `pnpm.cmd check:local` em cada pacote; regressão §7 9/9 no Vite/Playwright MCP em 21/10 (incluindo "Desfazer" do Entendi); linha "Período" da revisão da voz vista e salva (Detalhes "De 09/09/2026 até 15/10/2026"); revisões do Codex (só código) com achados tratados. Console: só o 404 do favicon.
Não verificado: leitor de tela real, Android/aparelho, leitura com voz pt-BR instalada, respostas digitadas da voz. Limitações: protótipo web.
Próxima tarefa e bloqueios: autor decide próximos passos de P4b e confirma referência de UX; M0 Android segue pendente.
Links/commits: https://github.com/aryfacanha/genki-prototype (`360535c`..`36e8fe5`); este commit.

ENTREGA — Protótipo P3: rolagem, acessibilidade e retorno — 21/10/2026 — Codex (modelo não identificado)
Partida: genki-prototype 72e38fa (Git limpo); este repositório 4375e23, main, cópia obtida do remoto nesta sessão.
Realizado e arquivos alterados: no genki-prototype, itens 5, 6 e 9 do handoff: degradê compartilhado que aparece quando há conteúdo abaixo; cartões com botão dentro do item de lista e nome acessível completo; rótulos associados, horários com IDs distintos, grupos de escolha única e navegação por setas/Home/End; retorno para Hoje após cadastro iniciado nessa tela (manual e voz/câmera). Dados, parser e validação preservados. `docs/HANDOFF.md` atualizado. Aqui: `docs/ESTADO.md`, `docs/ENTREGAS.md`, `docs/REGISTRO_IA.md`.
Decisões aceitas pelo autor nesta sessão: avançar para P3 em continuidade ao modo só Codex; sem mudança de requisitos oficiais ou regras de negócio.
Sugestões pendentes: revisão independente de P2/P3; decisões de UX anteriores continuam pendentes.
Verificado: `pnpm.cmd check:local` (typecheck/build); regressão §7 9/9 no Vite/Playwright MCP; rolagem início/fim e screenshot; nome/semântica/Enter dos cartões, rótulos e múltiplos horários, datas manuais, seleção por teclado em tratamento e Ajustes, formulário vazio e edição inválida/continuar/descartar; cadastro iniciado em Hoje retorna a Hoje, iniciado na lista retorna à lista; voz e câmera → voz salvos com retorno a Hoje. Console: somente 404 conhecido do favicon; sem erro de runtime capturado.
Não verificado: leitor de tela real, Android/aparelho, datas dos estados alternativos da voz e respostas digitadas de P2. Limitações: protótipo web; modo só Codex — aguardando revisão.
Próxima tarefa e bloqueios: Claude Code revisa P2/P3 quando voltar; confirmação final de UX e M0 Android continuam pendentes.
Links/commits: https://github.com/aryfacanha/genki-prototype (pacote P3 e `docs/HANDOFF.md`); este commit.

ENTREGA — Protótipo P2: clareza dos formulários — 21/10/2026 — Codex (modelo não identificado)
Partida: genki-prototype 7eae1f8 (Git limpo); este repositório f32aa25, branch main, cópia obtida do remoto nesta sessão.
Realizado e arquivos alterados: no genki-prototype, exemplos como ajuda persistente no cadastro/edição/correção por toque e respostas digitadas da voz; valores 18px semibold nas revisões/Detalhes; nomes completos dos dias nos resumos, lista e correções da voz. Ajuste localizado na associação de ajuda/erro, preservando as regras de validação e dados internos. `docs/HANDOFF.md` atualizado. Neste repositório: `docs/ESTADO.md`, `docs/ENTREGAS.md`, `docs/REGISTRO_IA.md`.
Decisões aceitas pelo autor nesta sessão: modo só Codex; implementar P2 (itens 2, 3 e 4 do handoff), sem alteração de requisitos oficiais ou dados/regras.
Sugestões pendentes: itens 5, 6 e 9 do handoff; decisões de UX pendentes já registradas.
Verificado: `pnpm.cmd check:local` (typecheck e build); regressão completa do handoff §7 (9/9) com Playwright MCP no Vite; cadastro manual com revisão/retorno/salvamento, dias específicos, edição inválida/descartar, associação ajuda/erro e tipografia computada; formulário e revisão de voz vistos em screenshots. Recarga final e abertura do formulário sem erros capturados no console/runtime.
| Não verificado: respostas digitadas dentro da voz (componente compilado; estado de falha não alcançado no percurso atual da simulação); revisão independente do Claude Code; Android/aparelho. | Limitações: protótipo web; modo só Codex — aguardando revisão.
Próxima tarefa e bloqueios: Claude Code revisa P2 quando voltar; autor escolhe próximo pacote. M0 Android e confirmação da referência final de UX seguem pendentes.
Links/commits: https://github.com/aryfacanha/genki-prototype (pacote P2 e `docs/HANDOFF.md`); este commit.

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
