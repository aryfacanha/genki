# Revisão independente — M0, M1 e M2 — Codex

Data: 26/10/2026. Assistente: Codex (GPT-6). Partida: `main`, `42a0d16`; `git pull` confirmou repositório atualizado. Implementações: M1 `f2ff130`, M2 `f111670`, com regressão do M0. Nenhum código do app foi alterado.

Fontes lidas: `AGENTS.md`, `ESTADO.md`, `DECISOES.md` (especialmente DEC-012/014/021/026–028), `HANDOFF_APP.md`, `ESPEC_ARQUITETURA.md`, `REQUISITOS_MAPA.md`; originais atuais da proposta e da planilha oficial pelo Google Drive; fontes do protótipo local em `295212e` (cadastro, revisão e Ajustes). As decisões aceitas são a referência; as correções abaixo são sugestões pendentes, não novas decisões do autor.

**Resultado:** sem achados novos em M0/M1 dentro da cobertura descrita; M2 precisa de correção de **dois S2** antes de ser considerado aprovado nesta revisão. Os 37 testes unitários existentes não cobrem os efeitos de `ReminderReceiver`/`ReminderScheduler` sobre as notificações e os alarmes.

## Achados

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-M2-01 | S2 — erro relevante | `app/src/main/java/br/edu/ifrs/genki/reminders/ReminderScheduler.kt:39` (cancelamento nas linhas 44–47; chamada em `ReminderReceiver.kt:74`) | Na última ocorrência de cada horário de um tratamento temporário, `FIRE` publica o aviso e agenda a repetição, mas depois `sync()` encontra `next == null` e chama `cancel()`. Isso remove imediatamente a própria notificação e a repetição, sem Entendi/deslizar. O tratamento ainda está no dia final inclusivo. Viola RF24 e impede a interação normal com RF25 nessa ocorrência. | Separar o reagendamento de futuros `FIRE` do cancelamento do aviso atual e seus retornos. A ausência de outra ocorrência futura não pode reconhecer implicitamente o lembrete recém-emitido. Manter o cancelamento completo para exclusão/desativação/alteração que o exija; testar explicitamente a última ocorrência. |
| RV-M2-02 | S2 — erro relevante | `app/src/main/java/br/edu/ifrs/genki/reminders/ReminderReceiver.kt:74` | Depois de um `FIRE`, `sync(entity, now + 1 min)` recalcula **todos** os horários do medicamento. Um segundo horário no minuto seguinte já fica antes de `after`: é substituído por outra data ou cancelado, embora ainda não tenha acontecido. Ex.: 20:45 e 20:46. Viola RF04/RF09. | Reagendar somente o horário disparado, usando uma referência que exclua a ocorrência já emitida sem saltar o minuto seguinte. `OccurrenceCalculator.next` já usa comparação estrita. Verificar dois horários consecutivos com o mesmo medicamento, inclusive no último dia. |

## Reprodução e evidências de M2

1. Pelo formulário do app, cadastrar `LosartanaQA`, concentração `50 mg`, dose `1 comprimido`, segunda-feira, **20:45 e 20:46**, tratamento temporário de **26/10/2026 até 26/10/2026**. Revisão mostrou exatamente esses dados; confirmar. Notificações e alarmes exatos já estavam permitidos; repetição ligada, intervalo 10 min.
2. `dumpsys alarm` antes do disparo mostrou os dois `FIRE`, com `window=0`, para 20:45:00 e 20:46:00. Captura `build/review_m1_review.png` e arquivo `build/review_alarm_before.txt`.
3. Sem interagir com a notificação, o Logcat registrou `20:45:00.045 lembrete_emitido timeId=7 acao=FIRE repeticao=0`.
4. Às **20:45:11**, `dumpsys notification --noredact` não tinha notificação ativa do Genki, e `dumpsys alarm` não tinha `REPEAT` do teste nem o `FIRE` das 20:46. Permaneciam apenas os dois alarmes futuros dos medicamentos preexistentes. A captura da Hoje ainda mostrava os dois horários cadastrados. Evidências: `build/review_notification_after.txt`, `build/review_alarm_after.txt`, `build/review_m2_after.png`.
5. As evidências do item 4 foram coletadas **antes de qualquer edição posterior**. Uma edição às 20:45:58 substituiu os IDs dos horários e recriou o alarme das 20:46 (`timeId=10`, emitido às 20:46:03); esse disparo posterior não é o alarme original cancelado e não foi usado para concluir RV-M2-02.

Esses defeitos são distintos das simplificações aceitas: não dependem de reinício, ausência de permissão ou restauração de adiamento. O relógio/fuso do emulador não foi alterado.

## Verificação executada

- **Build e testes: passou.** `.\gradlew.bat assembleDebug test`, `BUILD SUCCESSFUL`; tarefas estavam `UP-TO-DATE`. XMLs confirmam 37 testes, zero falhas/erros: validador 15, ocorrências 9, textos 7, mapeamentos 4, instrumentação 2. Não foram adicionados testes nesta revisão.
- **Instalado em:** Pixel_6_2, Android 15/API 35, `emulator-5556`, via `installDebug`.
- **Capturas versus esperado:** três abas funcionam; tema verde e indicador ativo; Hoje com data civil por extenso, próximo horário e lista; Ajustes com repetição, intervalo e permissões. Cadastro em três etapas e revisão mostram concentração separada da dose, horários, dias e período. Detalhes e edição permanecem legíveis nas capturas em fonte 1,5×, com rolagem no formulário.
- **M1:** erros de nome/dose vazios; dias específicos sem seleção; horário ausente e duplicado; duração ausente, término vazio e anterior ao início; término igual ao início aceito; revisão e Alterar/Concluir alteração; salvar/listar/detalhar; fechar e reabrir preserva cadastro; edição e aviso de descarte; atualização salva; desativar/ativar cancela/reagenda; opção desligada preservada ao salvar edição; exclusão com confirmação. `GenkiEval` registrou cadastro, salvamento, edição e exclusão.
- **Inspeção:** Kotlin/Compose/Room e injeção manual; cadastro/edição exigem resultado do `DraftValidator`; liga/desliga usa a mutação específica prevista na ESPEC. Datas persistidas como `LocalDate`; UTC no adaptador do DatePicker apenas representa o dia selecionado. Sem histórico de ingestão nem novas dependências fora do escopo. Voz/OCR ainda não implementados.
- **Erros no Logcat:** nenhum crash/exceção do app observado. Houve um `E FrameTracker: force finish cuj, time out: J<IME_INSETS_HIDE_ANIMATION...>` às 20:46:08, durante o teste com teclado; não impediu o fluxo. Os dois defeitos de M2 são falhas funcionais sem crash.
- **Reinício do emulador:** executado `adb reboot`; após o boot/desbloqueio, antes de iniciar a Activity manualmente, `dumpsys alarm` já mostrava os `FIRE` exatos dos medicamentos preexistentes para 27/10 às 08:00 e 19:30. `GenkiEval` registrou a inicialização automática do processo às 20:48:27. Depois, o app foi reaberto em Hoje. Isso verifica o reagendamento futuro após boot neste emulador; não verifica restauração de repetição/adiamento nem substitui o aparelho físico.

As capturas e os dumps em `build/review_*` são evidências locais não versionadas. Apenas o medicamento criado nesta revisão foi editado/excluído; Metformina e Losartana preexistentes foram preservadas. Fonte restaurada para 1,0×. Alterações locais preexistentes em `.idea/` e `.claude/settings.local.json` não foram editadas pela revisão.

## Limites e próximo passo

Não verificado nesta sessão: aparelho físico/alarme em aparelho real, Doze/economia de bateria, troca de fuso, TalkBack, fonte ≥1,8×, fluxo de primeira solicitação/recusa de permissões (já concedidas), lista vazia (havia cadastros preexistentes), ciclo completo de três repetições, retorno após Adiar e Entendi/deslizar. Os testes prévios de Claude Code continuam identificados separadamente no handoff e não se tornam verificação independente por este relatório. Voz e câmera são M3/M4, ainda não iniciados. Fonte 1,5× foi conferida apenas nas telas descritas, não em todos os estados.

Próximo passo proposto: corrigir RV-M2-01/02, acrescentar cobertura da decisão de reagendamento na lógica pura e repetir os dois cenários no emulador; depois completar a regressão de repetição/adiamento/reconhecimento e conferir no aparelho do autor. Nenhuma nova decisão de produto foi assumida.
