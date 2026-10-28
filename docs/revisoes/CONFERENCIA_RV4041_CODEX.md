# Conferência pontual — reparos RV-4041-01…04

28/10/2026 — Codex (GPT-6.1). Partida: `main`/`5a7ce9e364f9e6a2a70bfa6de24367868c001b28`.
Alvo: `f4a3dff28f6a7b6b1f228b1d56ab594e492de527`; `git diff f4a3dff HEAD -- app` vazio.
Roteiro: `ROTEIRO_CONFERENCIA_RV4041_CODEX.md`, handoff §5/6 e revisão original `REVISAO_DEC040_041_CODEX.md`.

**Resultado: quatro achados fechados na cobertura executada. Nenhum achado novo S1–S4 confirmado no reparo.**
Build final e **137 testes executados, zero falhas, erros ou ignorados**. Emulador API 35; A31 não operado.
Sem correção permanente de código/configuração, commit ou push. Toque real em notificação durante a introdução
não repetido; demais limites abaixo.

## Partida e referências

`git pull` tentado: `cannot open '.git/FETCH_HEAD': Permission denied`. Falta escrita nos metadados Git;
não foi possível confirmar atualização remota. O alvo local está disponível e o HEAD permaneceu na partida.
Alterações preexistentes em `.idea/` e `.claude/settings.local.json` preservadas.

Lidos ESTADO, DEC-026/040/041, ESPEC, mapa de requisitos, handoff, roteiro e revisão original. Reabertos pelo
conector Google Drive a [proposta oficial](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view)
e o texto da [planilha oficial de requisitos](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit),
em particular RF15–19, RF24/25 e RNF01–05. Confirmação, correção e alternativas por toque são compromissos;
DEC-040/041 são decisões já aceitas pelo autor. A introdução continua sendo acréscimo fora da proposta,
pendente de integração à monografia. Esta conferência não é aceite no aparelho nem nova decisão de produto.

O diff altera somente as quatro telas indicadas, sem novo caminho até o banco, dependência, permissão,
regra de data, OCR ou histórico de ingestão. Gravação continua via `DraftValidator`.

## Resultado por ID

| ID | Gravidade original | Situação | Evidência |
| --- | --- | --- | --- |
| RV-4041-01 | S2 | **Fechado** | `VoiceScreen.kt:177,221,330,573`: X passa por `onQuiet` antes do diálogo; sair com rascunhos também para fala e cancela escuta. No emulador, X e Voltar durante TTS interromperam callback pendente; diálogos mantidos por mais de 14 s sem nova escuta. Cancelar/Continuar cadastro deixou a pergunta parada; microfone voltou a ouvir só após toque. X com escuta já aberta também a encerrou. |
| RV-4041-02 | S2 | **Fechado** | `SettingsScreen.kt:54` inclui `settings.alarmSound` nas chaves de `remember`. Som de notificação e canal de alarme com importância 0: “Permitidas” → tocar Como alarme → “Não permitidas” sem sair da tela. Botão abriu “Lembretes como alarme”; voltar a Como notificação recuperou “Permitidas”. Canal restaurado a importância 4. |
| RV-4041-03 | S3 | **Fechado** | `IntroScreen.kt:193–211`: callback apenas invalida a leitura; `asked` impede novo pedido nesta página. POST_NOTIFICATIONS revogada e flags user-set/user-fixed limpas; Ajustes → rever introdução → página 4 → pedido Android → Don’t allow: permaneceu em “4 de 4”. Segundo toque abriu `AppNotificationSettingsActivity` do Genki. Permissão e flags originais restauradas. |
| RV-4041-04 | S3 | **Fechado** | `GenkiApp.kt:103,157,208` ancora na Hoje. Com `intro_done=false`, tanto Pular quanto Começar → Medicamentos → Ajustes → Voltar retornaram à Hoje; outro Voltar foi ao launcher. Rever introdução → Começar → mesmas abas também passou. Logs desses percursos sem `Ignoring popBackStack`. Atalho Resolver em Ajustes da Hoje abre Ajustes e Voltar retorna à Hoje. Toque real em notificação na introdução não verificado. |

## Evidências e regressão imediata

- **Fala → X:** início real do TTS às **19:41:13.062**; `speaker.stop()` às **19:41:13.205**, com `pendingDone=true`, seguido de `input_cancel active=false`. “Tirar Losartana?” continuou aberto após a espera; nenhum `input_start` adicional. Cancelar mostrou “Toque no microfone para responder”. Toque manual posterior mostrou “Ouvindo…”.
- **Fala → Voltar:** início real às **19:42:08.568**; parada às **19:42:08.703**, também com callback pendente. “Descartar cadastro?” permaneceu aberto além do fim esperado, sem escuta; Continuar cadastro manteve a pergunta parada. Em ambos os casos a síntese foi interrompida, portanto não se exige um `onDone` após a parada. A fala equivalente sem interrupção durou cerca de 7,3 s no ensaio de controle.
- **E16:** tirar o único rascunho retornou ao início da voz. Com Losartana e Metformina, tirar Losartana, da pergunta atual, preservou só Metformina e passou a perguntar os dias dela. Rascunhos remanescentes removidos/descartados; nenhum medicamento de teste salvo.
- **E17:** sem diálogo, o fim real do TTS abriu a entrada simulada. Após a remoção do primeiro medicamento, `tts_ended` às **19:43:05.053** → `input_start` às **19:43:05.055**, seguido de `voz_ouvindo pergunta=DAYS`. Verifica a orquestração, não captura de áudio nem timeout de silêncio real.
- **D4 do roteiro de conferência (atalho da Hoje):** só o canal de alarme bloqueado e esse som selecionado; aviso → Resolver em Ajustes → tela Ajustes → Voltar → Hoje. Aqui D4 significa o atalho solicitado neste roteiro; o D4 da bateria completa é Adiar e não foi repetido.
- **Build:** `.\gradlew.bat assembleDebug test` passou inicialmente com testes reutilizados; `.\gradlew.bat assembleDebug test --rerun-tasks` passou com 46 tarefas executadas e 137 testes nos XMLs. Após retirar a instrumentação, novo `assembleDebug test` passou, com `testDebugUnitTest` executado e os mesmos totais. Aviso preexistente de condição sempre falsa em `VoiceParser.kt:357`, fora do reparo.

Evidências locais ignoradas pelo Git em `build/rv4041/`: XMLs `01_*`, `02_*`, `03_*`,
`04_skip_intro_fresh.xml`, `04_skip_back_home_fresh.xml`, `04_start_*`, `04_reintro_home_final.xml`,
`E16_*`, `E17_*`, `D4_*`, `final_*`; logs `01_x_tts_log.txt`, `01_back_log.txt`, `E17_log.txt`,
`04_skip_log_fresh.txt`, `04_start_log.txt`, `04_reintro_log.txt` e `log_final.txt`;
preferências/permissões/canais antes e depois. O primeiro dump de Pular ficou desatualizado; foi descartado
como evidência e repetido nos arquivos com sufixo `fresh`.

## Restauração e limites

Somente `emulator-5554` (AVD Pixel_6_2, `sdk_gphone64_x86_64`, API 35). Entrada temporária em `SpeechInput.start()`
entregou texto e manteve escuta simulada até cancelamento; `Speaker` ganhou somente logs. TTS e callback de fim
foram reais. Instrumentação retirada, APK original recompilado/reinstalado apenas no emulador e arquivo de
entrada removido. SHA-256 restaurados, idênticos à partida:

- `SpeechInput.kt`: `13A0EA32404AC79212857AE5A344B873ADFE82D5807A38B62D1E6BB09AA415A7`.
- `Speaker.kt`: `8D1C52519C7B949502EDA802280A0D57E6B6FB51A90655A4482214C7ACB68CFD`.

`git diff -- app` vazio. Preferências `genki_settings` restauradas e comparadas por chave/tipo/valor:
som de notificação, intervalo 10 min, introdução já vista; escuta automática e repetição mantidas nos padrões
por ausência das chaves. POST_NOTIFICATIONS concedida com USER_SET; RECORD_AUDIO negada sem USER_SET/USER_FIXED;
CAMERA concedida, inalterada. Canais `lembretes` e `lembretes_alarme` com importância 4 e campos conferidos
iguais aos iniciais. Fonte 1,0×, relógio e mídia não alterados. Lista final mantém Metformina/Losartana anteriores;
nenhuma gravação de medicamento pelo revisor. Sem crash do app observado nos logs consultados.

**Limite da restauração:** o aviso de Losartana das 19:30 visível na partida deixou de aparecer durante as
paradas/reaberturas do app; `genki_alerts` terminou vazio. Não foi guardada cópia inicial desse estado transitório
nem ele foi reconstituído artificialmente. A restauração conferida acima cobre ajustes, permissões e canais;
não se afirma preservação do aviso/repetições que já estavam pendentes.

Não verificados: toque real em notificação com a introdução aberta, fala/reconhecimento reais, TTS audível,
TalkBack, silêncio/ruído e demais corridas de lifecycle, A31, fonte ampliada nesta rodada, câmera ou ciclo de
lembretes. A primeira abertura foi simulada apenas com `intro_done=false`, mantendo o banco existente.
Próximo: aceite do autor no A31 e verificações técnicas previstas, sem abrir revisão ampla do interpretador.
