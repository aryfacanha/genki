# Roteiro de conferência para o Codex — reparo RV-DVC-01…05

Preparado em 28/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Conferência **pontual** dos cinco achados de
`docs/revisoes/REVISAO_DOSE_VOZ_CONTRASTE_CODEX.md`, sem novo ciclo amplo. Siga o `HANDOFF_APP.md` §6; quem revisa
não altera código.

## Alvo

`main`, commit **`a57c591`** (`7ffddeb..a57c591`). Código em `ui/form/DoseUnit.kt`, `ui/form/FormScreen.kt`,
`voice/VoiceQuestions.kt`, `voice/ui/VoiceScreen.kt`; testes em `DoseUnitTest` (`stepperNeverLosesWhatWasTyped`, novo;
`buildsSingularAndPlural` ajustado) e `VoiceParserTest` (`timesFromClockAreUnderstood`).

| Achado | Reparo | Onde |
| --- | --- | --- |
| RV-DVC-01 | `canPlus`: acima de 100 o + fica apagado e a quantidade não muda | `DoseUnit.plus/canPlus`; `AmountStepper(canPlus)` |
| RV-DVC-02 | `amountOf` em milésimos (0,25 + 0,5 = "0,75"); − não chega a zero (`canMinus` = valor − passo > 0) | `DoseUnit.amountOf/minus/canMinus` |
| RV-DVC-03 | `VoiceQuestion.everyHours` só na pergunta do primeiro horário; `TimesPicker(everyHours)` aceita um horário, mostra "Horários do dia: …" (`Frequency.fill`) e troca "Adicionar outro" por "Trocar horário" | `VoiceQuestions.ask`, `VoiceScreen.TimesPicker` |
| RV-DVC-04 | `value` reconhece "1/2" e "½" como 0,5; consequência: "1/2" digitado no contador sai "meio comprimido" | `DoseUnit.value`, `HALVES` |
| RV-DVC-05 | `LaunchedEffect(state.correcting)` fecha o relógio; o ramo exige `!state.correcting` | `VoiceScreen.Asking` |

## Execução

1. `git pull`; registrar a partida. `.\gradlew.bat assembleDebug test`: esperado **134 testes, 0 falhas**.
2. Reexecutar os quatro reprodutores da revisão (`build/TemporaryDoseVoiceContrastReviewTest.kt`, se ainda existir)
   contra `a57c591`, só durante a execução, e removê-los depois. Esperado: passam.
3. Emulador (§5), sem mexer em código:
   - Gota → 120 → + (continua 120, + apagado) → − (119).
   - ml → 0,25 → + (0,75) → − (0,25, − apagado); 1,25 → + (1,75). Avançar até a revisão e conferir "0,75 ml".
   - Medicamento com "1/2 comprimido" (via voz simulada ou edição) → editar → + → "1 comprimido".
   - Voz, com fala injetada em `SpeechInput.start()` só durante a execução: "metformina três vezes ao dia" → pergunta
     do primeiro horário → "Escolher no relógio" → 07:30 → "Horários do dia: 07:30, 15:30 e 23:30"; "Trocar horário"
     substitui; confirmar → revisão com os três horários. Pergunta comum ("Em quais horários") continua com vários.
   - Relógio aberto na pergunta dos horários → "Algo errado? Corrigir por voz": o seletor some; depois de voltar ou
     Desfazer, a pergunta reaparece sem o seletor.
4. Pontos para olhar: regressão em `everyStepperAmountComesBackTheSame` e na ida e volta texto ↔ contador (doses salvas
   como "1/2 comprimido" não mudam sem edição); arredondamento em milésimos aceitável; se o + apagado com 120 deixa
   claro o limite (TalkBack não exigido nesta conferência).

## Relatório

`docs/revisoes/CONFERENCIA_RV_DVC_CODEX.md`: cada RV-DVC fechado ou não, achados novos S1–S4 só no diff, e o que não
foi verificado. Atualizar `HANDOFF_APP.md` (§3 e Diário), `ESTADO.md`, `ENTREGAS.md` e `REGISTRO_IA.md`. Não alterar
código; não fazer commit nem push; não operar o A31.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_CONFERENCIA_RV_DVC_CODEX.md`.
