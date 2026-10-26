# Genki — estado atual

Atualizado em 26/10/2026, por Claude Code (Opus 5.5) — M0 aberto no emulador, correção do rótulo da barra com fonte ampliada e merge com o chat do Projeto (decisão do handoff renumerada para DEC-026); antes: 26/10, Claude (chat do Projeto) — protótipo aprovado como referência de UX (DEC-025), justificativa das personas e e-mail à coordenação; antes: 26/10, Claude Code — handoff do app e protocolo de revisão (DEC-026); antes: 26/10, Claude Code — tema e barra inferior do M0 alinhados ao protótipo e divergências protótipo × ESPEC; antes: 26/10, Claude Code — M0 do app Android; antes: 26/10, Claude (chat do Projeto) — plano de etapas e cronograma (DEC-023).

## Situação

| Frente | Estado | Evidência |
| --- | --- | --- |
| Proposta | Aprovada; marco acadêmico | Drive (DEC-020) |
| Requisitos | RF01–RF25, RNF01–RNF11 na planilha; DEC-002 a 005 aplicadas em 21/10 | Drive |
| Protótipo | **Genki(22) com pacotes locais P1–P6.** P2/P3 (Codex) revisados pelo Claude Code; achados VC-01/02/03 e RV-01 corrigidos. P4a: opções de dias empilhadas, TTS só em voz portuguesa. P4b (meta "telas sem rolar"): pílula "Mais abaixo" no indicador de rolagem, cadastro manual em 3 etapas com "Alterar" na revisão, revisão final em cartões simplificados (a manual cabe sem rolar). P4c: Hoje com a lista recolhida em "Ver todos os horários de hoje" (a Home cabe sem rolar), revisado pelo Codex no navegador; achado S3 de acessibilidade corrigido. P4d: Medicamentos e Detalhes cabem sem rolar (cartões sem ícone decorativo; Detalhes com a lista em duas colunas da revisão), revisado pelo Codex no navegador; o S3 preexistente (aviso sobre cartão em lista longa) foi corrigido em 22/10 com o aviso em faixa própria (revisado pelo Codex). P4e: a lista Hoje mostra "Adiado para HH:MM" quando o alerta adiado volta, revisado pelo Codex (sem S1–S3). P4f: "Desfazer" no aviso de exclusão (diálogo mantido) restaura posição e respostas de lembrete, revisado pelo Codex sem achados. P4g: revisão final da voz mais curta (Duração "Uso contínuo" curta, sem título repetido; 2 medicamentos cabem com voz pt), revisado pelo Codex sem achados. Revisão geral em 22/10 (Codex: regressão §7 10/10, combinações entre pacotes e consistência; Claude: telas de 320 px e com ampliação de 130 %): sem S1/S2. P5 "telas estreitas" (botão Remover e lista de revisão sem corte em telas estreitas/ampliadas; nome acessível da concentração) e P6 "fala não entendida" (controle de demonstração mostra a tela de erro da voz; a resposta digitada só aceita a frase da ajuda, após 7 achados S2 do Codex na tentativa de ampliar o interpretador) aprovados pelo Codex. Build e regressão §7 9/9 no Vite/Playwright em 21/10. Confirmar como referência final de UX (DEC-022) ainda depende do autor | github.com/aryfacanha/genki-prototype (`docs/HANDOFF.md`; `origin/main` em `e865456`; tag da base `prototype-figma-final`); ZIPs em `D:\TCC\Prototipo\figma-versions\` |
| Protótipo — envio ao orientador | **Aprovado pelo orientador em 26/10 ("muito bom"); referência final de UX (DEC-025).**  P7/P7b (revisados pelo Codex) e P7c (só Claude Code) em `295212e` do genki-prototype; análise final com veredito "pode enviar após P7". Publicado no Netlify (https://joyful-biscuit-677f74.netlify.app) — responde publicamente; **versão publicada não conferida**. Novo fluxo de voz V1–V6 do retorno do autor: fora do escopo até o congelamento (DEC-023) | genki-prototype `docs/HANDOFF.md`, `docs/FEEDBACK_AUTOR_2026-10-26.md` |
| **Plano até a entrega** | **Aceito pelo autor em 26/10 (DEC-023; objetivo i revisto pela DEC-024: CEP + personas, meta de 10 participantes)**: sequência de 16 tarefas, cronograma, redução de variáveis na sessão com participantes, perguntas ao orientador com propostas | Drive, Avaliação/Genki — Plano de etapas e cronograma até a entrega |
| Arquitetura | **Definida** — especificação v2.0 aceita (DEC-008 a 017, 021) | `docs/ESPEC_ARQUITETURA.md` |
| Metodologia de avaliação | **Definida** (DEC-018); pontos dependentes do orientador em aberto | Drive, Avaliação/ |
| Código Android | **M0 implementado e compilado em 26/10; aberto no emulador Pixel_6_2 (API 35) em 26/10, regressão M0 ok; aparelho físico não.** Template "Empty Activity" (autor, `c1f2b7e`) + estrutura base (Claude Code, `cec21ab`): dependências da ESPEC §1, pacotes da §2, `GenkiApplication`/`AppContainer` vazio, barra inferior Hoje · Medicamentos · Ajustes com telas provisórias, tema com os tokens do protótipo (só claro), `GenkiEval` só no debug. **Alinhado ao protótipo `295212e` em 26/10 (só visual):** paleta completa de `shared.tsx` nos papéis do M3 (sem roxo padrão), tipografia sem estilo abaixo de 16 sp, barra inferior com linha superior, indicador verde-claro, rótulo 16 sp (negrito no ativo) e ícone de comprimidos do protótipo Versões (DEC-011): AGP 9.4.1 (Kotlin 2.2.10 embutido), Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26 | `app/`, `gradle/libs.versions.toml` |
| Handoff do app | `docs/HANDOFF_APP.md` (DEC-026): estado por marco, verificado/não verificado, checagem no emulador, roteiro de revisão, Diário; Claude Code implementa, Codex revisa | `docs/HANDOFF_APP.md`, `AGENTS.md` |
| Testes | `GenkiEvalTest` (2 unitários) passa; `.\gradlew.bat assembleDebug test` ok em 26/10 | `app/src/test/` |
| Monografia pós-proposta | Não está no Drive | — |
| Autorização ética | **Desconhecida — caminho crítico** para o objetivo i (SUS) | — |

## Prazos

Protótipo em 21/10/2026 (Claude Code + Codex): P4d — Medicamentos 559/629 → 575/575 e Detalhes 720/869 → 720/720 (360×800), confirmados pelo Codex no navegador; `pnpm.cmd check:local` e §7 passaram; um S3 preexistente registrado (RV-P4d-01). Android/leitor de tela/texto ampliado não verificados.

Protótipo em 21/10/2026 (Claude Code + Codex): P4c "Hoje: Agenda clara" (lista recolhida, Home 615/615 sem rolar) revisado pelo Codex no navegador; um S3 (`aria-controls` sem alvo com a lista recolhida) corrigido e retestado. `pnpm.cmd check:local` passou. Android/leitor de tela/texto ampliado não verificados.

Verificação complementar do protótipo em 21/10/2026 pelo Codex: revisão independente de P4b-1/2/3 no navegador concluída, sem achados novos; `pnpm.cmd check:local` e regressão §7 9/9 passaram. Conferidos cadastro em etapas, revisão/retornos, indicador de rolagem e Período da voz com correção e salvamento. Relatório: `docs/REVISAO_P4B_CODEX.md` no genki-prototype. Nenhuma alteração no produto; Android/leitor de tela/voz pt-BR real continuam não verificados.

| Data | Marco (DEC-023 + DEC-024, 26/10) |
| --- | --- |
| 26–28/10 | App M0 e M1 (CRUD manual com `DraftValidator`); e-mails à coordenação/CEP e resposta ao orientador; cadastro na Plataforma Brasil |
| 27–29/10 | Rascunhos do protocolo CEP (Claude): projeto, TCLE, roteiro da sessão, cronograma, orçamento |
| 29–30/10 | App M2 (lembretes) e início da voz; protocolo revisado, folha de rosto assinada e **submissão ao CEP** |
| 31/10–01/11 | Voz e PoC de imagem; 2 personas escritas a partir da literatura; corpus, embalagens, SUS conferida, cartões |
| **02/11** | **App congelado** (tag) |
| 02–03/11 | Revisão heurística (MET-3) |
| 03–05/11 | Verificação técnica: voz 24 ensaios, imagem 20 ensaios |
| 05–06/11 | Avaliação com personas (T1–T4 no app congelado) |
| 06–15/11 | Redação; sessões com até 10 participantes **só se aprovadas** (10–13/11 se parecer até ~09/11); nenhum contato com participantes antes do parecer |
| **16/11** | **Entrega da monografia em PDF** |

## Pendências e bloqueios

| Pendência | Responsável | Bloqueia |
| --- | --- | --- |
| Modelo e versão Android do celular de teste (ainda não definido) | Autor | Verificação no aparelho a partir do M1; o M0 pode usar emulador |
| Decidir se o export do protótipo vai também para o Drive. **Divergências de dados/regras listadas em 26/10 (`docs/DIVERGENCIAS_PROTOTIPO.md`, 17 itens; até decidir, vale a ESPEC — DEC-022)**; as que pedem decisão antes do M1/M2: horários repetidos (#2), rótulo de "Ainda não sei" (#7), "Desfazer" na exclusão (#12), "Visto às"/"Adiado para" na Hoje (#13), escolha do adiamento (#14), seletor de intervalo em Ajustes (#16) | Autor | Telas do M1 em diante |
| Protótipo: telas que ainda rolam (Editar, telas e revisão da voz com 2+ medicamentos); Editar ainda rola (edição por seção não iniciada; decisão do autor); estudo exploratório MyTherapy do Codex aguardando avaliação; sem teste com leitor de tela real/Android nem voz pt-BR real | Autor decide; Claude Code implementa, Codex revisa | Nada urgente para o M0 |
| Resposta do orientador recebida em 26/10 (registrada no plano de 26/10, Drive, e em `DECISOES.md` › Pendentes): CEP obrigatório; personas?; amostra de 5 pequena; heurística solo ok; embalagens a critério do autor; justificar a SUS; protótipo em análise; declaração de IA completa. Caminho decidido (DEC-024). CEP-IFRS: cepesquisa@ifrs.edu.br; Regimento vigente de 2024 (prazos 10 + 30 dias, arts. 12 V e 27); a página do CEP orienta coleta 45–60 dias após a submissão. **Resposta ao orientador redigida em 26/10 (envio pelo autor), perguntando se ainda vale submeter**. Calendário do CEP-IFRS: reuniões 30/10 (prazo 23/10, vencido), 27/11 (prazo 20/11) e 11/12 → primeira análise possível depois da entrega. Checklist de submissão e estrutura de justificativa das personas: Drive, Avaliação/Genki — Checklist do CEP e justificativa das personas (sugestão pendente) | Autor | Objetivo i; protocolo CEP |
| **GPT sem acesso ao GitHub** (busca do repositório retornou 404 em 26/10; repositório privado). Até liberar, decisões vigentes espelhadas no plano de 26/10 no Drive | Autor (conectar o GitHub no ChatGPT ou dar acesso) | Passagem de contexto para o GPT |
| Aceite do M0 "abre no aparelho" (ESPEC §9): aberto no emulador Pixel_6_2 (API 35) em 26/10 por Claude Code — abas, tema e `GenkiEval` conferidos (`docs/HANDOFF_APP.md` §3); falta a verificação manual do autor em aparelho; rótulo "Medicamentos" corta a partir de fonte 1,8× | Autor | Fechar o aceite do M0 |
| Padrão de autoria dos commits | Autor | Nada urgente |
| Subir monografia (ou modelo IFRS) e artigos ao Drive | Autor | Redação |
| Conferir lista de heurísticas (Galavi et al., 2024) no artigo; SUS (Lourenço; Carmona; Lopes, 2022, Aquichan 22(2):e2228): pontuação e amostra conferidas em 26/10 no texto online; **itens finais (Tabela 1 é pré-final; itens 1, 4 e 7) e forma ABNT dos nomes ainda a conferir no PDF**; âncoras em português a definir | Qualquer assistente, com os PDFs | MET-3, MET-6 |

## Próximo passo

Duas frentes em paralelo (DEC-023/024):
- **App:** autor conferir o M0 no aparelho para fechar o aceite (emulador já ok); autor decidir os itens de `docs/DIVERGENCIAS_PROTOTIPO.md` que afetam o M1 (#2, #7, #12); depois M1 (CRUD manual com `DraftValidator`) em 27–28/10.
- **Metodologia (chat do Projeto no Claude):** e-mail à coordenação/CEP, resposta ao orientador, rascunhos do protocolo CEP.
Plano completo: Drive, Avaliação/Genki — Plano de etapas e cronograma até a entrega.
