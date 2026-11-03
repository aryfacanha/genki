# Roteiro de conferência para o Codex — DEC-047 (achados 1, 2 e 3–4 da pré-revisão)

Preparado em 29/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Conferência **pontual**; não abrir revisão
ampla. Quem confere não altera código. Contexto: `docs/PRE_REVISAO_HEURISTICA_A31.md` (achados) e DEC-047.

## Alvo

`main`, commit **`260a535`** (versão 1.0.10), sobre `ebb6883`.

| Achado | Mudança | Onde |
| --- | --- | --- |
| 1 | Adiados pendentes reagendados ao abrir o app (`restoreSnoozes`, chamado em `syncAll`); horário vencido toca 5 s depois (`ActiveAlert.snoozeAlarmAt`). No reinício, `BootReceiver` usa `AlertStore.clearRinging` (só tira os avisos que estavam tocando). Motivo: no Android 15, reabrir app parado à força entrega `BOOT_COMPLETED`, e o `clear()` apagava o adiado | `reminders/ReminderScheduler.kt`, `AlertStore.kt`, `BootReceiver.kt`, `data/MedicationRepository.kt`, `domain/ActiveAlert.kt` |
| 2 | Nome igual a cadastrado (sem acento/maiúsculas/espaços) pede confirmação antes de gravar; edição ignora o próprio | `domain/DuplicateNames.kt`, `ui/form/FormViewModel.kt`/`FormScreen.kt`, `voice/ui/VoiceViewModel.kt`/`VoiceScreen.kt` |
| 3–4 | "Digitar o horário" no relógio: campos próprios de hora e minuto (`typedTime`), seleção total ao focar, validação 0–23/0–59, troca relógio ↔ campos levando o valor | `ui/components/TimeDialog.kt` |

## Execução

1. `git pull`; `.\gradlew.bat assembleDebug test`: esperado **171 testes, 0 falhas**.
2. Regras: gravação só pelo `DraftValidator` (o aviso só adia a gravação); nada de "Tomado"; sem dependência nova.
3. Pontos de atenção:
   - Achado 1: `restoreSnoozes` não duplica alarme (mesmo `PendingIntent`); adiado de medicamento excluído ou com
     lembretes desligados não volta a tocar (receptor revalida); adiado de vários dias atrás ao reabrir; corrida entre
     `BootReceiver` e `GenkiApplication.onCreate`; reinício real mantém o cartão do adiado e toca no horário.
   - Achado 2: `saving` volta a `false` quando o aviso aparece; "Cadastrar mesmo assim" grava uma vez só; edição
     trocando o nome para o de outro cadastro avisa; voz com dois nomes repetidos lista os dois; correção por toque
     (`FixDraft`) não avisa (a gravação é na revisão da voz).
   - Achados 3–4: digitar substitui o valor ao tocar no campo; vazio ou fora da faixa desliga o OK com a mensagem;
     horário repetido continua bloqueado; voltar ao relógio com valor digitado; rotação com o diálogo aberto; fonte 2,0×.
4. Emulador (§5): adiar um lembrete real → `am force-stop` → reabrir (cartão e alarme `FIRE_SNOOZED` presentes);
   adiado vencido → reabrir → toca em segundos; aviso de duplicidade no formulário e na voz (fala simulada temporária,
   removida depois); digitação do horário um dígito por vez. Restaurar banco, preferências e código.

## Relatório

`docs/revisoes/CONFERENCIA_DEC047_CODEX.md` (achados S1–S4 só se confirmados) e registros (`HANDOFF_APP` §2/§3/§7 e
Diário, `ESTADO`, `ENTREGAS`, `REGISTRO_IA`). Não alterar código; não commitar nem fazer push; não operar o A31.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_CONFERENCIA_DEC047_CODEX.md`.
