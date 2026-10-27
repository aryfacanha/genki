# HANDOFF — App Android Genki (Claude Code ↔ Codex)

Atualizado em: 26/10/2026, por Codex (GPT-6) — revisão de `a590fa5`: RV-M2-01/02 fechados; antes: Claude Code (Opus 5.5) — correção dos achados; antes: Codex — revisão M0/M1/M2; antes: Claude Code — M2, M1 e criação deste arquivo (DEC-026).
Quem assumir: leia `AGENTS.md`, `docs/ESTADO.md` e este arquivo, confira o estado real (`git log`, build) e não confie só
neste texto. Ao terminar cada alteração, **atualize as seções 2, 3 e 7 (se mudaram) e acrescente uma linha no Diário**.
Este arquivo é o retrato técnico do código; o estado do projeto inteiro (prazos, CEP, monografia) fica no `ESTADO.md`.

## 1. O que é

App Android do Genki (Kotlin + Compose + Room), especificado em `docs/ESPEC_ARQUITETURA.md`.
Referência visual: protótipo web `D:\dev\genki-prototype` (github.com/aryfacanha/genki-prototype, `docs/HANDOFF.md`).
Divergências de dados/regras entre os dois: `docs/DIVERGENCIAS_PROTOTIPO.md` (até o autor decidir, vale a ESPEC — DEC-022).

## 2. Estado por marco

| Marco | Situação | Commits |
| --- | --- | --- |
| M0 Base | Implementado, compilado e aberto no emulador (Pixel_6_2, API 35) em 26/10: regressão da seção 7 passa. Tema e barra inferior alinhados ao protótipo `295212e`. Aceite manual do autor no aparelho (ESPEC §9) pendente | `c1f2b7e`, `cec21ab`, `53fe97a`, correção do rótulo (ver Diário) |
| M1 CRUD | Implementado, compilado e conferido no emulador em 26/10 (cadastro em 3 etapas + revisão, lista, detalhes, edição, exclusão; dados persistem ao reabrir). Decisões #2, #7, #12 aplicadas (DEC-027). Codex revisou em `42a0d16`, sem achados novos na cobertura do relatório. Aceite manual do autor no aparelho pendente | `f2ff130` |
| M2 Lembretes | Implementado e compilado; fluxo principal conferido pelo Claude Code em 26/10 (DEC-028). **RV-M2-01/02 corrigidos em `a590fa5` e fechados pelo Codex no emulador em 26/10**, sem novos achados na correção. Avisos do último dia/minutos consecutivos e reabertura com novo processo passam; Adiar/Entendi e cancelamento ao desativar conferidos. Retorno de Adiar, ciclo completo de três repetições/deslizar na revisão independente e aceite no aparelho continuam pendentes | `f111670`, `a590fa5`; `revisoes/REVISAO_CORRECAO_M2_CODEX.md` |
| M3 Voz | Não iniciado | — |
| M4 Câmera | Não iniciado | — |

## 3. Verificado e não verificado

| Item | Situação |
| --- | --- |
| `.\gradlew.bat assembleDebug test` | Passa em `f111670` (Claude Code, 26/10), `42a0d16` (Codex, 26/10) e `a590fa5` (Claude Code e Codex, 26/10; 43 testes sem falhas; na revisão Codex tarefas `UP-TO-DATE`, XMLs conferidos) |
| Testes unitários | 43: `GenkiEvalTest` (2), `DraftValidatorTest` (15), `OccurrenceCalculatorTest` (9), `ReminderPlanTest` (6), `MedicationTextTest` (7), `MappersTest` (4). `FormViewModel` (etapas, voltar, "Alterar" da revisão) e os efeitos do `ReminderScheduler`/`ReminderReceiver` no Android sem teste automatizado (a decisão de agendamento está em `ReminderPlan`, testada) |
| Revisão da correção no emulador (Codex, 26/10, `a590fa5`) | Cadastro pela interface, último dia, 22:44/22:45: dois avisos ativos e repetições agendadas; novo processo executa `syncAll` e preserva os dois. Adiar no segundo remove seu aviso/repetição e agenda retorno +10 min, preservando o primeiro; Entendi no primeiro cancela seu aviso/repetição, preservando o adiamento do segundo. Desativar cancela o adiamento; exclusão com confirmação. Fonte e relógio mantidos; dados de teste removidos. Sem crash; três `FrameTracker` com teclado. Detalhes e limites em `revisoes/REVISAO_CORRECAO_M2_CODEX.md` |
| Correção RV-M2-01/02 no emulador (Pixel_6_2, API 35, 26/10, Claude Code) | Medicamento de teste cadastrado pelo formulário (todos os dias, tratamento de 26/10 até 26/10); como o relógio do diálogo só escolhe múltiplos de 5 min, os horários foram ajustados direto no banco (`run-as` + `sqlite3`) para **21:40 e 21:41** e o app reaberto (`syncAll`). `dumpsys alarm`: os dois `FIRE` exatos. 21:40 → aviso "Lembrete das 21:40" fica na tela e `REPEAT` 21:50 agendado; o `FIRE` das 21:41 continua (RV-M2-02). 21:41 → segundo aviso; os dois avisos ficam até interação (RV-M2-01). Repetições às 21:50 e 21:51 emitidas e as próximas (22:00, 22:01) agendadas. Excluir o medicamento cancelou alarmes e avisos. Evidências locais `build/fix_*` (não versionadas) |
| M1 no emulador (Pixel_6_2, API 35, 26/10) | Erros por etapa com as frases do protótipo; dias específicos; horário repetido → "Este horário já foi adicionado."; relógio 24 h; data de início = hoje; término vazio e anterior ao início; revisão com "Alterar" e "Concluir alteração"; salvar → aviso "Medicamento adicionado"; fechar e reabrir mantém os dados (RNF06); detalhes; desativar lembretes (preservado ao editar); edição com "Descartar alterações?"; exclusão com confirmação; fonte 1,5× no formulário. Capturas em `build/m1_*.png` (não versionadas) |
| M2 no emulador (Pixel_6_2, API 35, 26/10) | Hoje: data por extenso, "Próximo lembrete 19:30", lista do dia, "Os lembretes de hoje já passaram."; Ajustes: repetição, intervalo 10/20/30 sempre visível, estado das permissões; pedido explicado de notificação ao salvar; botão abre "Alarms & reminders" do sistema e, ao permitir, os alarmes viram exatos (`dumpsys alarm`: `window=0`). Lembrete às 19:30:00 com "Lembrete das 19:30 / Losartana 50 mg · 1 comprimido", "Entendi" e "Adiar 10 min"; repetições às 19:40 e 19:50 (3ª agendada); "Adiar" tira a notificação, cancela a repetição e agenda `FIRE_SNOOZED` em +10 min; aviso adiado às 20:03 com "(adiado)" e repetição; "Entendi" tira a notificação e cancela a repetição; "Desativar lembretes" cancela o alarme e "Ativar" reagenda; próxima ocorrência do dia seguinte agendada |
| Limitações do M2 | Repetições e adiamentos pendentes não voltam depois de reiniciar o aparelho (simplificação S2); ocorrência perdida com o aparelho desligado não é emitida depois; sem permissão de alarme exato o aviso pode atrasar; a Hoje não mostra se um lembrete foi visto ou adiado (DEC-028); boot com reagendamento futuro conferido pelo Codex no emulador, aparelho físico não; a repetição de um aviso adiado volta ao título sem "(adiado)" (menor). RV-M2-01/02 fechados na revisão de `a590fa5`; limites da cobertura no relatório |
| Limitações do M1 | "Adicionar medicamento" abre direto o formulário manual (a escolha Voz/Digitar/Câmera entra no M3, com `RegistrationSession`); calendário e relógio seguem o idioma do sistema (emulador em inglês); textos das telas em Kotlin, não em `strings.xml` (app só em português); na edição, trocar para "Tem data para terminar" mostra como início a data do cadastro (divergência #5) |
| App aberto em emulador/aparelho | Emulador Pixel_6_2 (API 35), 26/10, Claude Code. Aparelho físico: não |
| Abas, cores, barra inferior | Verificados no emulador (capturas): abre em Hoje, três abas trocam, indicador verde-claro e negrito na ativa, sem roxo |
| Rótulo da barra com fonte ampliada | Inteiro até 1,5×; **a partir de 1,8× "Medicamentos" é cortado com reticências** (limitação: a largura da aba não comporta a palavra; o rótulo encolhe até 12 sp e depois corta) |
| `GenkiEval` no Logcat | Verificado no emulador (`app_iniciado`); nenhum erro do processo do app |
| Revisão independente (Codex) | 26/10: M0/M1 sem achados novos em `42a0d16`; dois S2 de M2 encontrados nessa revisão e **fechados na revisão da correção `a590fa5`**, sem novos achados de código. Relatórios `revisoes/REVISAO_M0_M1_M2_CODEX.md` e `revisoes/REVISAO_CORRECAO_M2_CODEX.md`; nenhum código alterado pelo revisor. Aceite no aparelho e regressão temporal completa continuam pendentes |

## 4. Como rodar

- Build e testes (PowerShell, na raiz): `.\gradlew.bat assembleDebug test`.
- Instalar: `.\gradlew.bat installDebug` com um aparelho em `adb devices`.
- Versões: AGP 9.4.1 (Kotlin 2.2.10 embutido), Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26 (DEC-011).
- Codex CLI (forma que funcionou nas revisões do protótipo): `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < roteiro.md`
  (roteiro via stdin). Ajuste o modelo se o CLI recusar.

## 5. Checagem no emulador (roteiro comum; skill `/testar-emulador` do Claude Code aponta para cá)

Checagem rápida automatizada. **Não substitui a verificação manual do autor** (ESPEC S5). Não altera código.

1. `adb devices` — se nenhum aparelho aparecer como "device", pare e peça ao autor para abrir o emulador
   no Device Manager ou conectar o celular.
2. `.\gradlew.bat assembleDebug test` — se falhar, mostre o erro e pare.
3. `.\gradlew.bat installDebug`
4. `adb logcat -c` e depois `adb shell am start -n br.edu.ifrs.genki/.MainActivity`; aguarde ~3 s.
5. Captura de tela (não use redirecionamento `>` no PowerShell, corrompe a imagem):
   `adb shell screencap -p /sdcard/genki_tela.png` e `adb pull /sdcard/genki_tela.png build/genki_tela.png`.
   Compare com o esperado para a tarefa e com o protótipo, se disponível.
6. Se o foco pedir navegação, use `adb shell input tap X Y` / `input text` e capture de novo.
7. `adb logcat -d *:E` (erros e crashes) e `adb logcat -d -s GenkiEval`.

Relatório, sempre neste formato:
- Build e testes: passou/falhou
- Instalado em: <modelo do aparelho/emulador e API>
- O que a captura mostra x o esperado
- Erros no logcat (ou "nenhum")
- NÃO verificado: voz, câmera, alarmes reais, leitor de tela, texto ampliado — verificação manual do autor

Se achar problema, descreva e pergunte se deve corrigir.

## 6. Revisão (roteiro comum; skill `/revisar` do Claude Code aponta para cá)

Alvo: o commit ou intervalo indicado (padrão: `git show HEAD`). Quem revisa **não altera código**, salvo pedido do autor.

1. Leia o diff do alvo, `docs/ESPEC_ARQUITETURA.md` (seções pertinentes) e as regras do `AGENTS.md`.
2. Rode `.\gradlew.bat assembleDebug test`.
3. Verifique, no mínimo:
   - escopo: só o RF/marco pedido; nada de Hilt, WorkManager, DataStore, rede, LLM;
   - toda gravação passa pelo `DraftValidator`;
   - concentração ≠ dose; OCR não preenche dose/horários/dias/tratamento;
   - nada de "Tomado" nem histórico de ingestão;
   - datas civis no fuso do aparelho;
   - textos e fluxo coerentes com o protótipo (`D:\dev\genki-prototype`, se disponível);
   - testes unitários da lógica pura existem e passam;
   - regressão rápida da seção 7 para os marcos já entregues.
4. Liste achados em tabela: ID, gravidade (S1 bloqueia · S2 erro relevante · S3 menor · S4 sugestão),
   arquivo:linha, problema, correção sugerida.
5. Diga o que não pôde verificar (aparelho, voz etc.).
6. Grave o relatório em `docs/revisoes/REVISAO_<marco ou pacote>_<ASSISTENTE>.md` e uma linha no Diário.

## 7. Regressão rápida (por marco entregue)

- **M0:** o app abre na aba Hoje; as três abas (Hoje, Medicamentos, Ajustes) trocam de tela; a aba ativa tem o fundo
  verde-claro atrás do ícone e o rótulo em negrito; nenhum texto roxo/lilás do Material padrão; build debug registra
  `GenkiEval` no Logcat (`adb logcat -d -s GenkiEval`).
- **M1:** Medicamentos vazia mostra "Nenhum medicamento cadastrado."; "Adicionar medicamento" → etapas 1–3 bloqueiam com erro em campo vazio; dois horários iguais → "Este horário já foi adicionado."; "Tem data para terminar" exige término não anterior ao início; revisão mostra tudo e "Alterar" volta só àquela etapa; "Confirmar e salvar" → cartão na lista e aviso acima do botão; fechar e reabrir o app mantém o cartão; detalhes → Editar (sair com mudança pergunta "Descartar alterações?"), Desativar lembretes (cartão mostra "Lembretes desligados"), Excluir pede confirmação. Logcat: `cadastro_iniciado`, `medicamento_salvo`, `medicamento_editado`, `medicamento_excluido`.
- **M2:** Hoje mostra a data por extenso, o próximo lembrete e a lista do dia (sem "Visto"/"Adiado"); Ajustes mostra repetição, intervalo e permissões; ao salvar o 1º medicamento sem permissão, aparece "Permitir notificações?"; com um horário 2–3 min à frente, a notificação chega no horário com "Entendi" e "Adiar N min"; sem resposta repete a cada N min até 3 vezes; "Adiar" volta em N min com "(adiado)"; "Entendi" (ou deslizar) some e cancela as repetições; "Desativar lembretes" cancela os alarmes (`adb shell dumpsys alarm | grep genki`). Logcat: `lembrete_emitido`, `lembrete_adiado`, `lembrete_reconhecido`.
- **M2 — casos adicionais da revisão Codex:** no último dia do tratamento, o aviso da última ocorrência deve permanecer até interação (RV-M2-01); dois horários do mesmo medicamento em minutos consecutivos devem manter os dois alarmes (RV-M2-02). Para configurar minutos consecutivos pelo cadastro, selecione os minutos e arraste o ponteiro entre as marcações do relógio; confira os valores na revisão antes de salvar (22:44/22:45 configurados assim pelo Codex em 26/10). Após reiniciar/desbloquear, conferir alarmes futuros antes de abrir a Activity (reagendamento conferido no emulador em 26/10).
- M3 em diante: acrescentar ao entregar cada marco.

## 8. Divisão de trabalho (DEC-026)

- **Claude Code implementa; Codex revisa** (mesma divisão do protótipo, decisão do autor de 21/10).
- **Modo só Codex:** quando o limite do Claude Code acabar, o Codex implementa seguindo este arquivo e escreve no Diário
  "modo só Codex — aguardando revisão". Ao voltar, o Claude Code revisa primeiro esse trabalho (seção 6) e só depois
  começa algo novo.
- Nunca os dois editando os mesmos arquivos ao mesmo tempo. Um marco (ou parte dele) por commit.
- Mudança de configuração (`.claude/`, skills, `AGENTS.md`, Gradle, versões, permissões do manifest) entra no Diário
  com o que mudou e por quê, para o outro assistente saber.
- Push só com autorização do autor.

## Diário (acrescente no topo; uma linha por alteração: data — agente — o que mudou — verificado — pendente)

- 2026-10-26 — Codex (GPT-6) — revisão de `a590fa5`, `revisoes/REVISAO_CORRECAO_M2_CODEX.md`: **RV-M2-01/02 fechados**, sem novos achados de código; atualizado este handoff (§2/3/7), estado, entregas, registro de IA e nota no relatório anterior; §7 passa a configurar minutos consecutivos por arraste no TimePicker — build passa, 43 testes existentes sem falhas (`UP-TO-DATE`), último dia 22:44/22:45, avisos/repetições preservados com novo processo, Adiar/Entendi isolados por horário e cancelamento ao desativar; fixture removida; sem mudança de código/configuração — pendente: retorno de Adiar, ciclo completo das três repetições/deslizar na revisão independente; aparelho físico, Doze e TalkBack.
- 2026-10-26 — Claude Code — correção RV-M2-01/02 (RF09, RF24): `domain/ReminderPlan` (Schedule / NoMoreOccurrences / Disabled); `ReminderScheduler.sync` só cancela tudo com lembretes desligados, e tratamento encerrado cancela apenas o próximo `FIRE`, mantendo aviso e repetições; `syncFired` reagenda só o horário que tocou (+1 min) no lugar de `sync(entity, now + 1 min)`. Também evita que a abertura do app (`syncAll`) apague o aviso da última ocorrência — 43 testes passam (`ReminderPlanTest` +6); cenários 21:40/21:41 no último dia conferidos no emulador (§3) — pendente: nova revisão do Codex; regressão completa de Adiar/Entendi; aparelho físico.
- 2026-10-26 — Codex (GPT-6) — revisão independente de M0/M1/M2 em `main`/`42a0d16`, relatório `revisoes/REVISAO_M0_M1_M2_CODEX.md`; atualizados este handoff, estado, entregas e registro de IA; nenhum código/configuração alterado — build/testes passam (37, tarefas `UP-TO-DATE`), regressão de CRUD no Pixel_6_2 API 35, fonte 1,5× em detalhes/edição, boot reagenda alarmes futuros; **dois S2 de M2 reproduzidos** (aviso final cancelado e perda do horário no minuto seguinte); dados de teste removidos e fonte restaurada — pendente: correção e nova revisão de RV-M2-01/02, repetição/Adiar/Entendi completos nesta revisão independente, aparelho físico e TalkBack.
- 2026-10-26 — Claude Code — M2 (`f111670`): `OccurrenceCalculator`, `Settings` (SharedPreferences), `reminders/` (`ReminderScheduler`, `ReminderReceiver` FIRE/REPEAT/ACK/SNOOZE/FIRE_SNOOZED, `BootReceiver`, `Notifications`), repositório reagenda a cada mutação e cancela pelos ids antigos, telas Hoje e Ajustes, pedido de notificação ao salvar; DEC-028 (#13, #14, #16). **Config:** manifest com `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED` e dois receivers; `res/drawable/ic_notification.xml` — 37 testes passam; lembrete real conferido no emulador — pendente: revisão do Codex; aparelho físico; reinício do aparelho; TalkBack.
- 2026-10-26 — Claude Code — M1 (`f2ff130`): `domain/` (modelo, rascunho, `DraftValidator` com `DUPLICATE_TIME`), `data/` (Room, DAO, repositório só com `Valid`), telas de lista, detalhes, formulário em etapas + revisão e edição; navegação com rotas `DetailRoute`, `AddMedicationRoute`, `EditMedicationRoute`, barra inferior só nas abas e aviso (snackbar) no `Scaffold`; DEC-027 (#2, #7, #12) — 27 testes passam; fluxo completo conferido no emulador — pendente: revisão do Codex; aceite do autor no aparelho; TalkBack.
- 2026-10-26 — Claude Code — `/testar-emulador` no Pixel_6_2 (API 35): regressão M0 passa; com fonte 1,3× "Medicamentos" era cortado na barra inferior → rótulo com `autoSize` (16 sp → mín. 12 sp) em `GenkiApp.kt`, a pedido do autor — build, testes e emulador em 1,0×/1,3×/1,5× OK — pendente: corte persiste a partir de 1,8×; TalkBack não testado.
- 2026-10-26 — Claude Code — cria este arquivo, a seção "Implementação e revisão" no `AGENTS.md` e `docs/revisoes/`; skills `/revisar` e `/testar-emulador` passam a apontar para as seções 5 e 6 (DEC-026) — só documentação — pendente: primeira revisão pelo Codex.
- 2026-10-26 — Claude Code — config: skills `/testar-emulador` e `/revisar` em `.claude/skills/` (texto do autor; só o Claude Code as lê) (`19fcb3f`) — não executadas — nenhuma.
- 2026-10-26 — Claude Code — tema e barra inferior alinhados ao protótipo (`53fe97a`); divergências em `docs/DIVERGENCIAS_PROTOTIPO.md` (`0f73cb0`) — build e testes passam — não visto no aparelho.
- 2026-10-26 — Claude Code — M0: estrutura base (`cec21ab`) sobre o template do autor (`c1f2b7e`) — build e testes passam — não aberto no aparelho.
