# Conferência pontual — DEC-048 e RV-047-01…04 — Codex

29/10/2026 · Codex · Samsung A31, Android 12 · release 1.0.11 (versionCode 12).

**Resultado: dois S2 e dois S3 confirmados.** RV-047-01…04 fechados na cobertura descrita; há verificações do roteiro que permanecem pendentes. Nenhum reparo pelo revisor.

## Alvo e condições

- Partida: `main`/`953acb745d00b9a26b00df4d63280f82398c9361`; app do commit `48f290b`, sobre `a5e1d74`.
  `git pull` executado com sucesso: atualizado. Alterações preexistentes em `.idea/`, configuração local do Claude
  e logs nativos preservadas. Sem alteração do app, commit, push, instalação ou uso da chave de assinatura.
- Pedido explícito do autor: executar o roteiro DEC-048 **no A31 pelo ADB**; prevalece sobre a indicação de emulador
  e a proibição de operar o A31 no roteiro preparado anteriormente. Serial em todas as operações: `RX8N80313SX`.
- Lidos: estado, decisões, handoff, roteiro, estudo do alarme, relatório DEC-047, especificação, mapa de requisitos e
  diff do alvo. Proposta oficial e `Requisitos!A1:H45` reabertos pelo conector Drive. DEC-047/048 aceitas prevalecem
  sobre a ESPEC §6 antiga, que ainda proíbe tela cheia/serviço e diz que adiamentos não sobrevivem ao reinício.
- APK release não é depurável (`run-as` recusado); sem acesso ao banco privado para backup binário. Testes feitos
  pela interface, usando medicamentos temporários distintos. Relógio do aparelho preservado; esperas em tempo real.
  A versão release também não emite `GenkiEval`: evidências vêm de eventos do Android, notificações, serviços,
  reprodução/foco de áudio, alarmes, árvores de interface e capturas em `build/dec048/` (ignorado pelo Git).
- Partida: Losartana, 1 comprimido, todos os dias às 08:00/20:00, contínuo; Rivaroxabana, 1 comprimido, diariamente
  às 08:00, duração indefinida. Sem cartão de aviso/adiado na Hoje. Som **Como alarme**, repetição ligada, intervalo
  **20 min**, fonte **1,3×**, rotação automática desligada/retrato. Toque/notificações mudos, alarme não mudo (6/15
  no alto-falante). A captura inicial de notificações contém também referência histórica ao ID 14; não é teste criado
  nesta sessão. O autor optou expressamente por seguir apenas pelos registros, sem conferência de ouvido.

## Achados confirmados

S1 = bloqueia · S2 = erro relevante · S3 = menor · S4 = sugestão. Reparos abaixo são **sugestões pendentes**.

| ID | Gravidade | Local | Evidência e efeito | Reparo sugerido |
| --- | --- | --- | --- | --- |
| RV-048-01 | S2 | `reminders/AlarmSoundService.kt:50` | Dois lembretes reais às 22:48 (IDs 5 e 4). O serviço usa primeiro o ID 5 e depois o ID 4 como notificação de primeiro plano. Às 22:48:00.673 o Android cancela o ID 5, motivo APP_CANCEL/8, sem ação do usuário. Só o ID 4 permanece, apesar de a tela listar ambos. O primeiro lembrete perde sua notificação e suas ações individuais; não é agrupamento visual, pois há cancelamento efetivo no sistema. | Separar a notificação do serviço das notificações de cada lembrete, ou preservar explicitamente todas ao trocar o ID do primeiro plano. Conferir dois horários e o término por tempo. |
| RV-048-02 | S2 | `reminders/AlarmSoundService.kt:134`, `:105`; `ReminderScheduler.kt:77` | Com MidiaDEC048 tocando às 22:52, tocar Entendi na notificação às 22:52:40 para o som e retira o aviso da Hoje, mas a notificação continua visível. O cancelamento ocorre antes do encerramento assíncrono do serviço; o Android republica o ID 6 com flags 0xe2 às 22:52:40.149. A árvore/captura posterior ainda mostra Entendi/Adiar, sem serviço ativo. Um segundo Entendi, já sem serviço, remove a notificação. | Coordenar parada do primeiro plano e cancelamento: remover a notificação reconhecida/adiada/excluída depois da desvinculação do serviço; preservar somente os avisos que continuam pendentes. Conferir os comandos tanto da notificação quanto da Hoje/tela de alarme. |
| RV-048-03 | S3 | `ui/settings/SettingsScreen.kt:71`; `data/Settings.kt:27`; `reminders/ReminderScheduler.kt:117` | Selecionar Como notificação não reagenda alarmes existentes: os dumps antes/depois mantêm os mesmos AlarmClockInfo, inclusive os dois de 22:48 e o próximo despertador do sistema. O setter só altera preferências/StateFlow; não há observador que sincronize o agendamento. O teste voltou a Como alarme antes do disparo. | Reagendar os alarmes pertinentes quando o modo muda, preservando os adiados e avisos ativos. Conferir também notificação → alarme. |
| RV-048-04 | S3 | `ui/settings/SettingsControls.kt:90` | O texto promete tocar “até você responder”. No teste sem resposta, o áudio iniciou às 22:48:00.364 e parou às 22:49:00.528; o foco foi abandonado às 22:49:00.560, sem resposta. O limite de um minuto é a decisão aceita; a explicação da interface omite esse limite. | Informar que toca por até um minuto e que o aviso permanece, com novas emissões conforme a repetição configurada. |

## Verificações

| Caso | Resultado observado |
| --- | --- |
| Build | `assembleDebug test` passou, tarefas reutilizadas. `testDebugUnitTest --rerun-tasks` executou **173 testes, zero falhas/erros**. Nenhum teste temporário ou instrumentação no app. |
| Manifest/regras | Só as quatro permissões previstas acrescentadas; Activity/Service não exportados. Sem rede/dependência nova, ingestão/histórico ou novo caminho até Room; alterações de alarme não gravam medicamentos. |
| Tela apagada/bloqueada | Antes: aparelho em Dozing e keyguard ativo. Às 22:48: tela acordou (Awake), abriu AlarmActivity sobre o bloqueio, listou os dois medicamentos e mostrou ações individuais/todos. |
| Som no silencioso | `MediaPlayer` do UID do Genki ativo com `USAGE_ALARM`, foco `GAIN_TRANSIENT`, serviço `mediaPlayback` iniciado a partir do alarme. Não foi audição física. |
| Limite de um minuto | Sem interação: reprodução parou e serviço terminou em cerca de 60 s; tela ainda com ambos os avisos. Falhas de notificações descritas acima. |
| Adiar todos | Às 22:49:19, ambos viraram adiados na Hoje, com dois `FIRE_SNOOZED` para 22:59:19.637 e .659; `REPEAT` anterior cancelado. |
| Vídeo em uso | Arquivo local sintético (cor sólida + tom baixo), player Samsung com `USAGE_MEDIA`, `state:started`. Às 22:52:00.117 passou a `paused`, após foco do Genki às .110; áudio do alarme começou às .194. Houve aviso no topo, sem roubar a tela do player. Entendi na notificação às 22:52:40 parou áudio/foco; notificação residual em RV-048-02. |
| RV-047-02 | Digitando separadamente 2/2 e 4/8, campos chegaram a 22:48, sem salto automático. Toque no centro do minuto cheio e novos dígitos substituíram o conteúdo; também 52 → 55 na edição. |
| RV-047-03 | 22:48 preservado ao girar retrato → paisagem → retrato com o diálogo aberto; capturas/árvores antes/depois. |
| RV-047-04 | Fonte 2,0× com teclado aberto: Hora/Minuto inteiros, números e ações visíveis, sem a quebra Minut/o. |
| Tela de alarme ampliada | Retrato em 2,0×: nomes longos quebram em linhas, doses e ações visíveis. Paisagem em 2,0×: lista em área rolável; não cabe inteira de uma vez. Rotação preservou o grupo. Capturas iniciais têm também aviso do sistema sobreposto. |
| RV-047-01 | Um novo adiado de MidiaDEC048 para 23:17:15; app em segundo plano, `am kill`, PID ausente. Às 23:17:15.402 ocorreu uma publicação normal (flags 0x80), seguida da atualização de serviço (0xc0 às .415); não houve nova publicação normal em +5 s. A Hoje mostrou o aviso novamente. Fechado para esse reprodutor; APK release não registra `GenkiEval`, então eventos de notificação são a evidência indireta. |
| Toque com aparelho em uso | Notificação das 22:55 aberta pelo painel levou à tela de alarme. Voltar e abrir a Hoje mostrou o aviso ativo. |

## Restauração e limites

Os três cadastros temporários e o vídeo sintético foram excluídos. Lista final confirma Losartana e Rivaroxabana e os horários originais. Fonte 1,3×, rotação retrato/automática desligada, repetição ligada e intervalo 20 min restaurados; modo **Como alarme**, que era o modo encontrado, mantido. Sem serviço de alarme ou notificação do Genki ativa no final. Sem backup binário do banco por ser release: preservação dos dados verificada pela interface, não byte a byte. Relógio e permissões não foram modificados.

Ao tentar navegar pela Hoje, um toque atingiu o Entendi do grupo adiado dos dois testes e cancelou esses adiamentos; nenhum dos medicamentos do autor foi respondido. O reprodutor de RV-047-01 foi refeito com outro medicamento temporário e espera real. Antes da exclusão, um toque abriu por engano a confirmação de excluir Rivaroxabana; a confirmação foi cancelada, e sua presença/horário conferidos ao final.

Não verificados: som de ouvido, TalkBack, Android 14+, falta de permissão de alarme exato, canal bloqueado, migração efetiva do canal antigo, reinício físico e adiado vencido, exclusão/desativação enquanto toca, confirmação individual enquanto o serviço toca, e fechamento da AlarmActivity ao responder fora dela. Não se infere aceite do autor, execução do MET-3 ou integração à monografia. A tela também não foi reexaminada depois de rolar a lista em paisagem 2,0×. O estudo anterior de som sem vídeo não substitui essas verificações.

Próximo: autor avaliar os quatro achados e solicitar reparos pontuais, seguidos de nova conferência. Nenhuma decisão nova aceita.
