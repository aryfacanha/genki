# Genki — estado atual

Atualizado em 26/10/2026, por Claude (Opus 5.5, chat do Projeto) — plano de etapas e cronograma até a entrega (DEC-023); antes: 22/10, Claude Code — revisão geral do protótipo, P5, P6.

## Situação

| Frente | Estado | Evidência |
| --- | --- | --- |
| Proposta | Aprovada; marco acadêmico | Drive (DEC-020) |
| Requisitos | RF01–RF25, RNF01–RNF11 na planilha; DEC-002 a 005 aplicadas em 21/10 | Drive |
| Protótipo | **Genki(22) com pacotes locais P1–P6.** P2/P3 (Codex) revisados pelo Claude Code; achados VC-01/02/03 e RV-01 corrigidos. P4a: opções de dias empilhadas, TTS só em voz portuguesa. P4b (meta "telas sem rolar"): pílula "Mais abaixo" no indicador de rolagem, cadastro manual em 3 etapas com "Alterar" na revisão, revisão final em cartões simplificados (a manual cabe sem rolar). P4c: Hoje com a lista recolhida em "Ver todos os horários de hoje" (a Home cabe sem rolar), revisado pelo Codex no navegador; achado S3 de acessibilidade corrigido. P4d: Medicamentos e Detalhes cabem sem rolar (cartões sem ícone decorativo; Detalhes com a lista em duas colunas da revisão), revisado pelo Codex no navegador; o S3 preexistente (aviso sobre cartão em lista longa) foi corrigido em 22/10 com o aviso em faixa própria (revisado pelo Codex). P4e: a lista Hoje mostra "Adiado para HH:MM" quando o alerta adiado volta, revisado pelo Codex (sem S1–S3). P4f: "Desfazer" no aviso de exclusão (diálogo mantido) restaura posição e respostas de lembrete, revisado pelo Codex sem achados. P4g: revisão final da voz mais curta (Duração "Uso contínuo" curta, sem título repetido; 2 medicamentos cabem com voz pt), revisado pelo Codex sem achados. Revisão geral em 22/10 (Codex: regressão §7 10/10, combinações entre pacotes e consistência; Claude: telas de 320 px e com ampliação de 130 %): sem S1/S2. P5 "telas estreitas" (botão Remover e lista de revisão sem corte em telas estreitas/ampliadas; nome acessível da concentração) e P6 "fala não entendida" (controle de demonstração mostra a tela de erro da voz; a resposta digitada só aceita a frase da ajuda, após 7 achados S2 do Codex na tentativa de ampliar o interpretador) aprovados pelo Codex. Build e regressão §7 9/9 no Vite/Playwright em 21/10. Confirmar como referência final de UX (DEC-022) ainda depende do autor | github.com/aryfacanha/genki-prototype (`docs/HANDOFF.md`; `origin/main` em `e865456`; tag da base `prototype-figma-final`); ZIPs em `D:\TCC\Prototipo\figma-versions\` |
| Protótipo — envio ao orientador | P7/P7b (revisados pelo Codex) e P7c (só Claude Code) em `295212e` do genki-prototype; análise final com veredito "pode enviar após P7". Publicado no Netlify (https://joyful-biscuit-677f74.netlify.app) — responde publicamente; **versão publicada não conferida**. Novo fluxo de voz V1–V6 do retorno do autor: fora do escopo até o congelamento (DEC-023) | genki-prototype `docs/HANDOFF.md`, `docs/FEEDBACK_AUTOR_2026-10-26.md` |
| **Plano até a entrega** | **Aceito pelo autor em 26/10 (DEC-023)**: sequência de 16 tarefas, cronograma, redução de variáveis na sessão com participantes, perguntas ao orientador com propostas | Drive, Avaliação/Genki — Plano de etapas e cronograma até a entrega |
| Arquitetura | **Definida** — especificação v2.0 aceita (DEC-008 a 017, 021) | `docs/ESPEC_ARQUITETURA.md` |
| Metodologia de avaliação | **Definida** (DEC-018); pontos dependentes do orientador em aberto | Drive, Avaliação/ |
| Código Android | Nenhum | Este repositório (só documentação) |
| Testes | Nenhum | — |
| Monografia pós-proposta | Não está no Drive | — |
| Autorização ética | **Desconhecida — caminho crítico** para o objetivo i (SUS) | — |

## Prazos

Protótipo em 21/10/2026 (Claude Code + Codex): P4d — Medicamentos 559/629 → 575/575 e Detalhes 720/869 → 720/720 (360×800), confirmados pelo Codex no navegador; `pnpm.cmd check:local` e §7 passaram; um S3 preexistente registrado (RV-P4d-01). Android/leitor de tela/texto ampliado não verificados.

Protótipo em 21/10/2026 (Claude Code + Codex): P4c "Hoje: Agenda clara" (lista recolhida, Home 615/615 sem rolar) revisado pelo Codex no navegador; um S3 (`aria-controls` sem alvo com a lista recolhida) corrigido e retestado. `pnpm.cmd check:local` passou. Android/leitor de tela/texto ampliado não verificados.

Verificação complementar do protótipo em 21/10/2026 pelo Codex: revisão independente de P4b-1/2/3 no navegador concluída, sem achados novos; `pnpm.cmd check:local` e regressão §7 9/9 passaram. Conferidos cadastro em etapas, revisão/retornos, indicador de rolagem e Período da voz com correção e salvamento. Relatório: `docs/REVISAO_P4B_CODEX.md` no genki-prototype. Nenhuma alteração no produto; Android/leitor de tela/voz pt-BR real continuam não verificados.

| Data | Marco (DEC-023, 26/10) |
| --- | --- |
| 26/10 | Conferir Netlify; e-mail ao orientador (retorno até 28/10); M0 com registro `GenkiEval`; definir celular de teste |
| 27/10 | Cadastro manual com `DraftValidator` |
| 28/10 | Consulta, edição, exclusão e lembretes; registrar resposta do orientador como decisão |
| 29–31/10 | Voz (fluxo do protótipo; RF14 simplificado se faltar tempo) + preparo dos instrumentos (corpus, embalagens, SUS conferida, cartões, planilhas; TCLE só no cenário A) |
| 01/11 | PoC de imagem mínima (nome e concentração) |
| **02/11** | **App congelado** (tag) |
| 02–03/11 | Revisão heurística (MET-3) |
| 03–05/11 | Verificação técnica: voz 24 ensaios, imagem 20 ensaios |
| 06–11/11 | Tarefas + SUS, só com autorização documentada (cenário A) |
| 09–15/11 | Análise e fechamento da monografia |
| **16/11** | **Entrega da monografia em PDF** |

## Pendências e bloqueios

| Pendência | Responsável | Bloqueia |
| --- | --- | --- |
| Modelo e versão Android do celular de teste (ainda não definido) | Autor | Verificação no aparelho a partir do M1; o M0 pode usar emulador |
| Confirmar o Genki(22) como referência final de UX; listar mudanças em relação à v1 que afetem dados ou regras; decidir se o export vai também para o Drive | Autor | Telas do M1 em diante |
| Protótipo: telas que ainda rolam (Editar, telas e revisão da voz com 2+ medicamentos); Editar ainda rola (edição por seção não iniciada; decisão do autor); estudo exploratório MyTherapy do Codex aguardando avaliação; sem teste com leitor de tela real/Android nem voz pt-BR real | Autor decide; Claude Code implementa, Codex revisa | Nada urgente para o M0 |
| E-mail ao orientador (rascunho pronto em 26/10; envio pelo autor): 8 perguntas do plano de 26/10 com propostas (ético, cenário — proposta B, participantes, heurística só pelo autor, só embalagens, âncoras da SUS, prazo de mudanças no protótipo, declaração de IA) | Autor | Objetivo i; MET-2. Quanto mais tarde, menor a chance do cenário A |
| Padrão de autoria dos commits | Autor | Nada urgente |
| Subir monografia (ou modelo IFRS) e artigos ao Drive | Autor | Redação |
| Conferir lista de heurísticas (Galavi et al., 2024) no artigo; SUS (Lourenço; Carmona; Lopes, 2022, Aquichan 22(2):e2228): pontuação e amostra conferidas em 26/10 no texto online; **itens finais (Tabela 1 é pré-final; itens 1, 4 e 7) e forma ABNT dos nomes ainda a conferir no PDF**; âncoras em português a definir | Qualquer assistente, com os PDFs | MET-3, MET-6 |

## Próximo passo

Autor: enviar o e-mail ao orientador e criar o projeto Android (M0) hoje, conforme `docs/ESPEC_ARQUITETURA.md` §1–2 e §9,
já prevendo o registro `GenkiEval` (DEC-015). Sequência completa no plano de 26/10 (Drive, Avaliação/).
