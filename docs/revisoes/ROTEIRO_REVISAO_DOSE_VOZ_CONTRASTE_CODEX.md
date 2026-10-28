# Roteiro de revisão para o Codex — contador, relógio da voz, reparos do A31, menu da dose e contraste

Preparado em 28/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Revisão do pacote implementado depois da última
conferência do Codex (`REVISAO_CORRECAO_TROCA_UNIDADE_CODEX.md`, alvo `be24b9d`). Siga o `HANDOFF_APP.md` §6; quem
revisa não altera código.

## Alvo

`main`, intervalo **`be24b9d..61d4c22`** (código em quatro commits; os outros são só documentação):

| Commit | Pacote | O que mudou | Onde |
| --- | --- | --- | --- |
| `011d4e8` | DEC-038 | "Outra" quantidade com − número + (comprimido e ml de 0,5 em 0,5; cápsula e gota de 1 em 1); metade de comprimido gravada como "1 comprimido e meio"; "Escolher no relógio" na pergunta dos horários da voz | `DoseUnit` (`step`, `value`, `amountOf`, `plus`, `minus`, `canMinus`, `parse` de "N comprimido(s) e meio"); `FormScreen.AmountStepper`; `ui/components/TimeDialog.kt` (movido do formulário); `VoiceScreen.TimesPicker`; `VoiceQuestions.timesAnswer` |
| `f5188a0` | Reparos A31-01…06 | Achados do teste no A31 (`docs/RESULTADO_TESTE_DOSE_GRADES_A31.md`) | `VoiceScreen.Asking` (`rememberSaveable(draftId, field)`); `VoiceQuestions.nextClockTime`; `FieldError(message, modifier)` + `LocalErrorViews`/`errorView` no formulário; `AmountStepper(reveal)`; rótulo "Quantidade"; `GenkiTopBar` com `maxLines = 2` + `TextAutoSize` |
| `ce1aad7` | DEC-039 | Unidade em menu (`ExposedDropdownMenuBox`) e quantidade sempre no − número +; saem a grade de unidades, os botões Meio/1/2/3/Outra e o estado `typing` | `FormScreen.DoseInput`, `unitLabel` |
| `61d4c22` | Contraste | Campos com fundo branco (`genkiFieldColors()`) nos 6 `OutlinedTextField`; "Quanto você toma por vez" e "Horários" em `titleLarge`; avaliação `docs/AVALIACAO_CONTRASTE_WCAG.md` | `Components.kt`, `FormScreen.kt`, `VoiceScreen.kt`, `CameraScreen.kt` |

Decisões aplicáveis: DEC-036, DEC-037 (troca de unidade), DEC-038 e DEC-039 em `docs/DECISOES.md`.

## Execução

1. `git pull`; registrar a partida. `.\gradlew.bat assembleDebug test` — esperado **133 testes, 0 falhas**. Novos no
   intervalo: em `DoseUnitTest`, `halvesOfTabletAreWrittenTheWayPeopleSay`, `plusAndMinusWalkOneStepOfTheUnit` e
   `everyStepperAmountComesBackTheSame`; em `VoiceParserTest`, `timesFromClockAreUnderstood` e `clockOpensOnAFreeTime`.
   `anythingElseStaysWholeAsOther` perdeu o caso "1 comprimido e meio", que agora vira comprimido.
2. Escopo e regras (§6.3): só gravação via `DraftValidator`; o texto da dose continua livre e o banco não mudou;
   concentração ≠ dose; nada de dependência nova (o menu e o `TextAutoSize` são do Compose/Material3 já presentes).
3. Pontos para olhar com atenção:
   - **DEC-037 com o menu:** a troca de unidade usa `DoseUnit.keep` (só 1/2/3 entre comprimido e cápsula, 5/10/15
     entre gota e ml); a primeira escolha começa em 1; "Outro" leva o texto montado; editar o texto de "Outro" zera a
     quantidade guardada (RV-UN-01). Casos T01–T10 do roteiro do autor.
   - **Ida e volta do texto:** `DoseUnit.dose` normaliza "1,5" para "1 comprimido e meio" e "2.5" para "2,5 ml";
     `parse` precisa devolver o mesmo par para todo valor do − e + (teste de 1 a 60 passos). Conferir doses antigas e
     vindas da voz ("1/2 comprimido", "1 comprimido e meio", "2 jatos") e a edição de medicamentos já salvos.
   - **A31-01:** com o relógio, o campo digitado ou o seletor de dias abertos, responder por botão ou fala não pode
     deixar nada disso na pergunta seguinte; conferir também a correção por voz (`state.correcting`).
   - **A31-02:** com mais de um erro na etapa, a rolagem vai ao primeiro; a edição (página única) continua indo à
     seção. Conferir as etapas 2 e 3 (dias, horários, duração, data final), além da 1.
   - **A31-03:** escolher a unidade rola até o contador; abrir uma edição não deve rolar.
   - **A31-04:** o título com `TextAutoSize` não pode encolher em 1,0× quando cabe; conferir telas com título longo
     ("Detalhes do medicamento", "Confira antes de salvar") em 1,0×, 1,5× e 2,0×.
   - **Contraste:** conferir as razões do relatório (fórmula WCAG) e se algum texto ou ícone ficou sobre o fundo
     branco dos campos com contraste menor; o fundo branco aparece também atrás do rótulo flutuante (observação
     visual registrada).
   - **TimesPicker na pergunta do primeiro horário** ("duas vezes ao dia"): permite escolher mais de um horário,
     embora só o primeiro seja usado; dizer se é problema ou limitação.
4. Emulador (§5), sem mexer em código; se precisar de fala, injete texto em `SpeechInput.start()` só durante a
   execução e remova depois (como nas revisões anteriores). Repetir no mínimo: Q02–Q12, T01–T10 e F01–F04 do roteiro
   `docs/ROTEIRO_TESTE_DOSE_GRADES.md`; G07–G10 com fala injetada; fonte 1,0×, 1,5× e 2,0× no formulário (menu
   aberto, contador, título) e na voz. Regressão rápida §7 dos marcos M1–M3.
5. O que já foi conferido pelo Claude no A31 está em `docs/RESULTADO_TESTE_DOSE_GRADES_A31.md` (roteiro, reparos e
   sessão de voz com o autor) e no Diário do handoff. Isso não substitui a revisão: confirme de forma independente.

## Relatório

`docs/revisoes/REVISAO_DOSE_VOZ_CONTRASTE_CODEX.md`, com achados S1–S4 (ID, gravidade, arquivo:linha, problema,
correção sugerida) e o que não pôde ser verificado. Atualizar `HANDOFF_APP.md` (§2, §3, §7 e Diário), `ESTADO.md`,
`ENTREGAS.md` e `REGISTRO_IA.md`. Não alterar código; não fazer commit nem push; não operar o A31 sem pedido do autor.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_DOSE_VOZ_CONTRASTE_CODEX.md`.
