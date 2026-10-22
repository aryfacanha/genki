# Genki — estado atual

Atualizado em 22/10/2026, por Claude Code (Opus 5.5) — RV-P4d-01, P4e, P4f, P4g e revisões do Codex.

## Situação

| Frente | Estado | Evidência |
| --- | --- | --- |
| Proposta | Aprovada; marco acadêmico | Drive (DEC-020) |
| Requisitos | RF01–RF25, RNF01–RNF11 na planilha; DEC-002 a 005 aplicadas em 21/10 | Drive |
| Protótipo | **Genki(22) com pacotes locais P1–P4b-3.** P2/P3 (Codex) revisados pelo Claude Code; achados VC-01/02/03 e RV-01 corrigidos. P4a: opções de dias empilhadas, TTS só em voz portuguesa. P4b (meta "telas sem rolar"): pílula "Mais abaixo" no indicador de rolagem, cadastro manual em 3 etapas com "Alterar" na revisão, revisão final em cartões simplificados (a manual cabe sem rolar). P4c: Hoje com a lista recolhida em "Ver todos os horários de hoje" (a Home cabe sem rolar), revisado pelo Codex no navegador; achado S3 de acessibilidade corrigido. P4d: Medicamentos e Detalhes cabem sem rolar (cartões sem ícone decorativo; Detalhes com a lista em duas colunas da revisão), revisado pelo Codex no navegador; o S3 preexistente (aviso sobre cartão em lista longa) foi corrigido em 22/10 com o aviso em faixa própria (revisado pelo Codex). P4e: a lista Hoje mostra "Adiado para HH:MM" quando o alerta adiado volta, revisado pelo Codex (sem S1–S3). P4f: "Desfazer" no aviso de exclusão (diálogo mantido) restaura posição e respostas de lembrete, revisado pelo Codex sem achados. P4g: revisão final da voz mais curta (Duração "Uso contínuo" curta, sem título repetido; 2 medicamentos cabem com voz pt), revisado pelo Codex sem achados. Build e regressão §7 9/9 no Vite/Playwright em 21/10. Confirmar como referência final de UX (DEC-022) ainda depende do autor | github.com/aryfacanha/genki-prototype (`docs/HANDOFF.md`; `origin/main` em `5f999ee`; tag da base `prototype-figma-final`); ZIPs em `D:\TCC\Prototipo\figma-versions\` |
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

| Data | Marco |
| --- | --- |
| até 26/10 | Reunião com o orientador (pauta no Drive) |
| 21–22/10 | M0 — projeto Android base |
| 23–24/10 | M1 — cadastro, consulta, edição, exclusão |
| 25–26/10 | M2 — lembretes |
| 27–30/10 | M3 — voz |
| 31/10–01/11 | M4 — PoC de imagem |
| **02/11** | **App congelado** |
| 03–08/11 | Verificação técnica, revisão heurística, testes com participantes (só com autorização) |
| 09–15/11 | Análise e fechamento da monografia |
| **16/11** | **Entrega da monografia em PDF** |

## Pendências e bloqueios

| Pendência | Responsável | Bloqueia |
| --- | --- | --- |
| Modelo e versão Android do celular de teste (ainda não definido) | Autor | Verificação no aparelho a partir do M1; o M0 pode usar emulador |
| Confirmar o Genki(22) como referência final de UX; listar mudanças em relação à v1 que afetem dados ou regras; decidir se o export vai também para o Drive | Autor | Telas do M1 em diante |
| Protótipo: telas que ainda rolam (Editar, telas e revisão da voz com 2+ medicamentos); Editar ainda rola (edição por seção não iniciada; decisão do autor); estudo exploratório MyTherapy do Codex aguardando avaliação; respostas digitadas da voz não alcançáveis na simulação; sem teste com leitor de tela real/Android nem voz pt-BR real | Autor decide; Claude Code implementa, Codex revisa | Nada urgente para o M0 |
| E-mail e reunião com o orientador (ainda não enviado): procedimento ético, cenário A/B/C, participantes, prescrições | Autor | Objetivo i; MET-2. Quanto mais tarde, menor a chance do cenário A |
| Padrão de autoria dos commits | Autor | Nada urgente |
| Subir monografia (ou modelo IFRS) e artigos ao Drive | Autor | Redação |
| Conferir lista de heurísticas (Galavi et al., 2024) e itens/pontuação da SUS (Lourenço et al., 2022) no texto dos artigos | Qualquer assistente, com os PDFs | MET-3, MET-6 |

## Próximo passo

M0: criar o projeto Android no Android Studio ("Empty Activity", Kotlin, Compose, minSdk 26), conforme
`docs/ESPEC_ARQUITETURA.md` §1–2 e §9, e commitar neste repositório. Em paralelo: reunião com o orientador e estrutura da monografia.
