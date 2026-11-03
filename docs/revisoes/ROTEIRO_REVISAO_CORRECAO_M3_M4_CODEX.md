# Roteiro de revisão para o Codex — correção dos achados de M3/M4

Preparado em 27/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Segue o roteiro comum do `HANDOFF_APP.md` §6 e os
procedimentos de emulador do roteiro anterior (`ROTEIRO_REVISAO_M3_M4_CODEX.md` §4), que continuam valendo.

## 1. Alvo

`main`, intervalo **`1ed689b..7726739`** (dois commits de código):

| Commit | Achados |
| --- | --- |
| `d5b51d5` | RV-M3-01 (concentração composta), RV-M3-02 (dias somados), RV-M3-04 (negação de horário), RV-M3-06 (B12); `domain/Frequency.fill` |
| `7726739` | RV-M3-03 (frequência na troca para digitação), RV-M3-05 (data final na revisão da voz), RV-M2B-01 (aviso fora do dia na Hoje), RV-M4-01 (origem no log); **DEC-030** (dias ausentes sem seleção) |

`2283847` só registra (handoff, estado, entregas, registro, DEC-030). Relatório da revisão anterior:
`REVISAO_M3_M4_CODEX.md`; reprodutores locais da sessão anterior em `build/TemporaryReviewTest.kt` e `build/review_parser.xml`.

## 2. Antes de começar

1. `git pull`; registre branch e commit de partida.
2. Leia `AGENTS.md`, `ESTADO.md`, `HANDOFF_APP.md` (§3 "Correção dos achados…" e Diário de 27/10), `DECISOES.md`
   (DEC-029, **DEC-030**), `REVISAO_M3_M4_CODEX.md`.
3. `.\gradlew.bat assembleDebug test` — esperado: 96 testes, 0 falhas.

## 3. O que verificar

Para cada achado: a correção resolve o caso relatado **e não quebra o vizinho**. Rode de novo o seu `TemporaryReviewTest`
(copie para `app/src/test`, rode, remova) além dos testes novos.

- **RV-M3-01** (`VoiceParser.strength`, token `/` no `TOKEN`): "250 mg por 5 ml", "250mg/5ml", "500 mg por ml" →
  concentração composta, dose vazia; com dose dita depois ("…, tomo 10 ml") cada valor no seu campo. Procure efeito do
  novo token `/` em outras frases (datas "15/11", "1/2 comprimido").
- **RV-M3-02** (`days`, `beforeDays`, `additive`): "e sexta" soma; "na verdade é sexta", "só sexta" e "não, sexta"
  substituem; "todos os dias" vira todos. Verifique "de segunda a sexta" depois de "sábado" e dias ditos na primeira fala.
- **RV-M3-04** (`cue` com `nao`, `lastTime`/`negateLast` em `addTime`): "às 8 e às 20, não, às 21" → 08:00 e 21:00;
  "às 8 não às 9" → 09:00; "não sei" como duração continua; "não" sem horário antes vira correção (`correction = true`) —
  avalie se isso muda algo indevido (ex.: "não tomo de manhã, só à noite").
- **RV-M3-06** (token `\p{L}\d{1,3}`, junção em `name`): "vitamina B12", "vitamina b 12", "vitamina d3 2000 ui",
  e "vitamina d 7 gotas" (não junta: o número tem unidade depois). Procure falso positivo: letra solta + número que é
  horário ou quantidade sem unidade.
- **RV-M3-03** (`FormFields.everyHours`, `addTime`, aviso na etapa 2): "metformina 1 comprimido duas vezes ao dia" →
  "Prefiro digitar" → etapa 2 com "Você disse de 12 em 12 horas…"; primeiro horário gera o resto; depois disso, novo
  horário só se soma. Confira também "Continuar digitando" a partir da câmera (sem frequência).
- **RV-M3-05** (`VoiceScreen.period`): "por N dias" e "até dia X" na revisão da voz mostram "De … até …" igual ao que o
  validador grava; com vários medicamentos.
- **RV-M2B-01** (`HomeScreen`): aviso pendente aparece mesmo com o dia sem horários, com "Nenhum outro lembrete para
  hoje."; dia com horários continua igual. Reproduzido pelo Claude Code na virada do dia com o relógio (handoff §3);
  repita de forma independente se possível.
- **RV-M4-01** (`VoiceViewModel.save`): câmera → completar por voz → salvar → `medicamento_salvo fonte=CAMERA`
  (só inspecionado pelo Claude Code).
- **DEC-030** (`FormFields.from`, `daysMode` nulo, erro abaixo das opções): rascunho sem dias (voz ou câmera) abre sem
  opção marcada e bloqueia com "Selecione pelo menos um dia."; cadastro manual continua começando em "Todos os dias";
  edição de medicamento salvo continua com os dias dele.
- Regressão rápida do §7 (M1 formulário manual, M2/DEC-029 com o relógio, M3, M4).

## 4. Relatório

- `docs/revisoes/REVISAO_CORRECAO_M3_M4_CODEX.md`: situação de cada achado (fechado/aberto), novos achados S1–S4 com
  arquivo:linha, verificação com evidências e limites. Diga se usou os ajustes locais de voz/câmera e que foram removidos.
- Atualizar `HANDOFF_APP.md` (§2/§3 e Diário), `ESTADO.md`, `ENTREGAS.md`, `REGISTRO_IA.md`.
- **Não alterar código** (DEC-026). Não fazer push sem autorização do autor. Deixar o emulador limpo (Metformina 08:00 e
  Losartana 19:30 preservadas, fonte 1,0, relógio real).

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_CORRECAO_M3_M4_CODEX.md`.
