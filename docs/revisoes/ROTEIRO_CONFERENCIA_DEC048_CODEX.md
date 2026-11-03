# Roteiro de conferência para o Codex — alarme de verdade (DEC-048) e RV-047-01 a 04

Preparado em 29/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Conferência **pontual** do commit; quem confere
não altera código. Contexto: `docs/ESTUDO_ALARME_TELA_CHEIA.md`, `docs/revisoes/CONFERENCIA_DEC047_CODEX.md`, DEC-048.

## Alvo

`main`, commit **`48f290b`** (versão 1.0.11), sobre `a5e1d74`.

| Item | Mudança | Onde |
| --- | --- | --- |
| DEC-048 som | No modo "Como alarme", serviço em primeiro plano toca o alarme do sistema em loop (`USAGE_ALARM`, foco `AUDIOFOCUS_GAIN_TRANSIENT`, vibração), usa a notificação do lembrete como notificação do serviço e para no Entendi/Adiar/desativar/excluir ou em 1 min (`STOP_FOREGROUND_DETACH`) | `reminders/AlarmSoundService.kt`; chamadas em `ReminderReceiver`, `ReminderScheduler.acknowledge/snooze/cancel` |
| DEC-048 tela | Full-screen intent abre `AlarmActivity` (showWhenLocked/turnScreenOn), com os avisos que a abriram e os do mesmo horário; vários: "Entendi" por remédio, "Entendi todos", "Adiar todos" | `reminders/AlarmActivity.kt`, `Notifications.show` |
| DEC-048 agendamento | `setAlarmClock` no modo alarme; canal silencioso `lembretes_despertador`, canal antigo apagado; permissão de tela cheia em Ajustes no Android 14+ | `ReminderScheduler.set`, `Notifications`, `SettingsScreen`, manifest |
| RV-047-01 | `FIRE_SNOOZED` só avisa se o aviso ainda está adiado (`ActiveAlert.shouldFireSnoozed`) | `ReminderReceiver`, `domain/ActiveAlert.kt` |
| RV-047-02 a 04 | `twoDigits` (dígito em campo cheio substitui), `rememberSaveable` com `TextFieldValue.Saver`, rótulo acima e largura por peso | `ui/components/TimeDialog.kt` |

## Execução

1. `git pull`; `.\gradlew.bat assembleDebug test`: esperado **173 testes, 0 falhas**.
2. Regras: Entendi não registra ingestão (DEC-004); nada de "Tomado"; manifest só com as permissões novas declaradas
   (FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, USE_FULL_SCREEN_INTENT, VIBRATE); sem rede.
3. Pontos de atenção:
   - Serviço: início em segundo plano a partir do alarme exato (Android 12+ e 14+, tipo `mediaPlayback`); notificação
     ausente ou bloqueada; dois lembretes ao mesmo tempo; `stop` vindo do receptor (notificação) e da Hoje; processo
     morto no meio; som não para nunca? (limite de 1 min); som que continua depois de excluir/desativar.
   - Tela: abre por cima da tela bloqueada; com o celular em uso, aviso no topo e abertura ao tocar; vários do mesmo
     horário aparecem mesmo que o Android abra a tela por um só; avisos antigos de outro horário **não** aparecem nem são
     respondidos ali; fecha quando tudo é respondido em outro lugar; Voltar; rotação; fonte 2,0×; leitor de tela.
   - `setAlarmClock`: só no modo alarme; voltar para "Como notificação" com lembretes já agendados; sem permissão de
     alarme exato.
   - Canal novo: migração de quem já tinha "Como alarme" (canal antigo apagado, preferência mantida); `canNotify` e o
     atalho de ajuste do canal em uso.
   - RV-047-01..04: repetir os reprodutores do relatório da DEC-047.
4. Emulador (§5): ligar "Como alarme"; lembrete 2–3 min à frente com a tela bloqueada (`input keyevent KEYCODE_SLEEP`);
   conferir tela de alarme, `dumpsys audio` com `USAGE_ALARM`, `dumpsys alarm` (próximo despertador); dois remédios no
   mesmo horário (Entendi de um, depois de todos); com vídeo/áudio de mídia tocando, conferir perda de foco do player;
   Adiar pela tela; limite de 1 min. Restaurar banco, preferências e o modo de som.

## Relatório

`docs/revisoes/CONFERENCIA_DEC048_CODEX.md` (achados S1–S4 só se confirmados) e registros (`HANDOFF_APP` §2/§3/§7 e
Diário, `ESTADO`, `ENTREGAS`, `REGISTRO_IA`). Não alterar código; não commitar nem fazer push; não operar o A31.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_CONFERENCIA_DEC048_CODEX.md`.
