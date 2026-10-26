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

ENTREGA — App Android: M1 CRUD manual — 26/10/2026 — Claude Code (Opus 5.5)
Partida: genki `main` em `cbed4c9`; genki-prototype `295212e` (telas de lista, detalhes, cadastro manual em etapas, revisão, edição).
Realizado e arquivos alterados: `domain/` (`Medication`, `MedicationDraft`, `DraftValidator` com `DUPLICATE_TIME`), `data/` (entidades, conversores, `MedicationDao`, `GenkiDatabase`, `MedicationRepository` que só aceita `Valid`), `AppContainer`; `ui/medications` (lista), `ui/details` (detalhes, lembretes liga/desliga, exclusão com confirmação), `ui/form` (3 etapas + revisão; edição em página única; "Descartar alterações?"), `ui/components`, `ui/MedicationText`; navegação e aviso (snackbar) em `GenkiApp`. Testes: `DraftValidatorTest`, `MedicationTextTest`, `MappersTest`. Docs: DEC-027 em `DECISOES`, `DIVERGENCIAS_PROTOTIPO`, `ESPEC_ARQUITETURA` §3–§4, `HANDOFF_APP`, `ESTADO`, `REGISTRO_IA`.
Decisões aceitas pelo autor nesta sessão: DEC-027 (#2 erro para horário repetido; #7 rótulos do protótipo; #12 exclusão sem "Desfazer"), 26/10.
Sugestões pendentes: escolha Voz/Digitar/Câmera só no M3 (hoje "Adicionar" abre direto o manual); textos em Kotlin em vez de `strings.xml`.
Verificado: `.\gradlew.bat assembleDebug test` (27 testes); fluxo completo no emulador Pixel_6_2 API 35 (erros por etapa, horário repetido, datas, revisão/Alterar, salvar, reabrir o app, detalhes, lembretes, edição, descarte, exclusão, fonte 1,5×); `GenkiEval` registra cadastro, edição e exclusão. | Não verificado: aparelho físico, TalkBack, fonte ≥ 1,8× no formulário, `FormViewModel` sem teste automatizado. | Limitações: calendário/relógio no idioma do sistema; lembretes só gravam a escolha até o M2.
Próxima tarefa e bloqueios: revisão do Codex (M1); autor decidir #13, #14, #16; M2.
Links/commits: `f2ff130`; este commit.

ENTREGA — M0 no emulador, rótulo com fonte ampliada e merge com o chat do Projeto — 26/10/2026 — Claude Code (Opus 5.5)
Partida: genki `main` em `21a995b` (local, à frente do remoto); remoto em `3dbaca6`.
Realizado e arquivos alterados: `/testar-emulador` no Pixel_6_2 (API 35); `ui/navigation/GenkiApp.kt` (rótulo da barra com `autoSize`, 16 → mín. 12 sp) (`d74a75b`); decisão do handoff renumerada de DEC-025 para DEC-026, porque o chat do Projeto já usou DEC-025 (referências em `AGENTS.md` e docs; as mensagens dos commits `21a995b` e `d74a75b` ainda dizem DEC-025); merge com o remoto mantendo os dois lados em `DECISOES`, `ENTREGAS`, `ESTADO`, `REGISTRO_IA`; pendência "Confirmar o Genki(22) como referência" retirada (resolvida pela DEC-025). `HANDOFF_APP`, `ESTADO`, `REGISTRO_IA`.
Decisões aceitas pelo autor nesta sessão: correção do rótulo e renumeração para DEC-026 (26/10, pedido explícito).
Sugestões pendentes: rótulo "Medicamentos" ainda corta com fonte ≥ 1,8× (mudar texto ou aceitar).
Verificado: `.\gradlew.bat assembleDebug test`; emulador: abre em Hoje, abas trocam, cores, `GenkiEval`, sem erros do app; rótulo inteiro em 1,0×/1,3×/1,5×. | Não verificado: aparelho físico, TalkBack, voz, câmera, alarmes. | Limitações: corte a partir de 1,8×.
Próxima tarefa e bloqueios: aceite manual do M0 pelo autor; decisões #2, #7, #12 antes do M1.
Links/commits: `d74a75b`; commit de renumeração; este merge.

ENTREGA — Justificativa formal, conciliação com a revisão do GPT e e-mail à Coordenação TADS — 26/10/2026 — Claude (Opus 5.5, chat do Projeto "Genki - TCC")
Partida: genki `5e0f668`; Drive: justificativa formal, revisão do GPT (26/10), plano de 26/10.
Realizado e arquivos alterados: justificativa formal criada e depois ajustada com as recomendações do GPT (dispensa a confirmar; 27/11 = primeira análise, não aprovação; adequação a formalizar, não substituição; executor e registro por método; intenção de submeter sem afirmar submissão; objetivo i não realizado ou parcial; sem tempos/taxas/SUS reais; título de Wharton et al. 1994). Nota de conciliação e espelho das DEC-023/024/025 no plano do Drive (o GPT não acessa o GitHub). E-mail final à coordenação entregue no chat. Aqui: `ESTADO`, `DECISOES`, `ENTREGAS`, `REGISTRO_IA`.
Decisões aceitas pelo autor nesta sessão: DEC-025; contatar só a coordenação do curso, não o CEP diretamente.
Sugestões pendentes: resposta factual à pergunta "por que não submeter em agosto" (preparar com o orientador).
Verificado: título de Wharton et al. em duas fontes secundárias divergentes (adotado o da Wikipedia, igual ao do GPT). | Não verificado: o livro de Wharton et al.; Kneale et al. (2017). | Limitações: GPT sem acesso ao repositório.
Próxima tarefa e bloqueios: autor envia o e-mail; resposta da coordenação decide formalização e submissão.
Links/commits: Drive https://docs.google.com/document/d/1lSLVGNz5Seh2XSHxtqZFvpY4IVQOTYlgfeJhZ_mfy5c/edit ; este commit.

ENTREGA — Handoff do app e protocolo de revisão Claude Code ↔ Codex — 26/10/2026 — Claude Code (Opus 5.5)
Partida: genki `main` em `19fcb3f`.
Realizado e arquivos alterados: `docs/HANDOFF_APP.md` (novo), `docs/revisoes/README.md` (novo), seção "Implementação e revisão" no `AGENTS.md`; skills `/revisar` e `/testar-emulador` reduzidas a apontar para as seções 6 e 5 do handoff (roteiro do autor movido para lá sem mudança de conteúdo, mais o passo de gravar a revisão e a regressão da seção 7); `DECISOES` (DEC-026), `00_INDICE`, `ESTADO`, `REGISTRO_IA`.
Decisões aceitas pelo autor nesta sessão: DEC-026 (26/10).
Sugestões pendentes: comando do Codex CLI copiado do protótipo (modelo pode precisar de ajuste).
Verificado: só documentação; nada executado. | Não verificado: se o Codex segue o handoff na prática; skills após a mudança. | Limitações: nenhuma.
Próxima tarefa e bloqueios: abrir o M0 no aparelho/emulador; primeira revisão do Codex (M0).
Links/commits: este commit.

ENTREGA — App Android: tema e navegação do M0 alinhados ao protótipo + divergências protótipo × ESPEC — 26/10/2026 — Claude Code (Opus 5.5)
Partida: genki `main` em `8864ce1`; genki-prototype `295212e` (`docs/HANDOFF.md` §2).
Realizado e arquivos alterados: `ui/theme/Color.kt` (paleta de `src/shared.tsx`), `Theme.kt` (todos os papéis do M3 com tons do protótipo, inclusive secondary/tertiary/surfaceContainer*), `Type.kt` (escala do protótipo: 48/26/22/20/18/17/16 sp, nenhum estilo abaixo de 16 sp), `ui/navigation/GenkiApp.kt` (barra inferior como `BottomNav`: fundo da superfície sem elevação, linha superior, indicador `#b7f0d4`, ativo `#1b6b4a` em negrito, inativo `#5e6761`, rótulo 16 sp, ícone de comprimidos do protótipo). Tarefa 2: `docs/DIVERGENCIAS_PROTOTIPO.md` (17 divergências de dados/regras, só levantamento); `00_INDICE`, `ESTADO`, `REGISTRO_IA`.
Decisões aceitas pelo autor nesta sessão: nenhuma DEC nova; tarefa pedida pelo autor.
Sugestões pendentes: as recomendações da tabela de divergências; rótulo da barra com `maxLines = 1` (reticências se não couber).
Verificado: `.\gradlew.bat assembleDebug test` (build ok; testes passam). | Não verificado: nada visto em aparelho/emulador (cores, altura da barra, rótulo "Medicamentos" com fonte ampliada — com escala ≥ ~1,3 pode ficar com reticências em tela de 360 dp); voz do protótipo para "por N dias". | Limitações: tema escuro ignorado; DemoClock, DemoIntro e painel de demonstração fora, como pedido.
Próxima tarefa e bloqueios: abrir no aparelho/emulador; autor decidir #2, #7 e #12 antes do M1 e #13, #14 e #16 antes do M2.
Links/commits: commit "chore: alinha tema e navegação ao protótipo (M0)"; este commit.

ENTREGA — App Android: estrutura base (M0) — 26/10/2026 — Claude Code (Opus 5.5)
Partida: genki `main` em `c1f2b7e` (template "Empty Activity" commitado pelo autor).
Realizado e arquivos alterados: dependências da ESPEC §1 em `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts` (Navigation Compose 2.9.8 + kotlinx-serialization 1.9.0, lifecycle 2.10.0 runtime/viewmodel-compose, Room 2.8.4 com KSP 2.3.12, coroutines-test 1.10.2; `buildConfig = true`); `GenkiApplication` (no manifest), `AppContainer` vazio, `GenkiEval`, `MainActivity`, `ui/navigation` (rotas tipadas e barra inferior), `ui/home`, `ui/medications`, `ui/settings` (provisórias), `ui/theme` (tokens do protótipo), `strings.xml`, `GenkiEvalTest` no lugar do `ExampleUnitTest`.
Decisões aceitas pelo autor nesta sessão: nenhuma DEC nova; autor aprovou o diff e autorizou o commit.
Sugestões pendentes: `material-icons-core` acrescentado para os ícones da barra (fora da §1; trocável por drawables); lifecycle unificado em 2.10.0 (template tinha 2.6.1); tema só claro, sem cor dinâmica.
Verificado: `.\gradlew.bat assembleDebug test` (build sem avisos; 2 testes passam); versões escolhidas pelo kotlin-stdlib exigido nos POMs (até o Kotlin 2.2.10 embutido no AGP 9.4.1). | Não verificado: app aberto em aparelho ou emulador (o emulador recusa iniciar por falta de espaço em disco); navegação entre abas; `GenkiEval` no Logcat; Room sem entidades. | Limitações: tema escuro do sistema ignorado; `ExampleInstrumentedTest` do template mantido e não executado.
Próxima tarefa e bloqueios: abrir no emulador/aparelho para fechar o aceite do M0; M1. Bloqueio: espaço em disco ou celular de teste.
Links/commits: `cec21ab`; este commit.

ENTREGA — Resposta do orientador e caminho C (CEP + personas) — 26/10/2026 — Claude (Opus 5.5, chat do Projeto "Genki - TCC")
Partida: genki `3fe845c`.
Realizado e arquivos alterados: registro da resposta do orientador; conferência do Ofício-Circular nº 17/2022/CONEP (TCC deve ser submetido) e do Regimento do CEP-IFRS (prazos 10 + 30 dias); justificativas propostas (amostra: Faulkner, 2003; embalagens; SUS); trilhas CEP e personas com cronograma integrado. Drive: duas seções novas no plano de 26/10 e aviso de precedência no topo. Aqui: `DECISOES` (DEC-024), `ESTADO`, `ENTREGAS`, `REGISTRO_IA`.
Decisões aceitas pelo autor nesta sessão: DEC-024 (26/10).
Sugestões pendentes: só embalagens na PoC; justificativas a revisar pelo autor.
Verificado: Ofício 17/2022 e Regimento CEP-IFRS (texto online); Faulkner (2003) no PubMed (autoria não exibida na página). | Não verificado: contato da coordenação/CEP; fontes de personas e percurso cognitivo. | Limitações: prazo real do CEP desconhecido.
Próxima tarefa e bloqueios: e-mail à coordenação/CEP (contato a confirmar pelo autor); resposta ao orientador; rascunhos do protocolo.
Links/commits: Drive https://docs.google.com/document/d/1LZW0Ylpso5zc3NihXLg0mBhXrhynfKIHsBysZtWoqrs/edit ; este commit.

ENTREGA — Plano de etapas, cronograma e e-mail ao orientador — 26/10/2026 — Claude (Opus 5.5, chat do Projeto "Genki - TCC")
Partida: genki `ad3b988`; genki-prototype `295212e`; Drive: proposta final, metodologia de 21/10.
Realizado e arquivos alterados: verificação do protótipo pelos registros (análise final, P7/P7b/P7c, retorno do autor); conferência da referência da SUS brasileira no texto online; plano com procedimentos por objetivo, redução de variáveis, cronograma, 16 tarefas e 8 perguntas ao orientador com propostas. Drive: novo doc "Genki — Plano de etapas e cronograma até a entrega" (Avaliação/) e aviso no topo do doc de metodologia de 21/10. Aqui: `ESTADO`, `DECISOES` (DEC-023), `ENTREGAS`, `REGISTRO_IA`, `00_INDICE`. Rascunho do e-mail entregue ao autor no chat.
Decisões aceitas pelo autor nesta sessão: DEC-023 (26/10).
Sugestões pendentes: respostas propostas ao orientador (dependem dele).
Verificado: proposta §1.2.2 e §2; SUS: referência, amostra, alfa e pontuação no texto do artigo (Redalyc/Distant Reader); Netlify responde com o título "Genki — protótipo". | Não verificado: build do protótipo (registro npm bloqueado na sandbox); versão publicada no Netlify; itens finais da SUS no PDF. | Limitações: plano feito sem resposta do orientador.
Próxima tarefa e bloqueios: autor envia o e-mail e inicia o M0; resposta do orientador até 28/10 decide cenário e participantes.
Links/commits: Drive https://docs.google.com/document/d/1LZW0Ylpso5zc3NihXLg0mBhXrhynfKIHsBysZtWoqrs/edit ; este commit.

ENTREGA — Protótipo: revisão geral, P5 "telas estreitas" e P6 "fala não entendida" + revisões do Codex — 22/10/2026 — Claude Code (Opus 5.5) e Codex
Partida: genki-prototype `5722918`; principal `62ba3a9`.
Realizado e arquivos alterados: revisão geral conjunta (`docs/REVISAO_GERAL_CODEX.md`, `docs/REVISAO_GERAL_CLAUDE.md`) e HANDOFF enxuto (histórico em `docs/HANDOFF_HISTORICO.md`). P5 (`a2b3607`, `index.css`, `DigitarMedicamento.tsx`): "Remover" do horário desce para baixo do campo quando não cabe; lista de revisão/Detalhes empilhada em telas ≤ 340 px; nome acessível da concentração igual ao rótulo visível. P6 (`d5da449`…`092cb0d`, `App.tsx`, `UsarVozMedicamento.tsx`): botão do painel de demonstração "Simular: próxima fala de correção não entendida" leva à tela "Não consegui entender" (Tentar novamente / Digitar resposta); a resposta digitada só aceita a frase sugerida pela ajuda ("o amlodipino é às 9 da manhã"); o interpretador das falas simuladas ficou igual ao anterior. Revisões: `docs/REVISAO_P5_CODEX.md`, `docs/REVISAO_P6_CODEX.md`.
Decisões aceitas pelo autor nesta sessão: fazer a revisão geral; P5 com os achados de telas estreitas; P6 com o controle de demonstração; push e registro.
Sugestões pendentes: nenhuma da revisão geral.
Verificado: `pnpm.cmd check:local`; §7 10/10 (Codex, várias vezes); 360×800 sem mudança, 320×640 e 277×615 sem corte; tela de erro da voz e resposta digitada no navegador; P6 com 69 testes unitários, 1.248 sondagens e 213 verificações no navegador pelo Codex. | Não verificado: Android, leitor de tela real, voz pt-BR real, câmera/OCR reais, usuários idosos. | Limitações: o interpretador da simulação é por palavras-chave e só serve às falas fixas da demonstração.
Próxima tarefa e bloqueios: Editar ainda rola (edição por seção exige decisão do autor); seção 9 do HANDOFF (confirmar cartão Agora × Próximo e painel de demonstração como DEC).
Links/commits: https://github.com/aryfacanha/genki-prototype ; `ef815f6`, `a2b3607`, `092cb0d`, `e865456`.

ENTREGA — Protótipo: P4g "revisão da voz mais curta" + revisão do Codex — 22/10/2026 — Claude Code (Opus 5.5) e Codex
Partida: genki-prototype `21c1662`; principal `8422b2a`.
Realizado e arquivos alterados: P4g (`0dca849`, `UsarVozMedicamento.tsx`, `GenkiFocused.css`) — na revisão final da voz a Duração mostra "Uso contínuo" sem "(sem data para parar)" (texto completo mantido nas perguntas, lista, Detalhes e revisão manual; dados iguais) e sai o título repetido "Resumo para conferência". Juntar Dias e Horários foi medido e descartado (ganho de 15 px). Revisão do Codex: `docs/REVISAO_P4G_CODEX.md`.
Decisões aceitas pelo autor nesta sessão: Duração curta e sem título na revisão da voz; parar antes de Editar por seção.
Verificado: `pnpm.cmd check:local`; Playwright (Claude e Codex) 360×800: revisão com 2 medicamentos 767 → 618/594 com o aviso de voz ausente e 573/573 sem ele; Corrigir tocando com "(antes: …)", Período, salvar, câmera → voz; Codex sem achados S1–S4.
Não verificado: voz pt real, Android, leitor de tela real, texto ampliado.
Próxima tarefa e bloqueios: Editar ainda rola (edição por seção exige decisão do autor); seção 9 do HANDOFF.
Links/commits: https://github.com/aryfacanha/genki-prototype ; `0dca849`.

ENTREGA — Protótipo: P4f "Desfazer na exclusão" + revisão do Codex — 22/10/2026 — Claude Code (Opus 5.5) e Codex
Partida: genki-prototype `0e549fd`; principal `11285ef`.
Realizado e arquivos alterados: P4f (`b347ad7`, `App.tsx`, `MedicamentosScreen.tsx`) — diálogo de confirmação mantido; o aviso "Medicamento excluído" ganha "Desfazer", que restaura o medicamento na mesma posição e as respostas de lembrete, com aviso "Medicamento restaurado" e foco no cartão; respostas de lembrete do excluído saem do estado (cadastro novo com id reutilizado não as herda). Revisão do Codex: `docs/REVISAO_P4F_CODEX.md`.
Decisões aceitas pelo autor nesta sessão: diálogo + Desfazer na exclusão; próximo: revisão da voz mais curta.
Verificado: `pnpm.cmd check:local`; Playwright (Claude e Codex) 360×800: desfazer (posição, foco, "Visto às 12:00"), expirar, cancelar, id reutilizado, Desfazer ausente em outros avisos, teclado; Codex sem achados S1–S4.
Não verificado: Android, leitor de tela real, texto ampliado; §7 completa não repetida.
Próxima tarefa e bloqueios: revisão da voz com 2+ medicamentos mais curta (Claude Code implementa, Codex revisa); Editar ainda rola.
Links/commits: https://github.com/aryfacanha/genki-prototype ; `b347ad7`.

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
