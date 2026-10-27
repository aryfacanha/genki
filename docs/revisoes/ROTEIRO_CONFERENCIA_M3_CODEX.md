# Roteiro de conferência para o Codex — fechamento do M3

Preparado em 27/10/2026 por Claude Code (Opus 5.5), a pedido do autor. **Só conferência**, não revisão ampla:
confirmar os dois achados abertos em `REVISAO_CORRECAO3_M3_M4_CODEX.md` e o texto do cartão, e dizer se o M3 pode ser
dado como revisado. Achados novos sem relação com estas linhas vão para limitações.

## Alvo

`main`, commit **`0cef3f1`**:

| Item | Mudança | Onde |
| --- | --- | --- |
| RV-M3-02 (resto) | Na lista de dias, "na" seguido de "verdade" encerra a lista (é correção, não conector) | `VoiceParser.days`, laço da lista |
| RV-M3E-01 | Durante pergunta comum, "o nome é X" troca o nome do perguntado; nome solto continua ignorado | `VoiceParser.name`, retorno antecipado com `!renameNext` |
| Limitação do grupo | Cartão conta "N lembretes" (antes "N medicamentos") | `HomeScreen.AlertGroup` |

## Execução

1. `git pull`; `.\gradlew.bat assembleDebug test` — esperado: **112 testes, 0 falhas** (+2 em `VoiceParserTest`:
   `correctionAfterDayListWithAnotherMedication`, `nameIsDuringAnyQuestion`).
2. Reprodutores locais (`build/Temporary*.kt` + `VoiceParserBaseline.kt` + `VoiceParserBeforeSecond.kt`): copiar, rodar
   `--tests "br.edu.ifrs.genki.voice.Temporary*"`, remover. O Claude Code obteve **43/43** (7 + 14 + 15 + 7).
3. Opcional: "o nome é atenolol" na pergunta de dias pela tela (texto injetado, `ROTEIRO_REVISAO_M3_M4_CODEX.md` §4.2).

## Relatório

`docs/revisoes/CONFERENCIA_M3_CODEX.md` (curto): situação dos itens e veredito sobre o M3. Atualizar `HANDOFF_APP.md`
(§2/§3 e Diário), `ESTADO.md`, `ENTREGAS.md`, `REGISTRO_IA.md`. Não alterar código; não fazer push.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_CONFERENCIA_M3_CODEX.md`.
