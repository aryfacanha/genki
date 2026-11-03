# Roteiro de conferência para o Codex — reparo RV-4041-01…04

Preparado em 28/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Siga o `HANDOFF_APP.md` §6; quem revisa não
altera código.

## Alvo

`main`, commit **`f4a3dff`** (reparo dos achados de `docs/revisoes/REVISAO_DEC040_041_CODEX.md`, commit `04b2aac`).
Conferência pontual: só os quatro achados e a regressão imediata do que foi tocado; não abrir revisão ampla.

| ID | Reparo | Onde |
| --- | --- | --- |
| RV-4041-01 | X de "Entendi até agora" (`SoFarCard`, parâmetro `onAsk` = `onQuiet`) e a pergunta de sair (`handleBack` com rascunhos) chamam `speaker.stop()` (zera o fim pendente) e `input.cancel()` antes de abrir o diálogo | `voice/ui/VoiceScreen.kt` |
| RV-4041-02 | `notificationsAllowed` relido também quando `settings.alarmSound` muda | `ui/settings/SettingsScreen.kt` |
| RV-4041-03 | Pedido do Android só uma vez (`asked`); recusado, fica em "4 de 4"; o toque seguinte abre as configurações do celular | `ui/intro/IntroScreen.kt` |
| RV-4041-04 | Abas, "Ajustes" da Hoje e abrir a Hoje usam `popUpTo<HomeRoute>` em vez de `findStartDestination()` | `ui/navigation/GenkiApp.kt` |

## Execução

1. `git pull`; registrar a partida. `.\gradlew.bat assembleDebug test`: esperado **137 testes, 0 falhas**.
2. Emulador (§5), sem mexer em código; fala injetada em `SpeechInput.start()` e log do TTS só durante a execução,
   removidos depois (conferir SHA-256 como na revisão anterior).
   - **01:** repetir a evidência do relatório: pergunta sendo falada → X → manter "Tirar …?" aberto até depois do fim
     previsto da fala; a escuta não deve iniciar. Cancelar o diálogo: a pergunta fica parada, microfone só por toque.
     Repetir com Voltar (pergunta "sair do cadastro") e com o X tocado durante a escuta já aberta.
   - **02:** bloquear só `lembretes_alarme`, abrir Ajustes com "Como notificação" e trocar para "Como alarme": deve
     passar a "Não permitidas" na hora, com o botão abrindo o canal certo. Voltar a notificação: "Permitidas".
     Restaurar a importância do canal.
   - **03:** POST_NOTIFICATIONS revogada e sem flags; introdução pelos Ajustes → página 4 → Permitir → Não permitir:
     continua em "4 de 4"; segundo toque abre as configurações do Genki. Conceder de novo ao fim.
   - **04:** `intro_done=false` → abrir → Pular (e, separadamente, Começar) → Medicamentos → Ajustes → Voltar deve ir à
     Hoje; Voltar na Hoje sai do app; sem `Ignoring popBackStack` no Logcat. Também: "Ver a introdução de novo" →
     Começar → abas normais; toque numa notificação com o app na introdução (se viável).
3. Regressão rápida: E16 (tirar o único / tirar o da pergunta atual), E17 (escuta automática ainda abre sem diálogo),
   D4 (Ajustes pela Hoje).

## Relatório

`docs/revisoes/CONFERENCIA_RV4041_CODEX.md`: para cada ID, fechado / aberto / não verificado, com evidência; achado
novo só se causado pelo reparo (S1–S4). Atualizar `HANDOFF_APP.md` (§2, §3, §7 e Diário), `ESTADO.md`,
`ENTREGAS.md` e `REGISTRO_IA.md`. Restaurar preferências, permissões e canais do emulador como encontrados. Não
alterar código; não fazer commit nem push; não operar o A31.

Execução: `Get-Content docs/revisoes/ROTEIRO_CONFERENCIA_RV4041_CODEX.md -Raw -Encoding utf8 | codex.cmd exec -m gpt-6.1-sol -s workspace-write -`.
