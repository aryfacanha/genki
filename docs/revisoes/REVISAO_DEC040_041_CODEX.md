# Revisão — DEC-040 e DEC-041

28/10/2026 — Codex (GPT-6). Partida: `main`/`8aab297b302b29dc8be0e5cbaef1b3f7e16e3128`.
`git pull`: atualizado, sem conflito. Alvo: `9ee4def..a030860` (`352527e`, `eb67d7d`, `a030860`), conforme roteiro `8aab297`.

**Resultado: dois S2 e dois S3 abertos; nenhum S1 observado.** Build passou; **137 testes executados**, zero falhas/erros.
Reparos RV-DVC-01…05 anteriores permanecem fechados; não foi aberto outro ciclo amplo no interpretador.

## Escopo e referências

Lidos ESPEC, DEC-040/041, proposta da introdução aprovada, bateria do autor e roteiro desta revisão. Reabertos no
Drive a proposta oficial (§2.1) e os requisitos oficiais, incluindo RF11–RF19, RF24/25 e RNF01–RNF11. A introdução
é acréscimo fora da proposta, aceito pelo autor e já registrado na DEC-041; a monografia ainda precisa integrar
essa divergência. A revisão técnica não é aceite do autor nem integração acadêmica.

Manifest, Gradle e dependências não mudaram no intervalo. `chooseDose` altera só o rascunho; o salvamento continua
via `DraftValidator`. Não foram encontrados caminho novo até o banco, inferência de dose por OCR, tomada,
concentração confundida com dose ou mudança de regra de data civil. Contrato da API de fala consultado na
[referência oficial de SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer).

## Achados

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-4041-01 | S2 | `app/src/main/java/br/edu/ifrs/genki/voice/ui/VoiceScreen.kt:149` e `:571` | **Escuta automática abre por trás da confirmação de tirar medicamento.** O X só muda `confirm`; não interrompe a pergunta falada nem informa o diálogo ao callback. Se a fala termina com “Tirar Losartana?” aberto, os critérios de fase/lifecycle continuam válidos e `input.start()` é chamado. A pergunta pode receber uma resposta enquanto a interface está aguardando a decisão do diálogo. | Ao abrir a confirmação, interromper fala/escuta e invalidar a abertura automática pendente; impedir escuta enquanto houver diálogo. Aplicar a mesma proteção aos demais diálogos de saída. |
| RV-4041-02 | S2 | `app/src/main/java/br/edu/ifrs/genki/ui/settings/SettingsScreen.kt:53` | **Permissão fica desatualizada ao mudar o canal de som.** `notificationsAllowed` depende só de `resumes`, embora `Notifications.canNotify` agora dependa de `alarmSound`. Bloquear apenas `lembretes_alarme`, voltar a Ajustes com som de notificação e escolher Como alarme mantém “Permitidas”, sem ação de recuperação. O canal ativo está bloqueado e os lembretes não aparecem. Sair e voltar atualiza corretamente. | Incluir o canal/som selecionado na invalidação da leitura de permissões, além de reler ao retornar dos Ajustes do Android. |
| RV-4041-03 | S3 | `app/src/main/java/br/edu/ifrs/genki/ui/intro/IntroScreen.kt:192` | **Recusa da primeira solicitação abre os Ajustes do Android imediatamente.** O callback ignora a resposta negativa e chama `settingsIntent` sempre que `canNotify` é falso. Em vez de permanecer na introdução com a alternativa de permitir depois, a pessoa é enviada a outra tela justamente após negar. Voltar ao app permite continuar. | Respeitar a primeira recusa e manter a explicação na introdução; abrir os ajustes do sistema mediante ação explícita, quando necessário. |
| RV-4041-04 | S3 | `app/src/main/java/br/edu/ifrs/genki/ui/navigation/GenkiApp.kt:130`, `:157` e `:257` | **Concluir a primeira introdução deixa a âncora das abas apontando para IntroRoute, já retirada da pilha.** `start` continua IntroRoute durante esse processo, mas a conclusão esvazia a pilha e navega à Hoje. As abas tentam `popUpTo(findStartDestination())`, não encontram IntroRoute e passam a acumular telas. Reproduzido: concluir → Medicamentos → Ajustes → Voltar retorna a Medicamentos, com aviso de destino ausente no Logcat. | Normalizar a raiz/âncora da navegação para Hoje após concluir/pular a introdução, mantendo a política de pilha das abas. |

Evidência de RV-4041-01: pergunta falada iniciada às **18:48:44**; diálogo aberto às **18:48:46**; callback
iniciou escuta às **18:48:49.618** (`review_start generation=3`, seguido de `voz_ouvindo pergunta=TIMES`). A árvore
continuava mostrando “Tirar Losartana?”; ao cancelar o diálogo, a pergunta mostrava “Ouvindo…”. Entrada inicial e
reconhecedor foram simulados; o fim da síntese usou o callback real do TTS do emulador. Não se afirma captura de
áudio real nesse ensaio.

Evidência de RV-4041-02: canal de alarme com `mImportance=0`, `alarm_sound=true` e Ajustes ainda em “Permitidas”.
Na Hoje, o aviso aparece corretamente; ao retornar a Ajustes, o estado passa a “Não permitidas” e o botão abre
o canal `lembretes_alarme` correto. O defeito está na atualização da tela, não na escolha do canal pelo helper.

Evidência de RV-4041-04: `NavController` registrou duas vezes `Ignoring popBackStack ... was not found on the
current back stack` às 18:45:34/37. A reprodução ocorreu sem reiniciar entre concluir a primeira introdução e
trocar as abas.

## Executado e verificado

- `.\gradlew.bat assembleDebug test`: passou antes e depois de restaurar o código. `.\gradlew.bat testDebugUnitTest --rerun-tasks`: **137 testes executados, zero falhas/erros** nos XMLs, sem testes temporários na suíte. Aviso preexistente de condição sempre falsa em `VoiceParser.kt:357`, fora do diff desta revisão.
- Emulador Pixel_6_2, API 35; todas as operações direcionadas a `emulator-5554`. A31 não operado. Fontes 1,0× e 2,0× na introdução/voz; telas roláveis quando necessário, textos observados legíveis.
- **Introdução:** primeiro acesso simulado alterando apenas `intro_done` na instalação existente, preservando medicamentos; Pular → Hoje → reinício não repete; Ver a introdução de novo; quatro páginas, retorno pela tecla Voltar, personalização e conclusão. Permissão concedida prévia e primeira recusa verificadas. Não foi instalação totalmente limpa com banco vazio.
- **Dose na voz/E15:** Escolher a quantidade → ml → + → 1,5 → Confirmar quantidade: cartão atualizado e pergunta dos dias. Confirmação enviada ao TTS: “Anotei 1,5 ml. Em quais dias…”. Sem gravação nesse caso.
- **Escuta/E17–E20:** após terminar a pergunta, houve chamada automática ao reconhecedor simulado; erro 7 simulado encerrou a escuta, falou a mensagem e não abriu outra sozinho. Escolher quantidade/relógio e responder por toque interrompem a abertura/escuta anterior. `autoListen=false` persistiu; numa nova sessão, só houve o acionamento manual inicial, sem outro após a pergunta. Isso verifica a orquestração, não o silêncio/ruído de um reconhecedor real.
- **Confirmação/E19:** registros do texto passado ao TTS incluem nome, dose, dias, frequência, horários e uso contínuo antes da próxima pergunta/revisão; fala inicial com dois medicamentos identificou ambos. Não se verificou áudio audível nem pronúncia.
- **X/E16:** confirmação pode ser cancelada; tirar o único rascunho volta ao início; com Losartana/Metformina, tirar Losartana preserva Metformina e passa à pergunta dela. Rascunhos descartados. A corrida do diálogo está aberta em RV-4041-01.
- **Horários/E12:** primeiro 07:30 gerou 07:30/15:30/23:30; tentar substituir 23:30 por 07:30 mostrou erro de repetição e bloqueou confirmação; trocar por 22:00 preservou 07:30/15:30/22:00 até a revisão. Sem salvar esse rascunho.
- **“Use contínuo”/E14:** frase completa com `use continuo` chegou à revisão com um único medicamento e uso contínuo; não criou outro medicamento “Uso”. Também coberto pela suíte existente.
- **Alarme/D11 e adiar/D4:** criado `Revisaodec` pelo cadastro por voz e revisão, com 18:57; depois do agendamento, som trocado de notificação para alarme. No APK original reinstalado, disparou às **18:57:00.071**, `timeId=39`, pelo canal **`lembretes_alarme`**; atributos do canal: `USAGE_ALARM`, `alarm_alert`. Adiar 10 min gerou cartão verde-claro com borda, sem novo botão Adiar; Entendi retirou o aviso. Não se aguardou o retorno do adiamento nem se ouviu o som no silencioso.
- **Regressão G1–G7:** primeira unidade inicia em 1; meio comprimido, 2 entre comprimido/cápsula, meio → cápsula vazia, texto livre; 120 gotas/+ inalterado/−119; 0,25 ml/+0,75/−0,25; dose persistida como texto livre `1/2 comprimido`, reaberta como Comprimido/1/2 e + chega a 1. Alteração final descartada; medicamento de teste excluído.
- **Estados vazios:** inspecionado `EmptyMessage` centralizado no Box das duas telas. Não se esvaziou o banco original para repetir A3 visualmente. Cartão adiado verificado por captura real; não há histórico de ingestão.

Evidências locais ignoradas pelo Git: `build/review_dec40_*.xml`, `build/review_dec41_*.xml`, capturas
`dec40_voice20.png`, `dec41_intro20.png`, `dec41_snoozed.png`, `dec4041_log_final.txt` e
`dec41_notification_fire_dump.txt`. Arquivos originais de voz guardados em `build/dec4041_*_original.kt`.

## Restauração e limites

Instrumentação temporária em `SpeechInput.start()` (entrada inicial, escuta e erro simulados) e log do texto do
TTS retirados. SHA-256 restaurados: SpeechInput `13A0EA32404AC79212857AE5A344B873ADFE82D5807A38B62D1E6BB09AA415A7`;
Speaker `8D1C52519C7B949502EDA802280A0D57E6B6FB51A90655A4482214C7ACB68CFD`. `app/src` sem diff final.
APK original reinstalado só no emulador; arquivo de entrada simulada removido.

`Revisaodec` excluído e seus lembretes cancelados; lista final com Metformina/Losartana anteriores. Rascunhos
descartados. Preferências originais restauradas e relidas (notificação comum, intervalo 10 min, introdução já
vista, escuta automática no padrão por ausência da chave); fonte 1,0×. POST_NOTIFICATIONS concedida e RECORD_AUDIO
negada, como antes; canal de alarme novamente habilitado (importância 4). Relógio e mídia não alterados. Emulador
na Hoje; nenhum crash AndroidRuntime observado no log consultado. Sem commit/push ou correção permanente de código.

Não verificados: A31, fala/reconhecimento reais, TTS audível, TalkBack, ruído e segunda tentativa automática após
fala não entendida, todas as corridas de lifecycle/segundo plano, ausência de leitor pt-BR, banco totalmente vazio,
câmera, Doze, silencioso audível e ciclo completo de repetições/adiamento. As capturas em 2,0× não são auditoria de
todas as telas. Próximo: reparar/conferir os quatro achados pontualmente; depois manter o aceite do autor e as
verificações técnicas previstas separados desta revisão.
