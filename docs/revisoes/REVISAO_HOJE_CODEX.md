# Revisão da Hoje — Codex

27/10/2026 — Codex (GPT-6). Partida: `main`/`4706c3a`; `git pull`: já atualizado.
Alvos: `f79feb9`, `2d3b4d9` e `4706c3a` (diff `c55a3cd..4706c3a`).
Escopo: resposta imediata a Entendi/Adiar, DEC-034 e DEC-035; RF02/RF09/RF24/RF25 e RNF01/02/04.

**Resultado: um S2 e dois S3 abertos; nenhum S1 encontrado na cobertura executada.**
O aviso novo não detecta o bloqueio da categoria de notificações. A linha Agora também desaparece
quando todos os horários do dia ainda estão no futuro; com fonte 2,0×, o rótulo Passou comprime
nome e dose. Sem correção de código pelo revisor.

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-HOJE-01 | **S2 — erro relevante** | `app/src/main/java/br/edu/ifrs/genki/ui/home/HomeScreen.kt:169` e `:185`; causa em `reminders/Notifications.kt:30` | A nova faixa considera somente a permissão geral do app. Se o usuário desliga apenas a categoria **Lembretes de medicamentos**, o único canal do Genki, nenhum lembrete aparece no sistema, mas a Hoje não avisa. Reproduzido no emulador API 35: `AppSettings` continuou `importance=DEFAULT`, enquanto o canal `lembretes` ficou `mImportance=0`; após voltar, a Hoje mostrou o próximo horário sem a faixa. O helper já existia; a lacuna afeta a nova garantia da DEC-034. | Verificar também `getNotificationChannel("lembretes").importance != IMPORTANCE_NONE`. Compartilhar a checagem com Ajustes e oferecer acesso ao ajuste da categoria quando ela estiver bloqueada; hoje Ajustes usa o mesmo helper e pode indicar “Permitidas” sem ação para resolver. |
| RV-HOJE-02 | **S3 — menor** | `app/src/main/java/br/edu/ifrs/genki/ui/home/HomeScreen.kt:242` | Com todos os horários ainda futuros, `firstAhead == 0`. O teste `index > 0` impede o marcador antes da primeira linha e a condição final também não o mostra. Reproduzido às 07:00 com 08:00 e 19:30: nenhuma linha Agora na lista. | Permitir o marcador também quando `index == 0`; conferir antes do primeiro, entre horários e depois do último, incluindo horários simultâneos. |
| RV-HOJE-03 | **S3 — menor** | `app/src/main/java/br/edu/ifrs/genki/ui/home/HomeScreen.kt:515` | Com fonte 2,0×, horário e o novo rótulo Passou ocupam as laterais fixas da linha, deixando nome/dose numa coluna estreita. No Pixel_6_2, “Metformina” vira “Metformin / a” e “1 comprimido” ocupa três linhas (“1 / comprimid / o”), mesmo com um nome curto. O texto completo permanece, mas a leitura piora com a ampliação (RNF02). | Colocar Passou acima/abaixo do conteúdo ou reorganizar a linha em largura reduzida/fonte ampliada, preservando espaço para nome e dose. Não reduzir a fonte para contornar a preferência do usuário. |

## Cobertura e evidências

- Lidos estado, decisões, handoff, ESPEC e mapa de requisitos. Proposta oficial e planilha atuais
  reabertas pelo conector do Drive; RF24 distingue reconhecimento do lembrete de ingestão.
- `.\gradlew.bat assembleDebug test`: **BUILD SUCCESSFUL**, 116 testes nos XMLs, zero falhas/erros;
  tarefas de compilação e testes `UP-TO-DATE` nesta execução. Não foram criados testes temporários.
- `firstAfter`: inspecionados filtro de lembretes ativos, início/fim inclusivos, busca após o dia atual
  e desempate por data/horário/nome. Os três testes novos cobrem próximo dia, dias específicos,
  desativado/encerrado e início futuro; o quarto teste novo cobre o texto da data.
- `f79feb9`: a resposta direta e o receiver usam os mesmos métodos; cancelamento de notificação,
  repetição/adiamento e atualização do `AlertStore` foram preservados por comparação do diff.
  Esses métodos não gravam medicamento nem histórico de ingestão. A resposta direta executa na
  thread principal, incluindo os `commit()` já existentes no store; latência sob carga não medida.
- APK original do alvo instalado somente em `emulator-5554` (Pixel_6_2, API 35). Conferidos por
  captura/árvore: “Passou” com fundo cinza, Agora entre 08:00 e 19:30, horário futuro com borda,
  toque em 08:00 abrindo os detalhes corretos e “Voltar para Hoje” retornando à Hoje.
- Após recuperar o emulador, conferidos: bloqueio global exibe a faixa; Resolver em Ajustes abre
  Ajustes; 07:00 sem marcador confirma RV-HOJE-02; FIRE real de 19:30 → Adiar 10 min muda
  imediatamente para 19:40 → Entendi retira o cartão, a notificação e o adiamento. Depois aparece
  “Amanhã / 08:00 / Metformina”. Logs: `lembrete_emitido` às 19:30:00, `lembrete_adiado`
  às 19:30:12 e `lembrete_reconhecido` às 19:30:15. Retorno temporal do adiamento e grupos
  não repetidos nesta rodada. Captura com fonte 2,0× confirma RV-HOJE-03.
- Evidências locais não versionadas: `build/hoje_review_initial.png`,
  `build/hoje_channel_blocked.png`, `build/hoje_channel_no_warning.png`, `build/hoje_before_first.png`,
  `build/hoje_global_warning.png`, `build/hoje_alert.png`, `build/hoje_snoozed.png`,
  `build/hoje_after_ack.png` e `build/hoje_font20.png`.

## Limites e próximo passo

Reparar os três achados e fazer conferência pontual. O resultado anterior de M3 não é reaberto.
Não houve operação no A31, alteração de código/configuração do app, commit ou push.
Alterações locais em `.idea/` e `.claude/settings.local.json` preservadas.
TalkBack audível, voz/TTS reais, câmera, Doze e aceite completo no aparelho não verificados nesta rodada.
O emulador ficou offline durante a primeira tentativa de conferir bloqueio global; uma ferramenta
PowerShell reportou `OutOfMemoryException` e o helper de controle do computador não inicializou.
Não foi observado crash do app no buffer AndroidRuntime. O emulador foi reiniciado com autorização,
sem carregar snapshot, e os testes acima foram retomados. Ao terminar: canal novamente com importância 4,
notificações concedidas, relógio real/automático, fonte 1,0× e `genki_alerts` vazio. Nenhum medicamento
criado, editado ou excluído. O reinício limpa alertas pendentes por regra do produto; já estavam vazios
antes do teste. Alarmes futuros foram reagendados ao restaurar a hora/reabrir o app.
