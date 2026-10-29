# Som "Como alarme" com vídeo tocando e estudo de alarme em tela cheia

29/10/2026, Claude Code (Opus 5.5), a pedido do autor ("testar … na versão alarme … quando algum vídeo está tocando;
estudar … se é viável … virar como se fosse um alarme mesmo, com overlay, mesmo com o celular desligado").
**Categoria:** teste técnico + estudo. Nada foi alterado no app.

## 1. Teste no A31 (Genki 1.0.10, via ADB)

Condições: A31 no **silencioso** (toque e notificação em 0), volume de alarme 15/15, mídia 9/15, sem Não perturbe.
Ajustes → Som do lembrete → **Como alarme** (restaurado para "Como notificação" no fim). Medicamento de teste com
horário digitado, app em segundo plano. Medição: `dumpsys audio` (reprodutores ativos, histórico de reprodução e
foco) a cada ~1,5 s em volta do horário e `dumpsys notification`.

| Caso | Notificação | Som de alarme | Vídeo |
| --- | --- | --- | --- |
| 20:33, YouTube tocando | Postada no canal `lembretes_alarme` (som `alarm_alert`, uso `USAGE_ALARM`, importância alta); o serviço a registrou como a notificação sonora (`mSoundNotificationKey`) | **Nenhum reprodutor iniciado**; nenhum pedido de foco de áudio do sistema | Continuou tocando, sem abaixar |
| 20:37, sem vídeo | Idem | **Nenhum reprodutor iniciado** | — |

**Leitura:** o problema não é o vídeo. No A31 em silencioso, o canal "Como alarme" não produziu som em nenhum dos dois
casos, pelo que o sistema registra. O canal está configurado certo; quem decide tocar o som de uma **notificação** é o
Android/One UI, e o som com uso de alarme não é garantido em silencioso (nem toca em loop, como um despertador).

**Limites:** medição pelos registros do sistema, **sem ouvir** (o ADB não capta som). D11 da bateria também nunca foi
conferido de ouvido. Confirmar com o autor perto do aparelho antes de concluir.

## 2. Estudo: tela de alarme como a do relógio

| Pedido | Viável? | Como |
| --- | --- | --- |
| Celular **desligado** | **Não** | Nenhum app de terceiros roda com o aparelho desligado (alguns relógios de fabricante usam um recurso próprio do sistema) |
| Tela **apagada/bloqueada**: tela cheia por cima, como despertador | **Sim** | Notificação com *full-screen intent* abrindo uma tela própria com `showWhenLocked` e `turnScreenOn` (o mesmo mecanismo do relógio e das chamadas) |
| Tocar **mesmo em silencioso e por cima de vídeo**, em loop até responder | **Sim** | O app toca o som ele mesmo (`MediaPlayer`/`Ringtone` com `USAGE_ALARM`, em loop) num serviço em primeiro plano, pedindo foco de áudio transitório: o vídeo pausa ou abaixa. O volume é o de alarme, que o silencioso não zera |
| Celular **em uso** (tela ligada) | Parcial | O Android mostra como aviso no topo (*heads-up*), não em tela cheia; o som em loop continua |
| Prioridade máxima no horário | Sim | `AlarmManager.setAlarmClock` (ícone de despertador na barra; o sistema trata como alarme do usuário) |

**Permissões e regras:** `USE_FULL_SCREEN_INTENT` (no Android 12 é concedida na instalação; a partir do 14 o Google
Play só a mantém para apps de chamada/alarme, e fora da loja dá para conferir com `canUseFullScreenIntent()` e levar a
pessoa ao ajuste); `FOREGROUND_SERVICE` + tipo do serviço (Android 14+); alarme exato já existe. Não perturbe: alarmes
passam por padrão.

**O que mudaria no Genki** (só no modo "Como alarme"):
1. Tela de alarme (nova): horário grande, remédios do horário, **Entendi** e **Adiar N min**, abre sobre a tela bloqueada.
2. Serviço de som: toca o alarme em loop com foco de áudio; para no Entendi/Adiar ou depois de ~1 minuto (então fica a
   notificação e a repetição de sempre).
3. Agendamento com `setAlarmClock` para esses lembretes.
4. Nada muda em "Como notificação".

**Custo e risco:** cerca de 1 dia (tela, serviço, permissões, testes no A31 de ouvido), mais conferência do Codex.
Mexe no caminho do lembrete (RF09/RF24/RF25) a 3 dias do congelamento (02/11). É ampliação do que a DEC-032 previa
(som de alarme), não da proposta em si, mas precisa de decisão do autor e registro.

## Recomendação

1. Confirmar de ouvido: no A31 em silencioso, "Como alarme", lembrete 2 minutos à frente, sem vídeo. Se não tocar,
   o "Como alarme" atual **não cumpre o que a tela promete** ("mesmo com o celular no silencioso").
2. Se não tocar: ou implementar o alarme real acima (resolve silencioso, vídeo e tela bloqueada), ou, no mínimo, trocar
   o texto do Ajustes para não prometer o que o sistema não garante e registrar como limitação.
