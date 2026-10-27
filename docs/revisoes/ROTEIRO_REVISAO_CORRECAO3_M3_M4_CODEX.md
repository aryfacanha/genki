# Roteiro de revisão para o Codex — terceira correção de M3/M4 (fechamento do interpretador)

Preparado em 27/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Segue o roteiro comum do `HANDOFF_APP.md` §6.
**Escopo fechado:** confirmar os três achados abaixo e procurar regressão causada por eles. Casos novos sem relação
com estas mudanças vão para "limitações observadas", não para novos ciclos, salvo S1 ou erro que grave dado diferente
do dito sem passar pela revisão. Objetivo do autor: encerrar a revisão do interpretador e seguir para o teste no celular.

## 1. Alvo

`main`, commits **`fac6a0a`** (só `voice/VoiceParser.kt` e `VoiceParserTest`) e **`8191c25`** (abaixo). Achados de `REVISAO_CORRECAO2_M3_M4_CODEX.md`:

| ID | Correção | Onde olhar |
| --- | --- | --- |
| RV-M3-02 | Correção dentro da fala ("na verdade", "corrigindo", "só", "não" sem horário antes) limpa os dias já ditos nela; trechos aditivos continuam somando | `correct()` e `spokenDays.clear()` em `cue` |
| RV-M3C-03 | Data impossível ("31/2", "15/13", "31 de fevereiro") é consumida inteira e não preenche término, com ou sem pergunta de duração/data | `treatment` (retorno `next` quando `date` é nulo) |
| RV-M3D-01 | Ano dito é respeitado ("15/12/2027", "15/12/26", "10 de janeiro de 2027"); data já passada não é aceita e a tela pergunta de novo; sem ano, próxima ocorrência | `date()` |

**Acrescentado em 27/10 (a pedido do autor): commit `8191c25`**, ajustes após o teste do autor no Samsung A31 (DEC-031):

| Mudança | Onde olhar | O que conferir |
| --- | --- | --- |
| Respostas direcionadas: com pergunta ativa (exceto a do nome), nomes e palavras desconhecidas não criam medicamento; tudo vai para o perguntado | `VoiceParser.name` (retorno antecipado com `target` + `expecting`) | "eu tomo umas 9 da noite" e "também tomo metformina às 20" na pergunta de horário; fala livre e "Corrigir por voz" (sem `expecting`) continuam criando medicamento; pergunta do nome continua aceitando nome |
| Palavras de aproximação ("umas", "uns", "lá pelas", "por volta das", "mais ou menos", "geralmente"…) | `COMMON` | Nenhuma vira nome; não quebram horários ("umas 9 da noite" = 21:00) |
| "Corrigir por voz" na revisão com "Manter correção"/"Desfazer"; voltar desfaz só a correção | `VoiceViewModel.interpret/keepCorrection/undoCorrection`, `VoiceScreen.Review`, `handleBack` | Correção pendente + "Confirmar e salvar" não aparece; "Corrigir" de um cartão durante a correção pendente (comportamento não pensado: avalie); correção que deixa campo faltando → "Manter" pergunta |
| Leitura em voz alta tolerante: `setLanguage` ≥ disponível, leitor do Google como reserva, log `tts_iniciado` | `voice/Speaker.kt` | Init com falha dentro do construtor (`isInitialized`); troca para o Google sem vazar o primeiro `TextToSpeech`; `stop`/`shutdown` |

Telas mudaram neste commit: conferir no emulador com o texto injetado do `ROTEIRO_REVISAO_M3_M4_CODEX.md` §4.2.
Aparelho de teste definido: Samsung A31 (Android 12); o ADB pelo cabo atual cai — não depender dele.

## 2. Execução

1. `git pull`; `.\gradlew.bat assembleDebug test` — esperado: **106 testes, 0 falhas**.
2. Reprodutores locais em `build/` (`TemporaryReviewTest`, `TemporaryCorrectionReviewTest`, `TemporarySecondCorrectionTest`,
   com `VoiceParserBaseline.kt` e `VoiceParserBeforeSecond.kt`): copiar para `app/src/test/java/br/edu/ifrs/genki/voice/`,
   rodar `--tests "br.edu.ifrs.genki.voice.Temporary*"`, **remover**. O Claude Code obteve 36/36 (7 + 14 + 15).
3. Vizinhos diretos a tentar: "só" usado para horário depois de dias ("segunda e quarta só às 8" mantém os dias?);
   "na verdade" entre dois medicamentos; "até 29/2/2028" (bissexto) e "até 29/2" sem ano; "até 15/12/2025" como
   resposta END_DATE; ano de 3 dígitos.

Para `fac6a0a` as telas não mudaram; para `8191c25`, ver acima.

## 3. Relatório

`docs/revisoes/REVISAO_CORRECAO3_M3_M4_CODEX.md`: situação dos três achados, regressões (se houver) e limitações
observadas. Atualizar `HANDOFF_APP.md` (§2/§3 e Diário), `ESTADO.md`, `ENTREGAS.md`, `REGISTRO_IA.md`.
Não alterar código; não fazer push; `app/src` sem ajustes ao terminar.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_CORRECAO3_M3_M4_CODEX.md`.
