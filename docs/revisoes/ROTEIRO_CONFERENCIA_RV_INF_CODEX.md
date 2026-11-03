# Roteiro de conferência para o Codex — reparo RV-INF-01 a RV-INF-05

Preparado em 29/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Conferência **pontual** dos achados de
`docs/revisoes/REVISAO_TESTE_INFORMAL_CODEX.md`; não abrir revisão ampla. Quem confere não altera código.

## Alvo

`main`, commit **`7f91441`** (versão 1.0.8), sobre `a1f2ebe`.

| Achado | Reparo | Onde |
| --- | --- | --- |
| RV-INF-01 | `Confirm` só quando todas as palavras são de confirmação e há pelo menos uma central; confirmação com ressalva ("sim, mas falta X", "pronto, remover X") tira o começo e trata o resto, ficando em `NAMES` | `NameCommands.parse`, `dropLead`, `CONFIRM_WORDS`/`CONFIRM_CORE`/`LEAD_WORDS` |
| RV-INF-02 | `NOT_IS` exige o segundo "é" com acento; "não é A e B" vira `Other` (vai ao `VoiceParser`) | `NameCommands.NOT_IS` |
| RV-INF-03 | Parte de nome presente em mais de um nome composto → `Ambiguous`, nada muda; `find`/`resolve` distinguem nenhum, um e vários | `NameCommands.remove`, `resolve`, `matches`; `VoiceViewModel.interpretNames` (mensagem) |
| RV-INF-04 | Voltar e Cancelar na troca do nome param `Speaker` e `SpeechInput` e anulam o `prompt` (não abre microfone) | `VoiceScreen` (`handleBack`, `onCancelRename`), `VoiceViewModel.cancelRename` |
| RV-INF-05 | `Remove(localIds, partial)` resolve todos os nomes antes; inteiro + parte no mesmo comando; nome não encontrado ou ambíguo → nada muda | `NameCommands.remove`, `VoiceViewModel.interpretNames` |
| Sugestão | "Prefiro digitar" na conferência; `preferTyping` marca a lista como conferida | `VoiceScreen.Names`, `VoiceViewModel.preferTyping` |

## Execução

1. `git pull`; registrar a partida. `.\gradlew.bat assembleDebug test`: esperado **166 testes, 0 falhas**.
2. Repetir os reprodutores do relatório (os oito de nomes/fluxo), agora com o resultado esperado em cada achado; os casos
   novos estão em `NameCommandsTest` e `VoiceNamesFlowTest`. Procurar regressão nos que já passavam (confirmações
   curtas, "trocar X por Y", "não é X, é Y", separar, câmera → voz, fala completa → revisão).
3. Emulador (§5), fala injetada em `SpeechInput.start()` só durante a execução e removida depois:
   - RV-INF-04 na tela: tocar um nome → Voltar **durante** a fala "Diga o nome certo…" → resposta simulada não deve
     incluir nada; repetir com Cancelar, durante a escuta e com resposta pendente. Conferir `voz_ouvindo` no logcat.
   - RV-INF-01/03/05 com fala injetada no fluxo (um caso de cada) e a mensagem de ambiguidade na tela e falada.
   - "Prefiro digitar" na conferência: um remédio → formulário; vários → revisão, sem voltar à conferência.
   - Fonte 2,0× na conferência com o link novo.
4. Restaurar o emulador como na revisão (banco, preferências, fonte, permissões).

## Relatório

`docs/revisoes/CONFERENCIA_RV_INF_CODEX.md`: cada achado fechado, reaberto ou parcial, com evidência; achados novos
só se confirmados no reparo (S1–S4). Atualizar `HANDOFF_APP.md` (§2, §3, §7 e Diário), `ESTADO.md`, `ENTREGAS.md` e
`REGISTRO_IA.md`. Não alterar código; não fazer commit nem push; não operar o A31; não usar a chave de assinatura.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_CONFERENCIA_RV_INF_CODEX.md`.
