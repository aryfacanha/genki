# HANDOFF — App Android Genki (Claude Code ↔ Codex)

Atualizado em: 27/10/2026, por Claude Code (Opus 5.5) — melhorias do autor (DEC-029); antes: 26/10, Codex (GPT-6) — revisão de `a590fa5`: RV-M2-01/02 fechados; antes: Claude Code (Opus 5.5) — correção dos achados; antes: Codex — revisão M0/M1/M2; antes: Claude Code — M2, M1 e criação deste arquivo (DEC-026).
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
| M2 — melhorias do autor (DEC-029) | Implementado e compilado em 27/10; instalado no Pixel 8a (Android 17) do autor, tela não conferida (aparelho bloqueado). Cartão "Agora"/"Lembrete adiado" na Hoje, relógio sem horário repetido, término não anterior ao início, sem esmaecer. Teste do autor e revisão do Codex pendentes | `1ddd544`, `290b638`, `d592b8a` |
| M3 Voz | **Implementado e compilado; fluxo conferido no emulador (Pixel_6_2, API 35) com texto injetado no lugar do microfone (ajuste local, não commitado); voz real não testada.** Parte 1: `VoiceParser` + corpus (28). Parte 2 (27/10): "Como você quer cadastrar?" (Usar voz / Digitar; câmera entra no M4), `RegistrationSession`, tela de voz (`SpeechInput` com reconhecedor do aparelho e fallback para o do sistema, `Speaker` só com voz pt), perguntas do que falta com respostas por toque ("Todos os dias", "Uso contínuo", "Ainda não sei"), revisão de vários medicamentos com "Corrigir" por toque (formulário em `FormMode.FixDraft`), "Remover da lista", "Corrigir por voz", "Prefiro digitar" (1 rascunho → formulário preenchido; vários → revisão), `RECORD_AUDIO` e `<queries>` no manifest, `GenkiEval` (`voz_ouvindo`, `voz_resultado` com latência, `voz_interpretado`, `voz_erro`). Revisão do Codex e teste no aparelho pendentes | `4bebc3b`, `3200047` |
| M4 Câmera | **Implementado e compilado; conferido no emulador (Pixel_6_2, API 35) em 27/10.** "Usar câmera" na escolha; CameraX (pré-visualização + uma foto, sem gravar) → ML Kit com modelo latino embutido (sem internet e sem Google Play Services) → `OcrParser` (nome = linha mais longa sem algarismos entre as 3 maiores; concentração = 1º número + mg/mcg/g/ml/UI) → "Confira o que eu li" com nome e concentração editáveis (RF22) → "Continuar por voz" (perguntas a partir da dose) ou "Continuar digitando" (formulário preenchido); falha → "Tentar de novo" ou "Digitar". Dose, horários, dias e tratamento nunca vêm da foto (RF23, teste). Leitura real de embalagens no aparelho (20 ensaios) pendente; revisão do Codex pendente | `0c06d49` |

## 3. Verificado e não verificado

| Item | Situação |
| --- | --- |
| `.\gradlew.bat assembleDebug test` | Passa em `d592b8a` (Claude Code, 27/10; 48 testes, +5 `ActiveAlertTest`); antes em `f111670` (Claude Code, 26/10), `42a0d16` (Codex, 26/10) e `a590fa5` (Claude Code e Codex, 26/10; 43 testes sem falhas; na revisão Codex tarefas `UP-TO-DATE`, XMLs conferidos) |
| Testes unitários | 87 em `0c06d49` (+6 `OcrParserTest`); 81 em `3200047` (+5 `ActiveAlertTest`, +28 `VoiceParserTest`, +5 `VoiceQuestionsTest`); antes 43: `GenkiEvalTest` (2), `DraftValidatorTest` (15), `OccurrenceCalculatorTest` (9), `ReminderPlanTest` (6), `MedicationTextTest` (7), `MappersTest` (4). `FormViewModel` (etapas, voltar, "Alterar" da revisão) e os efeitos do `ReminderScheduler`/`ReminderReceiver` no Android sem teste automatizado (a decisão de agendamento está em `ReminderPlan`, testada) |
| Revisão da correção no emulador (Codex, 26/10, `a590fa5`) | Cadastro pela interface, último dia, 22:44/22:45: dois avisos ativos e repetições agendadas; novo processo executa `syncAll` e preserva os dois. Adiar no segundo remove seu aviso/repetição e agenda retorno +10 min, preservando o primeiro; Entendi no primeiro cancela seu aviso/repetição, preservando o adiamento do segundo. Desativar cancela o adiamento; exclusão com confirmação. Fonte e relógio mantidos; dados de teste removidos. Sem crash; três `FrameTracker` com teclado. Detalhes e limites em `revisoes/REVISAO_CORRECAO_M2_CODEX.md` |
| Correção RV-M2-01/02 no emulador (Pixel_6_2, API 35, 26/10, Claude Code) | Medicamento de teste cadastrado pelo formulário (todos os dias, tratamento de 26/10 até 26/10); como o relógio do diálogo só escolhe múltiplos de 5 min, os horários foram ajustados direto no banco (`run-as` + `sqlite3`) para **21:40 e 21:41** e o app reaberto (`syncAll`). `dumpsys alarm`: os dois `FIRE` exatos. 21:40 → aviso "Lembrete das 21:40" fica na tela e `REPEAT` 21:50 agendado; o `FIRE` das 21:41 continua (RV-M2-02). 21:41 → segundo aviso; os dois avisos ficam até interação (RV-M2-01). Repetições às 21:50 e 21:51 emitidas e as próximas (22:00, 22:01) agendadas. Excluir o medicamento cancelou alarmes e avisos. Evidências locais `build/fix_*` (não versionadas) |
| M1 no emulador (Pixel_6_2, API 35, 26/10) | Erros por etapa com as frases do protótipo; dias específicos; horário repetido → "Este horário já foi adicionado."; relógio 24 h; data de início = hoje; término vazio e anterior ao início; revisão com "Alterar" e "Concluir alteração"; salvar → aviso "Medicamento adicionado"; fechar e reabrir mantém os dados (RNF06); detalhes; desativar lembretes (preservado ao editar); edição com "Descartar alterações?"; exclusão com confirmação; fonte 1,5× no formulário. Capturas em `build/m1_*.png` (não versionadas) |
| M2 no emulador (Pixel_6_2, API 35, 26/10) | Hoje: data por extenso, "Próximo lembrete 19:30", lista do dia, "Os lembretes de hoje já passaram."; Ajustes: repetição, intervalo 10/20/30 sempre visível, estado das permissões; pedido explicado de notificação ao salvar; botão abre "Alarms & reminders" do sistema e, ao permitir, os alarmes viram exatos (`dumpsys alarm`: `window=0`). Lembrete às 19:30:00 com "Lembrete das 19:30 / Losartana 50 mg · 1 comprimido", "Entendi" e "Adiar 10 min"; repetições às 19:40 e 19:50 (3ª agendada); "Adiar" tira a notificação, cancela a repetição e agenda `FIRE_SNOOZED` em +10 min; aviso adiado às 20:03 com "(adiado)" e repetição; "Entendi" tira a notificação e cancela a repetição; "Desativar lembretes" cancela o alarme e "Ativar" reagenda; próxima ocorrência do dia seguinte agendada |
| Limitações do M2 | Repetições e adiamentos pendentes não voltam depois de reiniciar o aparelho (simplificação S2); ocorrência perdida com o aparelho desligado não é emitida depois; sem permissão de alarme exato o aviso pode atrasar; a lista do dia não mostra "Visto às" (DEC-028; o cartão do aviso pendente veio com a DEC-029); boot com reagendamento futuro conferido pelo Codex no emulador, aparelho físico não; "(adiado)" na repetição corrigido em `f214691`. RV-M2-01/02 fechados na revisão de `a590fa5`; limites da cobertura no relatório |
| M3 no emulador (Pixel_6_2, API 35, 27/10, Claude Code) | Microfone real: reconhecedor do aparelho sem pacote pt-BR (erro 13) → fallback para o reconhecedor do sistema, que ouviu em pt-BR **pela internet**; sem fala, "Não ouvi nada" (erro 7). Fluxo com texto injetado (ajuste local temporário em `SpeechInput`, removido antes do commit): "tomo losartana 50 mg às 8 e metformina 850 mg um comprimido às 8 e às 20" → dois cartões em "Entendi até agora" → perguntas de dose, dias (botão "Todos os dias"), duração ("uso contínuo"), dias da metformina ("de segunda a sexta") e duração ("por 10 dias") → revisão com os dois; "Corrigir" abriu o formulário preenchido e "Concluir correção" voltou com "2 comprimidos"; "na verdade a metformina é às 9" trocou 08:00 por 09:00; "Confirmar e salvar todos" → "2 medicamentos adicionados" e Hoje atualizada; frase sem sentido → "Não entendi. Tente de novo."; "Prefiro digitar" com um rascunho → etapa 1 preenchida. Sem crash. Dados de teste removidos. TTS não ouvido (emulador) |
| M4 no emulador (Pixel_6_2, API 35, 27/10, Claude Code) | Câmera traseira virtual: pré-visualização e foto funcionam; a cena não tem texto → "Não consegui ler a embalagem." e "Tentar de novo" volta à pré-visualização. Leitura com imagem de embalagem gerada no PC (ajuste local temporário que trocava a foto pela imagem, removido antes do commit): "LOSARTANA POTÁSSICA / 50 mg / 30 comprimidos revestidos / …" → nome "Losartana potássica", concentração "50 mg", **"30 comprimidos" não virou dose**, 2,2 s (`ocr_resultado`); "Continuar por voz" → "Quanto você toma de Losartana potássica por vez?"; "Prefiro digitar" → etapa 1 preenchida. Primeira versão saía da pré-visualização antes da foto terminar e a câmera era desligada ("Não consegui tirar a foto."); corrigido antes do commit. APK debug: 55,7 MB (modelo do ML Kit embutido) |
| Limitações do M4 | Nome pela regra da ESPEC §8 (pode pegar a marca do laboratório se ela for o texto grande sem número; a tela permite corrigir); "ml" conta como concentração (volume do frasco pode aparecer); só alfabeto latino; foto não é guardada; leitura de embalagens reais não medida |
| Limitações do M3 | Sem o pacote offline de pt-BR, o reconhecedor do sistema usa a internet (o app não usa rede; quem usa é o serviço do Android); fala só com voz pt instalada; "às 8" é sempre 08:00; "dia sim, dia não" e "por um mês" não são interpretados (a tela pergunta); palavra desconhecida pode virar nome (a revisão mostra); rascunho da voz sem dias abre no formulário em "Todos os dias", como o cadastro manual; rascunhos se perdem se o processo morrer (ESPEC §4); opção Câmera só no M4 |
| Limitações do M1 | "Adicionar medicamento" abre direto o formulário manual (a escolha Voz/Digitar/Câmera entra no M3, com `RegistrationSession`); calendário e relógio seguem o idioma do sistema (emulador em inglês); textos das telas em Kotlin, não em `strings.xml` (app só em português); na edição, trocar para "Tem data para terminar" mostra como início a data do cadastro (divergência #5) |
| App aberto em emulador/aparelho | Emulador Pixel_6_2 (API 35), 26/10, Claude Code. Aparelho físico: Pixel 8a (Android 17) do autor, teste manual do autor em 27/10 (achados viraram DEC-029). **No Pixel 8a os alarmes estavam inexatos** (`dumpsys alarm`: janelas de +15 min e +1 h): a permissão "Alarmes e lembretes" não estava concedida; ligar em Ajustes |
| Abas, cores, barra inferior | Verificados no emulador (capturas): abre em Hoje, três abas trocam, indicador verde-claro e negrito na ativa, sem roxo |
| Rótulo da barra com fonte ampliada | Inteiro até 2,0× desde `f214691` (encolhe até 8 sp, conferido no emulador em 27/10); antes cortava a partir de 1,8× |
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
8. **Relógio para testar lembretes** (sem root; conferido no emulador API 35 em 27/10): `adb shell settings put global auto_time 0`
   e `adb shell cmd alarm set-time <epoch em ms>` (ex.: 30 s antes de um horário cadastrado; o alarme toca na hora e,
   se o salto passar do horário, toca logo). Ao terminar: `cmd alarm set-time` com a hora real e `settings put global auto_time 1`.
   Em aparelho com Android antigo o comando pode não existir: mudar a hora pelos Ajustes do sistema.

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
- **M2 — DEC-029:** com um aviso tocando, a Hoje mostra o cartão "Agora" com "Entendi" e "Adiar N min" e, abaixo, "Depois: HH:MM · nome"; "Adiar" troca o cartão por "Lembrete adiado / Novo alerta às HH:MM / Horário original"; "Entendi" (no cartão ou na notificação) tira os dois; desativar/excluir tira o cartão. No relógio, escolher um horário já da lista mostra "Este horário já foi adicionado." e desabilita OK; no término, dias antes do início ficam desabilitados.
- **M3:** "Adicionar medicamento" abre "Como você quer cadastrar?"; "Usar voz" → microfone (pede permissão na 1ª vez) → falar "losartana 50 mg um comprimido às 8 todos os dias uso contínuo" → revisão direto; frase incompleta → "Entendi até agora" + pergunta escrita e falada; botões de resposta rápida; "Prefiro digitar" leva ao formulário preenchido; na revisão, "Corrigir" abre o formulário e volta; "Corrigir por voz"; "Confirmar e salvar" → aviso e Hoje. Logcat: `voz_ouvindo`, `voz_resultado`, `voz_interpretado`, `medicamento_salvo fonte=VOICE`. **Sem microfone no emulador:** o microfone do PC não chegou ao emulador em 27/10; para conferir o fluxo, injetar texto com um ajuste local em `SpeechInput.start()` e removê-lo antes do commit.
- **M4:** "Usar câmera" → permitir câmera → pré-visualização e "Tirar foto" → "Lendo a embalagem…" → "Confira o que eu li" (nome e concentração editáveis; nada de dose/horários) → "Continuar por voz" pergunta a dose; "Continuar digitando" abre o formulário preenchido; foto sem texto → "Não consegui ler a embalagem." com "Tentar de novo"/"Digitar". Logcat: `cadastro_iniciado fonte=CAMERA`, `ocr_resultado` (texto, nome, concentração, ms), `ocr_confirmado`, `medicamento_salvo fonte=CAMERA`. No emulador a câmera mostra uma sala virtual sem texto; para testar a leitura, trocar a foto por uma imagem com ajuste local e removê-lo antes do commit.

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

- 2026-10-27 — Claude Code — M4 (`0c06d49`): `camera/OcrParser` (+6 testes), `camera/ui/CameraViewModel` e `CameraScreen`, opção "Usar câmera", `CameraRoute`, `VoiceRoute(fromSession)` (voz começa pelo rascunho da câmera), `CameraIcon`. **Config:** `libs.versions.toml` com `camerax = 1.4.2` (camera-camera2, camera-lifecycle, camera-view) e `mlkitTextRecognition = 16.0.1` (`com.google.mlkit:text-recognition`, modelo embutido); manifest com `CAMERA` e `uses-feature camera.any required=false` — 87 testes passam; fluxo no emulador (§3) — pendente: embalagens reais no aparelho, revisão do Codex.
- 2026-10-27 — Claude Code — ajustes (`f214691`): repetição de aviso adiado mantém "(adiado)" (extra `snoozed` no `REPEAT`; conferido no emulador com o relógio: "Lembrete das 19:30 (adiado)" na 1ª repetição); `SpeechInput` ignora a segunda resposta de erro do serviço; rótulo da barra com `minFontSize = 8.sp` (inteiro em 2,0× no emulador) — build e testes passam.

- 2026-10-27 — Claude Code — §5 passo 8 (relógio pelo ADB). No emulador, relógio levado a 19:29:30: lembrete das 19:30 emitido às 19:30:00; Hoje com cartão "Agora" (Entendi / Adiar 10 min); "Adiar" → "Lembrete adiado / Novo alerta às 19:40 / Horário original: 19:30"; relógio a 19:40:40 → notificação "Lembrete das 19:30 (adiado)" e cartão "Agora" de novo; "Entendi" no cartão → `lembrete_reconhecido`. **DEC-029 conferida no emulador.** Relógio devolvido à hora real — pendente: mesmo roteiro no celular de teste (Samsung, a confirmar).

- 2026-10-27 — Claude Code — instalado `3200047` + `f2a7a05` no Pixel 8a do autor: o aparelho usa **GrapheneOS** (`akita`), sem serviços de fala do Google. O único `RecognitionService` é o do app Claude, sem padrão escolhido → `SpeechRecognizer: no selected voice recognition service` (erro 5) e nenhum reconhecedor no aparelho; TTS só `app.grapheneos.speechservices` (voz pt não conferida). `f2a7a05`: erro 5 mostra "Este aparelho não tem reconhecimento de voz configurado. Você pode digitar." — build e testes passam; mensagem não vista no aparelho — pendente: autor decidir o reconhecedor (instalar um serviço de reconhecimento e escolhê-lo como padrão, ou outro celular de teste).

- 2026-10-27 — Claude Code — M3 parte 2 (`3200047`): `ui/add/AddChoiceScreen`, `domain/RegistrationSession` (no `AppContainer`), `voice/VoiceQuestions` (+ teste), `voice/SpeechInput`, `voice/Speaker`, `voice/ui/VoiceViewModel` e `VoiceScreen`; `FormViewModel` com `FormMode` (New/Edit/FixDraft) e `FormFields.from(draft)`; rotas `AddChoiceRoute`, `VoiceRoute`, `AddMedicationRoute(fromSession)`, `FixDraftRoute`; `MicIcon` em `Components`. **Config:** manifest com `RECORD_AUDIO` e `<queries>` (RecognitionService, TTS_SERVICE) — 81 testes passam; fluxo completo no emulador com texto injetado (§3) — pendente: voz real e TTS no Pixel 8a, TalkBack, revisão do Codex.

- 2026-10-27 — Claude Code — M3 parte 1 (`4bebc3b`): `voice/VoiceParser.parse(texto, rascunhos, hoje, target, expecting)` e corpus `VoiceParserTest` (28); `MedicationDraft.everyHours` (zerado pelo validador). Decisões de interpretação registradas no KDoc: "às 8 e 20" = 08:20 (ESPEC §7), "às 8, 14 e 22" = três horários, "dia sim, dia não" e "por um mês" não são interpretados (a tela pergunta), "na verdade" troca o horário mais próximo e "só" troca todos — 76 testes passam — pendente: telas de voz e teste com o reconhecedor real no aparelho.

- 2026-10-27 — Claude Code — melhorias do autor (DEC-029): `NavHost` sem transições (`1ddd544`); `TimeDialog` recusa horário repetido e `DateDialog` com `SelectableDates` a partir do início (`290b638`); `domain/ActiveAlert` + `reminders/AlertStore` (SharedPreferences `genki_alerts`), receptor grava tocando/adiado e apaga no ACK, `ReminderScheduler.cancel` e boot limpam, `respond()` usa o mesmo PendingIntent da notificação; Hoje com cartões "Agora"/"Lembrete adiado" (`d592b8a`). Também commitados os docs da revisão Codex que estavam locais (`bbc650d`) — 48 testes passam; instalado no Pixel 8a, telas não conferidas (bloqueado) — pendente: teste do autor, revisão do Codex, permissão de alarme exato no Pixel 8a.

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
