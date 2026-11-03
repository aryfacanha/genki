# Terceira revisão da correção de M3/M4 — Codex

Data: 27/10/2026. Assistente: Codex (GPT-6).
Partida: `main`, `b56dd661df9a2b100b1d533587bbc1d335d5eaa8`; `git pull`: atualizado.
Alvos: `fac6a0a`, `8191c25`, `d7b8c63`, `24888ec`, `caafe29`, conforme o roteiro atualizado.

## Resultado

**RV-M3C-03 e RV-M3D-01 fechados. RV-M3-02 permanece parcialmente corrigido, com um S2 residual diretamente
relacionado à interpretação de “na verdade” depois de uma lista de dias.** As datas inválidas e os anos explícitos
passaram nos reprodutores. Não foi encontrada gravação sem revisão na cobertura executada.

Nas mudanças DEC-031/033, registrado **um S3 novo, RV-M3E-01**: “o nome é X” é ignorado durante pergunta comum;
o botão explícito de corrigir funciona como alternativa. Os demais fluxos exercitados passaram: Manter/Desfazer,
correção por toque durante a correção pendente, respostas por botão/texto, navegação da notificação, grupos de avisos
e remoção do horário atual de “Próximo lembrete”.

**Build e 110 testes existentes passam; 36 reprodutores anteriores passam.** Sete vizinhos diretos adicionais:
cinco passam, dois falham nos achados abaixo. Sem S1 identificado. Casos fora do núcleo de correção foram registrados
como limitações observadas, sem propor outro ciclo amplo do interpretador. Não se declara encerrado o RV-M3-02.

## Situação dos três achados do interpretador

| ID | Situação | Evidência e ação sugerida |
| --- | --- | --- |
| RV-M3-02 | **Aberto, S2 residual** | O caso anterior `losartana segunda e quarta na verdade só sexta` passa, e `segunda e quarta só às 8` preserva os dias. Entretanto, `losartana segunda e quarta na verdade sexta e metformina sábado` resulta em segunda/quarta/sexta para Losartana, em vez de somente sexta; Metformina fica corretamente em sábado. Em `voice/VoiceParser.kt:507`, o laço dos dias consome `na` como conector antes de chegar a `verdade`; assim, `cue` (`:280`) não recebe o par `na verdade` e não limpa o acumulador. É uma lacuna do próprio achado e um vizinho explicitamente pedido no roteiro, não uma exigência de ampliar a gramática. Sugestão mínima: não consumir o início desse marcador como conector da lista; adicionar o caso ao corpus e conservar o comportamento aditivo sem correção. |
| RV-M3C-03 | **Fechado na cobertura executada** | `31/2`, `15/13`, inclusive em TREATMENT/END_DATE, não viram outro dia. A expressão inválida é consumida e o término fica pendente. Os testes anteriores que produziam 02/11 e 13/11 passaram agora. |
| RV-M3D-01 | **Fechado na cobertura executada** | Ano explícito respeitado em `15/12/2027`, dois dígitos e formato por extenso (testes permanentes); ano passado não aceito, inclusive como resposta END_DATE. Teste adicional: `29/2/2028` → 29/02/2028; ano de três dígitos fica sem término por ser passado. Sem ano, `1/1` continua indo a 01/01/2027. |

## Achado nas mudanças de interação

| ID | Gravidade | Arquivo:linha | Evidência | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-M3E-01 | S3 | `voice/VoiceParser.kt:597` | Na pergunta de dias para Losartana, `o nome é atenolol` é ignorado, o nome continua Losartana e aparece “Não entendi. Tente de novo.”. Confirmado em teste unitário e na tela. O retorno antecipado de resposta direcionada executa antes de avaliar `renameNext`/`renameTarget` (`:618`), apesar da DEC-033 prever “o nome é X” em qualquer fala. Pelo botão “Algo errado? Corrigir por voz”, a mesma entrada troca o nome e pede Manter/Desfazer corretamente; não houve gravação errada. | Dar prioridade à correção explícita do nome sobre a regra que ignora nomes durante respostas comuns, mantendo a proteção contra criar outro medicamento. A alternativa pelo botão já funciona; não ampliar reconhecimento livre em todas as respostas. |

## Testes e escopo

Lidos AGENTS, ESTADO, DECISOES (DEC-003/014/026/030–033), HANDOFF_APP, ESPEC pertinente, revisão anterior,
roteiro atualizado e diffs de código. Proposta reaberta no Google Drive e requisitos oficiais consultados em
`Requisitos!A9:F26` (RF08–RF25). Nenhuma decisão nova aceita pelo autor nesta sessão.

- **Suíte original:** `assembleDebug test` inicial passa, em grande parte `UP-TO-DATE`; depois de remover os ajustes,
  executado novamente com `testDebugUnitTest`. XMLs finais: **110 testes, zero falhas/erros**.
- **Reprodutores anteriores:** copiados três testes temporários e as duas cópias de parser necessárias;
  `--tests br.edu.ifrs.genki.voice.Temporary*` passou **36/36** (7 + 14 + 15), incluindo os probes observacionais.
  XMLs preservados em `build/third_TEST-*.xml`.
- **Vizinhos diretos:** `TemporaryThirdCorrectionTest`, 7 testes, 5 passando e 2 falhando. Falhas correspondem
  a RV-M3-02/RV-M3E-01. Também conferidos: horário com “só” preserva dias, resposta direcionada não cria medicamento,
  fala livre continua criando outro, correção de nome em fala livre com dois rascunhos, correção de horário sem nome,
  ano bissexto explícito e data passada. Fonte/XML locais: `build/TemporaryThirdCorrectionTest.kt`,
  `build/third_direct_results.xml`.
- **Ambiente de tela:** Pixel_6_2, Android 15/API 35, `emulator-5554`. Samsung A31 estava conectado, mas **não foi
  operado nem recebeu APK** nesta revisão. Todas as operações ADB de teste/instalação selecionaram o emulador.

## Verificação de DEC-031 e DEC-033

- `como losartana` abriu rascunho Losartana, sem medicamento “Como”. Botão `1 comprimido`, resposta digitada
  `todos dias`, botão `08:00 e 20:00` e `Uso contínuo` completaram o cadastro até a revisão. Enviar texto vazio
  não avançou. O conjunto completo de botões é coberto pelo teste permanente que os passa pelo interpretador.
- Pergunta ativa: `eu tomo umas 9 da noite` → 21:00 no medicamento perguntado; `também tomo metformina às 20`
  não criou outro rascunho. Sem pergunta, a mesma menção criou o segundo medicamento, como exige DEC-031.
  Estes três casos foram conferidos no teste adicional, não com voz real.
- **Correção durante pergunta:** botão “Algo errado? Corrigir por voz” → entrada de nome → “Confira a correção”
  com Manter/Desfazer; voltar restaurou o nome e a pergunta originais. Correção por texto de Guardavoz para
  Metoprolol foi mantida e o app perguntou a dose ausente. “hum” não entendido preservou o modo de correção,
  dados e alternativa digitada; erro real de serviço de fala não foi provocado (tratamento inspecionado).
- **Correção na revisão:** “na verdade às 9” alterou 08:00 para 09:00 e ocultou Confirmar e salvar até decidir.
  Durante a correção pendente, “Corrigir” abriu o formulário; o nome foi mudado por toque para Losartanarev.
  Concluir voltou ainda com Manter/Desfazer. Voltar desfez **ambas** as mudanças desse bloco e restaurou
  Losartana/08:00/20:00; repetir a correção e Manter deixou 09:00/20:00. Cadastro depois descartado, sem gravação.
- **TTS:** `tts_iniciado` no processo do teste confirmou Google como leitor padrão, `status=0 idioma=1 pronto=true`.
  `Speaker` aceita `setLanguage >= LANG_AVAILABLE`. Por inspeção, o fallback encerra o primeiro TTS antes de criar
  o do Google; `stop` limpa a fala pendente e `shutdown` verifica inicialização. O guard evita acesso a `lateinit`
  quando o erro vem dentro do construtor. Não foi forçada essa falha nem a troca entre dois motores; TTS audível
  no A31 permanece evidência do autor/Claude no handoff, não verificação independente desta sessão.

Capturas: `build/third_rename_ignored.png`, `third_correction_question.png`, `third_correction_back.png`,
`third_empty_typed.png`, `third_pending_review.png`, `third_undo_with_touch.png`, `third_correction_missing_field.png`.

## Notificações, avisos agrupados e próximo lembrete

- **Roteamento:** entrega do Intent com flags `NEW_TASK|CLEAR_TOP|SINGLE_TOP` e extra `abrir_hoje` saiu de Detalhes
  para Hoje sem recriar a Activity (ADB confirmou entrega à instância existente). O mesmo Intent preservou o
  formulário com `Guardarev` digitado e uma edição de Metformina, que foi cancelada sem salvar.
- **Toque real com processo encerrado:** dois avisos publicados às 18:00, `timeId=27/28`. `am kill` não encerrou o
  processo; depois, PID 20525 foi encerrado de forma controlada via `run-as ... kill -9`, sem `force-stop`.
  Notificações permaneceram; toque em Revalfa abriu a Hoje em novo processo 21966, com os avisos recuperados.
  Em seguida, toque real na notificação de Revgama preservou o cadastro por voz Guardavoz em andamento.
  A condição “já aberto na Hoje” foi coberta pelo fluxo da mesma Activity e por inspeção de `launchSingleTop`,
  sem teste separado de toque real nessa condição.
- **Grupos:** Revalfa 18:00 e Revbeta 18:00/18:01 (tratamentos só em 27/10). Inicialmente dois avisos em um cartão;
  “Ver um por um” mostrou os cartões individuais e “Ver juntos” voltou ao grupo (foi necessário rolar até o botão).
  O horário 18:01 chegou antes de Adiar; então o grupo tinha três avisos. Adiar enviou `SNOOZE` para os três,
  às 18:01:15 (`27/28/29`), cancelou as notificações e mostrou “Lembretes adiados” com novo horário 18:11 em cada linha.
- **Tipos mistos:** Revgama 18:05 acrescentou um aviso tocando, separado do grupo adiado. O aviso único usou o cartão
  original; o grupo adiado permaneceu com suas linhas/Entendi. Capturas/XML confirmam dois cartões.
- **Entendi no mesmo minuto:** relógio fixado em 18:05:10 e fluxo de relógio atualizado ao sair/voltar à Hoje;
  Entendi no aviso único às 18:05:22 retirou `timeId=30` e mostrou Depois: 19:30, sem ressuscitar 18:05 como próximo.
  Entendi no grupo às 18:05:26 reconheceu os três adiados; Hoje voltou ao Próximo lembrete 19:30 da Losartana.
  Antes do minuto, o próximo 18:00 havia sido exibido. O efeito de alarme inexato durante o minuto é a escolha
  explícita do roteiro; não foi medido atraso real.
- **ABI:** APK sem `-Pgenki.abi` contém `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` e funcionou no emulador.
  Ramo `ndk.abiFilters` por propriedade inspecionado; APK arm64 isolado não foi gerado/instalado nesta sessão.
  Configuração já registrada no Diário do handoff pelo implementador.
- **Erros:** filtro `AndroidRuntime:E`/`TransportRuntime.Executor:E` sem mensagens capturadas nesta sessão;
  o encerramento de PID foi uma ação deliberada do teste. Logs completos têm também registros anteriores:
  as evidências acima usam os PIDs 20525/21966 e os horários citados. Sem medição de som/precisão temporal real.

Evidências locais: `build/third_genkieval.txt`, `third_errors.txt`, `third_final_alarms.txt`,
`third_notification_preserves_form.png`, `third_notification_preserves_edit.png`, `third_notification_preserves_voice.png`,
`third_group_after_cold_tap.png`, `third_group_individual.png`, `third_group_snoozed.png`, `third_group_mixed.png`,
`third_current_minute_answered.png`, `third_group_ack_all.png`, `third_final_clean.png`.

## Limitações observadas, sem abrir outro ciclo amplo

- `até 29/2` sem ano, em 2026, fica sem término; não busca sozinho o próximo ano bissexto. `29/2/2028` explícito funciona.
- O título do grupo conta avisos, embora diga “medicamentos”: três avisos de Revalfa e Revbeta aparecem como
  “3 medicamentos”. As linhas identificam os horários e as ações operaram nos avisos corretos; registrado para
  ajuste de texto posterior, sem ampliar o escopo do interpretador.
- TTS em fallback, callback após encerramento e erro real de reconhecimento durante correção não foram reproduzidos.
  Init normal, navegação/encerramento de tela sem crash e chamadas de cleanup foram conferidos; não se infere que
  todos os serviços/motores do Android terão o mesmo comportamento.
- Sem voz real, TTS audível, câmera/embalagens reais, operação no A31, TalkBack ou Doze nesta revisão. O autor adiou
  testes de fonte/lembretes/TalkBack no aparelho, conforme handoff; não foram promovidos a verificação independente.

## Limpeza e próximo passo

Usado apenas o ajuste local temporário em `SpeechInput.start`, autorizado no roteiro, para injetar texto.
Removido, assim como os quatro arquivos de testes temporários e duas cópias de parser. `git diff -- app/src` vazio.
Original recompilado e reinstalado **só no emulador**; `debug.genki.voz` esvaziado. Fonte 1.0, relógio real e
`auto_time=1`; intervalo original de 10 min preservado. Revalfa, Revbeta e Revgama excluídos pela interface;
rascunhos Losartana/Guardarev/Guardavoz/Metoprolol descartados. Metformina 08:00 e Losartana 19:30 preservadas.
Mudanças preexistentes de `.idea/` e `.claude/` preservadas. Nenhum commit/push; nenhum código/configuração alterado
de forma permanente pelo revisor.

Próximo passo: reparo pontual do marcador em RV-M3-02 e reconferência desse caso e dos aditivos; RV-M3E-01 tem
alternativa funcional pelo botão de correção e pode ser tratado sem ampliar o parser livre. Continuar a validação
no A31 dentro das decisões aceitas, distinguindo o teste do autor da cobertura desta revisão. Nenhuma limitação
ou recomendação acima foi convertida em nova decisão aceita pelo autor.
