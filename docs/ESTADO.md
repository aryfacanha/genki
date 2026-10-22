# Genki — estado atual

Atualizado em 21/10/2026, por Claude Code (Opus 5.5).

## Situação

| Frente | Estado | Evidência |
| --- | --- | --- |
| Proposta | Aprovada; marco acadêmico | Drive (DEC-020) |
| Requisitos | RF01–RF25, RNF01–RNF11 na planilha; DEC-002 a 005 aplicadas em 21/10 | Drive |
| Protótipo | **Genki(22) com pacotes locais P1–P4b-3.** P2/P3 (Codex) revisados pelo Claude Code; achados VC-01/02/03 e RV-01 corrigidos. P4a: opções de dias empilhadas, TTS só em voz portuguesa. P4b (meta "telas sem rolar"): pílula "Mais abaixo" no indicador de rolagem, cadastro manual em 3 etapas com "Alterar" na revisão, revisão final em cartões simplificados (a manual cabe sem rolar). Build e regressão §7 9/9 no Vite/Playwright em 21/10. Confirmar como referência final de UX (DEC-022) ainda depende do autor | github.com/aryfacanha/genki-prototype (`docs/HANDOFF.md`; `origin/main` em `36e8fe5`; tag da base `prototype-figma-final`); ZIPs em `D:\TCC\Prototipo\figma-versions\` |
| Arquitetura | **Definida** — especificação v2.0 aceita (DEC-008 a 017, 021) | `docs/ESPEC_ARQUITETURA.md` |
| Metodologia de avaliação | **Definida** (DEC-018); pontos dependentes do orientador em aberto | Drive, Avaliação/ |
| Código Android | Nenhum | Este repositório (só documentação) |
| Testes | Nenhum | — |
| Monografia pós-proposta | Não está no Drive | — |
| Autorização ética | **Desconhecida — caminho crítico** para o objetivo i (SUS) | — |

## Prazos

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
| Protótipo: telas que ainda rolam (Hoje, Medicamentos, Detalhes, Editar, revisão da voz com 2+ medicamentos) — próximos caminhos exigem decisão; estudo exploratório MyTherapy do Codex aguardando avaliação; respostas digitadas da voz não alcançáveis na simulação; sem teste com leitor de tela real/Android nem voz pt-BR real | Autor decide; Claude Code implementa, Codex revisa | Nada urgente para o M0 |
| E-mail e reunião com o orientador (ainda não enviado): procedimento ético, cenário A/B/C, participantes, prescrições | Autor | Objetivo i; MET-2. Quanto mais tarde, menor a chance do cenário A |
| Padrão de autoria dos commits | Autor | Nada urgente |
| Subir monografia (ou modelo IFRS) e artigos ao Drive | Autor | Redação |
| Conferir lista de heurísticas (Galavi et al., 2024) e itens/pontuação da SUS (Lourenço et al., 2022) no texto dos artigos | Qualquer assistente, com os PDFs | MET-3, MET-6 |

## Próximo passo

M0: criar o projeto Android no Android Studio ("Empty Activity", Kotlin, Compose, minSdk 26), conforme
`docs/ESPEC_ARQUITETURA.md` §1–2 e §9, e commitar neste repositório. Em paralelo: reunião com o orientador e estrutura da monografia.
