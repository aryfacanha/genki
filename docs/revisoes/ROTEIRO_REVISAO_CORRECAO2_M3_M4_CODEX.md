# Roteiro de revisão para o Codex — segunda correção de M3/M4

Preparado em 27/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Segue o roteiro comum do `HANDOFF_APP.md` §6.

## 1. Alvo

`main`, commit **`69711ce`** (só `voice/VoiceParser.kt` e `VoiceParserTest`); `62ce6a0` só registra.
Achados que ele fecha, todos de `REVISAO_CORRECAO_M3_M4_CODEX.md`:

| ID | Correção | Onde olhar |
| --- | --- | --- |
| RV-M3-02 (restante) | Dias de trechos diferentes da mesma fala se somam; correção ("na verdade", "só", "não") substitui só os dias de antes da fala | `spokenDays` e `set(..., additive)` em `days` |
| RV-M3-04 (restante) | "não" desfaz também o horário entendido antes da fala | `negatedBefore` em `addTime` e `finish` |
| RV-M3C-01 | Letra + número não junta no nome quando o número é hora (marcador "h/hora/horas", período "da manhã…", ou pergunta de horário) | `timeAfter` em `name` |
| RV-M3C-02 | "1/2 comprimido" → dose "1/2 comprimido" | `fraction` em `dose` |
| RV-M3C-03 | "até 15/12" e "15/12/2026" leem o mês; mês inválido não vira data | `treatment`, ramo `"/"` |

## 2. Antes de começar

1. `git pull`; registre partida.
2. Leia `REVISAO_CORRECAO_M3_M4_CODEX.md` e o handoff §3 ("Segunda correção M3/M4").
3. `.\gradlew.bat assembleDebug test` — esperado: 101 testes, 0 falhas.
4. Rode seus reprodutores locais (`build/TemporaryReviewTest.kt`, `build/TemporaryCorrectionReviewTest.kt`): copiar para
   `app/src/test/java/br/edu/ifrs/genki/voice/`, rodar, **remover**. O `compareBaseline` depende da cópia antiga do parser
   (`VoiceParserBaseline`), que não está no repositório; o Claude Code rodou sem ele (20/20).

## 3. Casos vizinhos a tentar

- Dias: "segunda, quarta e também sexta e sábado"; "na verdade só sábado e também domingo"; "todos os dias" no meio de
  uma fala com dias específicos; dias para dois medicamentos na mesma fala ("losartana segunda e metformina sexta").
- Negação: "às 8, não, às 9, não, às 10"; "não às 21" sem horário anterior (vira correção); negação com "os dois";
  "não sei" na pergunta de duração; "não tomo de manhã, só à noite".
- Nome × hora: "vitamina d 8" fora da pergunta de horário (junta, como "B12"? avalie se é aceitável); "complexo b 6
  comprimidos"; "vitamina b 12 às 8".
- Fração: "1/2 comprimido e meio"? "3/4 de comprimido"; "1/2" sem unidade.
- Data: "até 31/2", "até 15/12/2025" (ano passado: o ano é ignorado e vale a próxima ocorrência — avalie), "até 1/1".

Telas não mudaram; regressão de tela opcional (texto injetado do `ROTEIRO_REVISAO_M3_M4_CODEX.md` §4.2).

## 4. Relatório

`docs/revisoes/REVISAO_CORRECAO2_M3_M4_CODEX.md` com a situação de cada achado, novos achados S1–S4 (arquivo:linha),
evidências e limites. Atualizar `HANDOFF_APP.md` (§2/§3 e Diário), `ESTADO.md`, `ENTREGAS.md`, `REGISTRO_IA.md`.
Não alterar código; não fazer push; deixar `app/src` sem ajustes.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_CORRECAO2_M3_M4_CODEX.md`.
