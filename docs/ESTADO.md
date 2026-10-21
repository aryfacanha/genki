# Genki — estado atual

Atualizado em 21/10/2026 por Claude (Opus 5.5).

## Situação

| Frente | Estado | Evidência |
| --- | --- | --- |
| Proposta | Aprovada; marco acadêmico | Drive (DEC-020) |
| Requisitos | RF01–RF25, RNF01–RNF10 na planilha. DEC-002 a 005 aceitas, **não aplicadas** (planilha alterada pela última vez em 04/09) | Drive |
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
| Modelo e versão Android do celular de teste | Autor | Verificação no aparelho (M0) |
| Reunião com o orientador: procedimento ético, cenário A/B/C, participantes, prescrições | Autor | Objetivo i; MET-2 |
| Autorizar aplicação de DEC-002 a 005 na planilha | Autor | Coerência requisitos ↔ código |
| Padrão de autoria dos commits | Autor | Nada urgente |
| Subir monografia (ou modelo IFRS) e artigos ao Drive | Autor | Redação |
| Conferir lista de heurísticas (Galavi et al., 2024) e itens/pontuação da SUS (Lourenço et al., 2022) no texto dos artigos | Qualquer assistente, com os PDFs | MET-3, MET-6 |

## Próximo passo

M0: criar o projeto Android no Android Studio ("Empty Activity", Kotlin, Compose, minSdk 26), conforme
`docs/ESPEC_ARQUITETURA.md` §1–2 e §9, e commitar neste repositório. Em paralelo: reunião com o orientador e estrutura da monografia.
