# Roteiro de revisão para o Codex — pré-congelamento (1.0.12 a 1.0.17)

Preparado em 30/10/2026 por Claude Code (Opus 5.5), a pedido do autor ("faça uma outra bateria utilizando o codex para
revisar"). Congelamento do app previsto para **02/11/2026**. Quem revisa **não altera código** (AGENTS.md, DEC-026).
Leia antes: `AGENTS.md`, `docs/ESTADO.md` (topo), `docs/HANDOFF_APP.md` (§2, §3, §5, §7), `docs/DECISOES.md`
(DEC-004, DEC-014, DEC-040, DEC-046 a DEC-050).

## Alvo

`main`, commit **`a184acd`** (versão 1.0.17), sobre **`8a27111`** (última conferência do Codex, DEC-048). O autor
testou manualmente no A31 as mudanças da 1.0.17 e diz que estão ok (30/10).

| Versão | Commit | Mudança | Onde |
| --- | --- | --- | --- |
| 1.0.12 | `ebad64f` | RV-048-01..04: notificação própria do serviço de alarme (`Int.MAX_VALUE`, canal `alarme_tocando`), troca do som reagenda (`syncAll`), texto do limite de 1 min | `reminders/AlarmSoundService.kt`, `Notifications.kt`, `GenkiApplication.kt`, `SettingsControls.kt` |
| 1.0.13 | `e2b905b` | Lote 1 do retorno da bateria: textos de Ajustes; nome repetido avisado ao sair da etapa 1 e na conferência dos nomes da voz; faixa "Lembretes ativos/desativados"; "Resolver em Ajustes" rola até Permissões; exemplo na abertura da voz; verbos "tomou/tomei/usei…" ignorados no nome; etiqueta "Experimental" na câmera | `FormViewModel`, `VoiceViewModel`, `DetailScreen`, `GenkiApp`, `SettingsScreen`, `VoiceParser.COMMON`, `AddChoiceScreen`, `CameraScreen` |
| 1.0.14 | `b0b7d45` | DEC-049: assistente de voz opcional (`voice_assistant`, padrão falso), silenciar, tutorial lido; conferência dos nomes sem pergunta falada | `Settings`, `Tutorial.kt`, `IntroScreen`, `NameCommands`, `VoiceScreen` |
| 1.0.15 | `3d0198e` | Espera da voz de volta à da 1.0.13 (a da 1.0.14 "travava"); leitura ligável na própria voz; sem leitura, microfone só no toque | `SpeechInput.kt` (igual a `b1e5a40`), `VoiceScreen` |
| 1.0.16 | `cb4c7ad` | DEC-050: botão "Silenciar a voz"/"Ouvir as perguntas em voz alta" abaixo do microfone em todas as fases da voz; alto-falante pisca enquanto fala (`Speaker.speaking`); leitura ligada ao abrir a voz; `GenkiTopBar` sem `extra`; pré-revisão heurística | `Components.kt` (`ReadAloudButton`, `VoiceMuteButton`), `Speaker.kt`, `Tutorial.kt`, `VoiceScreen`, `docs/PRE_REVISAO_HEURISTICA_1.0.16.md` |
| 1.0.17 | `a184acd` | Achados N1–N5/N15: Hoje recolhe os horários que passaram e diz quais remédios não aparecem e por quê; "Até dd/mm/aaaa"/"Tratamento terminou em…"; "Sem lembretes: o tratamento terminou"; voz com rodapé em `FlowRow`, pergunta rolando para a vista (`BringIntoViewRequester`), outros remédios numa linha | `HomeScreen`, `MedicationText`, `OccurrenceCalculator.ended`, `MedicationsScreen`, `DetailScreen`, `VoiceScreen` |

## Execução

1. `git pull`; registrar commit de partida; `.\gradlew.bat assembleDebug test`: esperado **179 testes, 0 falhas**.
2. Regras do produto a conferir em todo o diff (`git diff 8a27111 a184acd -- app`): toda gravação pelo `DraftValidator`;
   "Entendi" não registra ingestão, nada de "Tomado" (DEC-004); datas civis no fuso do aparelho (`LocalDate.now()`
   novos em `MedicationsScreen`, `DetailScreen`, `HomeViewModel`); sem rede, Hilt, WorkManager, DataStore ou LLM;
   concentração ≠ dose.
3. Pontos de atenção:
   - **Voz (`VoiceScreen`, `Speaker`, `SpeechInput`):** leitura ligada ao abrir com o assistente desligado;
     `toggleReading` no meio da fala e durante a escuta; `afterPrompt` só abre o microfone com leitura ligada; callback
     `onDone` do `Speaker` chamado duas vezes ou nunca (fala trocada, `stop`, motor sem português) e `speaking` preso em
     verdadeiro; `rememberSaveable` do `reading` na rotação; `BringIntoViewRequester` disparando em cada recomposição ou
     brigando com a rolagem do teclado (`typing`); `FlowRow` com fonte 2,0×.
   - **Nome repetido no começo (1.0.13):** `duplicateOnContinue`/`acceptedDuplicate` no formulário e
     `duplicatesOnNames`/`acceptedDuplicates` na voz; edição do próprio remédio não pode acusar repetido; a checagem
     final ao salvar continua.
   - **Hoje (1.0.17):** `showPast` com `rememberSaveable` dentro de um ramo do `when`; contagem `firstAhead` quando
     todos já passaram; `hidden` (desligados e terminados) com nomes repetidos; texto quando todos estão desligados.
   - **Tratamento terminado:** `OccurrenceCalculator.ended` coerente com `isScheduledOn` (data final inclusiva);
     "Desativar lembretes" e edição de um remédio terminado.
   - **Alarme (1.0.12):** só regressão rápida do RV-048 (o autor validou alarmes e notificações no A31).
4. Emulador (§5, Pixel_6_2 API 35): copiar antes banco e preferências (`run-as br.edu.ifrs.genki`) e **restaurar byte a
   byte** no fim. Conferir: voz com leitura ligada e silenciada; Hoje com vários horários passados e um remédio com
   lembretes desligados; fonte 2,0× na voz e na Hoje. Para a voz sem microfone real, pode simular a fala só no emulador
   e desfazer depois (como em `PRE_REVISAO_HEURISTICA_1.0.16.md`), conferindo `git diff -- app` vazio no fim.

## Veredito pedido

Além dos achados, responda: **a 1.0.17 está apta ao congelamento de 02/11?** Liste o que bloqueia (S1/S2) e o que pode
ficar como limitação registrada (S3/S4).

## Relatório

`docs/revisoes/REVISAO_PRE_CONGELAMENTO_CODEX.md` (achados S1–S4 só se confirmados, com evidência e reprodutor) e
registros (`HANDOFF_APP` §2/§3/§7 e Diário, `ESTADO`, `ENTREGAS`, `REGISTRO_IA`). Não alterar código; não commitar nem
fazer push; não operar o A31.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_PRE_CONGELAMENTO_CODEX.md`.
