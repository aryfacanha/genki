# Revisão da correção de RV-M2-01/02 — Codex

Data: 26/10/2026. Assistente: Codex (GPT-6).
Partida e alvo: `main`, `a590fa5`; `git pull` confirmou repositório atualizado. Revisão anterior: `REVISAO_M0_M1_M2_CODEX.md` (`42a0d16`). Código do app não alterado nesta revisão.

## Escopo e fontes

Revisão do diff de `a590fa5` e dos efeitos de `ReminderPlan`, `ReminderScheduler.syncFired` e `ReminderReceiver`. Lidos o handoff vigente, estado, decisões DEC-012/014/021/026–028, ESPEC §6, relatório anterior e código relacionado. Proposta e planilha oficial reabertas pelo Google Drive; requisitos pertinentes RF04/RF09/RF10/RF24/RF25. Nenhuma decisão nova de produto.

O reparo mantém o escopo: lógica pura de planejamento sem Android; sem novas dependências, permissões ou tabelas. `NoMoreOccurrences` cancela apenas o futuro `FIRE`; `Disabled` continua cancelando tudo. Depois de um disparo, `syncFired` aplica o plano apenas ao `timeId` recebido. Cadastro, validação, concentração/dose, datas civis e ausência de histórico de ingestão não foram modificados.

## Resultado

**Correção aprovada na cobertura executada: RV-M2-01 e RV-M2-02 fechados. Nenhum novo achado de código S1–S4.** O teste no último dia com minutos consecutivos passou, inclusive preservando os avisos na inicialização de um novo processo do app. Isso não equivale ao aceite completo de M2 no aparelho físico.

| ID anterior | Gravidade original | Situação | Correção revisada e evidência |
| --- | --- | --- | --- |
| RV-M2-01 | S2 | Fechado | `reminders/ReminderScheduler.kt:49` cancela só o futuro `FIRE` quando não há próxima ocorrência. Avisos dos dois horários permaneceram ativos e com `REPEAT` agendado, inclusive após novo processo executar `syncAll`. |
| RV-M2-02 | S2 | Fechado | `reminders/ReminderScheduler.kt:41` aplica o plano somente ao horário disparado. Após 22:44, o `FIRE` das 22:45 continuou agendado e foi emitido às 22:45:00.019. |

## Verificação

- **Build e testes: passou.** `.\gradlew.bat assembleDebug test`, `BUILD SUCCESSFUL`, tarefas `UP-TO-DATE`. XMLs: 43 testes sem falhas/erros (37 anteriores + 6 de `ReminderPlanTest`). `git diff --check a590fa5^ a590fa5` sem problemas.
- **Instalado em:** Pixel_6_2, Android 15/API 35, `emulator-5556`, via `installDebug`.
- **Preparação pelo app:** `LosartanaRevisao`, `50 mg`, `1 comprimido`, todos os dias, 22:44 e 22:45, início e término em 26/10/2026; cadastro em três etapas, revisão e confirmação. Permissões já concedidas; repetição ligada, intervalo 10 min. `dumpsys alarm` confirmou ambos os `FIRE` exatos. Evidências locais: `build/review_fix_review.png` e `build/review_fix_alarm_before.txt`.
- **Primeiro disparo:** às 22:44:00.040, `FIRE timeId=15`; às 22:44:20 havia notificação ativa "Lembrete das 22:44", `REPEAT` para 22:54:00.041 e o `FIRE` das 22:45 preservado. Evidências: `build/review_fix_alarm_first.txt` e `build/review_fix_notification_first.txt`.
- **Segundo disparo:** às 22:45:00.019, `FIRE timeId=16`; às 22:45:20, ambos os avisos estavam ativos, com `REPEAT` para 22:54 e 22:55. Captura `build/review_fix_both.png`; dumps `build/review_fix_alarm_both.txt` e `build/review_fix_notification_both.txt`. O grupo automático do Android contém as duas notificações, conforme esperado.
- **Reabertura:** uma tentativa inicial com `am kill` não encerrou o processo (PID continuou 5480), portanto só comprovou retorno à Activity. Depois, o processo 5480 foi encerrado de forma controlada com `run-as ... kill -9` e o app aberto novamente (PID 6389; `app_iniciado` às 22:45:34.807). Ambos os avisos e repetições permaneceram após `syncAll`. Não houve `force-stop`, reinício do emulador nem mudança de relógio/fuso. Evidências: `build/review_fix_notification_cold.txt` e `build/review_fix_alarm_cold.txt`.
- **Adiar:** botão da notificação das 22:45, às 22:46:20.798 (`timeId=16`). Removeu esse aviso e seu `REPEAT`; agendou `FIRE_SNOOZED` para 22:56:20.795. O aviso e `REPEAT` do horário das 22:44 foram preservados. Dumps `build/review_fix_alarm_snooze.txt` e `build/review_fix_notification_snooze.txt`.
- **Entendi:** botão da notificação das 22:44, às 22:46:37.548 (`timeId=15`). Removeu aviso e `REPEAT` daquele horário; o `FIRE_SNOOZED` do outro horário continuou pendente. Nenhum registro de ingestão. Dumps `build/review_fix_alarm_ack.txt` e `build/review_fix_notification_ack.txt`.
- **Desativar e excluir:** desativar pelos detalhes cancelou o `FIRE_SNOOZED` pendente; só ficaram os alarmes dos medicamentos preexistentes. Em seguida, exclusão do cadastro de teste com confirmação, às 22:47:15.427. Metformina e Losartana preexistentes preservadas; app deixado em Hoje. Evidência: `build/review_fix_alarm_disabled.txt` e `build/review_fix_genkieval.txt`.
- **Erros no Logcat:** sem exceção/crash do app observado. Três mensagens `E FrameTracker` durante animações do teclado, às 22:39:19/24/27; não interromperam o fluxo. O encerramento controlado do processo acima foi uma ação do teste.
- **Observação sobre o roteiro:** é possível escolher minutos individuais arrastando o ponteiro do TimePicker. O 22:44 foi escolhido e conferido na interface; não foi preciso alterar o banco diretamente. O §7 do handoff foi ajustado para orientar o teste pela interface. A descrição histórica da sessão do Claude permanece atribuída a ele.

Capturas e dumps em `build/review_fix_*` são evidências locais não versionadas. Nenhum código/configuração do app foi alterado; mudanças locais preexistentes em `.idea/` e `.claude/` preservadas. Fonte e relógio do emulador mantidos.

## Limites

Esta é a revisão da correção de M2; não repete integralmente os fluxos de M0/M1 já revisados, além das abas, cadastro/revisão, lista/detalhes e exclusão usados no teste. Não foram aguardados o retorno do aviso adiado nem o ciclo de três repetições; foram conferidos os agendamentos e cancelamentos descritos. O adiamento foi cancelado ao desativar o medicamento de teste antes de 22:56. Aparelho físico, Doze/economia de bateria, alteração de fuso, permissões inicialmente negadas, deslizar a notificação e TalkBack não foram verificados nesta sessão. Voz/câmera continuam não iniciadas. Os testes e as capturas anteriores de Claude Code não são classificados como verificação independente desta sessão.

Próximo passo: completar a regressão temporal de M2 (retorno de Adiar, três repetições, deslizar) e o aceite no aparelho do autor. A revisão dos dois reparos não está mais pendente. Nenhuma nova sugestão de código ou decisão de produto.
