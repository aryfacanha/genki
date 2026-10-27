# Revisão da correção de M3/M4 — Codex

Data: 27/10/2026. Assistente: Codex (GPT-6).
Partida: `main`, `d62ab732f1431e82347e3ca6dfe6441a1f5e8dfd`; `git pull` confirmou atualizado.
Alvo: `1ed689b..7726739` (`d5b51d5` e `7726739`), conforme `ROTEIRO_REVISAO_CORRECAO_M3_M4_CODEX.md`.
Referência: `REVISAO_M3_M4_CODEX.md`. Os commits posteriores do HEAD são de documentação.

## Resultado

**Seis achados anteriores fechados; RV-M3-02 e RV-M3-04 parcialmente corrigidos, ainda abertos (S2).**
Os sete reprodutores da sessão anterior passaram. Os casos vizinhos exigidos pelo roteiro mostram que a correção
ainda não cobre complementos de dias em mais de um trecho da fala e negação de horário já presente no contexto.

**Três novos S2 registrados:** uma regressão da junção de letra/número no nome e dois erros preexistentes
(fração na dose e mês em data com barra). Cinco S2 abertos ao todo nesta revisão; nenhum S1 identificado.
Não considerar o pacote integralmente aprovado até corrigir/revisar os casos abertos.

Build e 96 testes existentes passam. DEC-030, frequência transferida ao formulário, datas na revisão da voz,
cartão após a meia-noite e origem CAMERA no log foram conferidos independentemente no emulador.
Código/configuração do app sem alteração permanente; ajustes temporários removidos e APK original reinstalado.

## Situação de cada achado anterior

| ID | Gravidade anterior | Situação | Evidência |
| --- | --- | --- | --- |
| RV-M3-01 | S2 | **Fechado na cobertura executada** | `VoiceParser.strength` consome `250 mg por 5 ml`, `250mg/5ml`, `500 mg por ml` e `500mg/ml` como concentração, sem dose. Acrescentar `tomo 10 ml` preenche a dose separadamente. No emulador, concentração `250 mg/5 ml` e pergunta sobre dose, sem pular para dias. |
| RV-M3-02 | S2 | **Aberto; correção parcial** | O caso original “e sexta” passa; “na verdade é sexta”, “só sexta” e “não sexta” substituem; “todos os dias” seleciona todos; intervalo segunda–sexta após sábado se soma. Porém, após segunda/quarta, “e sexta e também sábado” resulta em segunda/quarta/sábado, perdendo sexta. Na primeira fala “losartana segunda e quarta e também sexta”, restam somente sexta. `voice/VoiceParser.kt:415` sempre usa `beforeDays`, anterior à fala, e cada trecho sobrescreve o trecho já processado. **Correção sugerida:** acumular também os dias identificados na fala corrente, mantendo substituição explícita; acrescentar ambos os testes. |
| RV-M3-03 | S2 | **Fechado** | `FormFields.everyHours` é preservado. Emulador: resposta com dose e “duas vezes ao dia” → “Prefiro digitar” → etapa 2 com explicação “Você disse de 12 em 12 horas…”. Primeiro horário 08:00 gerou 20:00; adicionar 12:00 manteve os três, sem gerar outro ciclo. Validador ordena antes de gravar. |
| RV-M3-04 | S2 | **Aberto; correção parcial** | Os casos originais “às 8 e às 20, não, às 21” e “às 8 não às 9” passam sem contexto prévio. Com rascunho `08:00/20:00`, responder “às 20 não às 21” termina com `08:00/20:00/21:00`. `voice/VoiceParser.kt:182` retira apenas de `newTimes`; `finish` (`:198`) reaproveita o 20:00 de `before`, pois esse caminho não ativa `correction`. **Correção sugerida:** aplicar a negação também ao horário equivalente no contexto anterior, sem retirar os demais horários. “Não sei” continua funcionando como duração indefinida. |
| RV-M3-05 | S2 | **Fechado na cobertura executada** | Revisão direta, sem abrir correção por toque: “por 1 dia” mostrou `De 27/10/2026 até 27/10/2026`; outro medicamento com “até dia 30” mostrou `De 27/10/2026 até 30/10/2026`. Comparados com o cálculo do validador; ambos exibidos na mesma revisão múltipla. |
| RV-M2B-01 | S2 | **Fechado** | Reprodução independente no Pixel_6_2: tratamento só em 27/10, 23:50; outros medicamentos temporariamente com lembretes desligados. Adiar exibiu retorno 00:00; em 28/10, após `FIRE_SNOOZED`, Hoje mostrou cartão “Agora” e “Nenhum outro lembrete para hoje.”. Entendi retirou o cartão e restou “Nenhum lembrete para hoje.”. |
| RV-M3-06 | S3 | **Fechado para os nomes relatados** | Testes preservam `Vitamina B12`, `vitamina b 12` → `Vitamina B12`, `vitamina d3 2000 ui` → `Vitamina D3`; `vitamina d 7 gotas` mantém nome e dose separados. A junção adicionada criou a regressão distinta RV-M3C-01, abaixo. |
| RV-M4-01 | S3 | **Fechado** | Câmera → completar por voz → salvar: Logcat registrou `medicamento_salvo fonte=CAMERA id=13`, às 13:08:14.942. Registro de teste excluído depois. Código usa `valid.draft.source`. |

## Novos achados

Todos abertos. Correções sugeridas ainda não implementadas; não constituem decisões novas aceitas pelo autor.
Linhas do código original do alvo, sem os ajustes de teste.

| ID | Gravidade | Arquivo:linha | Problema, reprodução e origem | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-M3C-01 | S2 | `voice/VoiceParser.kt:537` | **Horário é incorporado ao nome.** `vitamina d 8 horas` agora gera `Vitamina D8` e nenhum horário; antes do reparo gerava `Vitamina d`, 08:00. O teste de `unitAfter` só exclui concentração/dose, não marcadores de hora. Com rascunho `Vitamina d` e pergunta TIMES, responder `vitamina d 8` ainda cria um segundo rascunho `Vitamina D8`, em vez de preencher 08:00 no existente. Comparação direta com `1ed689b` confirma a regressão no primeiro caso. RF12/13/15. | Não juntar número ao nome quando o contexto é horário ou há marcador de hora; pedir esclarecimento quando ambíguo. Preservar B12/D3 explícitos e os testes de dose/unidade. |
| RV-M3C-02 | S2 | `voice/VoiceParser.kt:334` | **Fração vira dose inteira diferente.** `losartana 1/2 comprimido às 8` gera dose `2 comprimido`. A barra é ignorada entre dois números e a regra de dose consome apenas `2 comprimido`. Comparação com `1ed689b` mostra o mesmo erro antes/depois: **preexistente, não introduzido pelos reparos**. Caso solicitado pelo roteiro ao avaliar `/`. RF12/15. | Interpretar a fração suportada ou deixar dose pendente e solicitar resposta (“meio comprimido” já é aceito). Não tratar o denominador como dose. Não exige ampliar para frações arbitrárias. |
| RV-M3C-03 | S2 | `voice/VoiceParser.kt:387` | **Data com barra ignora o mês e assume outro término.** Em 27/10/2026, `amoxicilina até 15/12` produz `endDate=2026-11-15`; `12` não entra na data. O caso `15/11` passa apenas por coincidência com o próximo dia 15. Comparação com `1ed689b` confirma que é **preexistente**; foi detectado na checagem de `/` prevista no roteiro. RF08/15. | Ler dia/mês como unidade ou rejeitar essa forma, manter término pendente e pedir a data por extenso. Não aceitar só o prefixo da data explícita. |

## Execução e evidências

Fontes reabertas: AGENTS, ESTADO, DECISOES (DEC-003/004/012/014/015/026/029/030), HANDOFF_APP, ESPEC §4–§8,
roteiros atual/anterior e relatório anterior. Proposta oficial e `Requisitos!A1:F37` consultados novamente pelas
ferramentas do Google Drive. `git pull` e Gradle precisaram de execução fora do sandbox para acessar `.git`/cache;
ambos funcionaram. Não houve bloqueio de acesso aos originais.

- **Build:** `assembleDebug test` inicial passou (`UP-TO-DATE`); após retirar os ajustes, `assembleDebug test`
  passou novamente, com execução dos testes, seguido de `installDebug` bem-sucedido. XMLs finais: **96 testes, zero falhas/erros**.
- **Reprodutores:** copiado temporariamente `build/TemporaryReviewTest.kt` para `app/src/test`, como solicitado: sete
  testes passaram. Criados 14 testes vizinhos temporários (incluindo comparação e observações); sete falharam nos
  comportamentos descritos. Execução final dos reprodutores: 21 testes, 14 passando/7 falhando; isso é distinto da
  suíte permanente de 96. Uma asserção exploratória sobre ordem dos horários foi ajustada para comparar os horários
  ordenados, pois a ordenação já é responsabilidade do validador; não foi classificada como defeito do app.
  Para atribuição de regressão, o `VoiceParser` de `1ed689b` foi copiado apenas para testes, sob outro nome.
  Evidências locais: `build/correction_original_results.xml`, `build/correction_neighbor_results.xml`,
  `build/TemporaryCorrectionReviewTest.kt`, `build/VoiceParserBaseline.kt`.
- **Ambiente:** Pixel_6_2, Android 15/API 35, `emulator-5554`. Voz substituída por texto; foto substituída por imagem
  somente nos ajustes locais previstos no roteiro anterior §4.2/4.3. Não houve uso de aparelho físico.
- **Concentração/dose e frequência:** `amoxicilina 250 mg por 5 ml` mostrou concentração correta e perguntou dose;
  resposta `10 ml duas vezes ao dia` preencheu dose/frequência; transferência para formulário e escolha de horários
  conferidas. Capturas `build/correction_strength_no_dose.png`, `correction_pending_frequency.png`,
  `correction_frequency_filled.png`.
- **DEC-030:** voz sem dias → formulário → etapa 2 sem opção marcada; Continuar exibiu “Selecione pelo menos um dia.”.
  Câmera → Continuar digitando → dose preenchida → etapa 2 também sem seleção e bloqueada. Cadastro manual começou
  em Todos os dias; edição de Metformina manteve Todos os dias e 08:00, sem salvar alterações.
  Capturas `correction_voice_missing_days.png`, `correction_camera_missing_days.png`, `correction_edit_existing_days.png`
  e `correction_manual_days.png` em `build/`.
- **Período da voz:** duas falas criaram dois rascunhos temporários; início/fim mostrados na revisão, inclusive após
  rolar para o segundo. `Auxiliar` removido antes de salvar; `Revmeia` salvo só com 23:50, início/fim 27/10.
  Capturas `correction_voice_period.png` e `correction_voice_multi_period.png`.
- **Virada do dia (M2/DEC-029):** Metformina e Losartana estavam com lembretes ativos; foram temporariamente
  desativados pela interface para não haver outra ocorrência em 28/10. Repetição/adiamento de 10 min, permissões já
  concedidas. Um primeiro disparo ocorreu às 23:50:03.199, depois da captura inicial; o teste refez a preparação
  com margem de 30 s (não foi falha do app). `FIRE timeId=24` às 23:50:00.019; Adiar às 23:50:08.552 mostrou 00:00;
  avanço a 28/10 00:01 gerou `FIRE_SNOOZED` às 00:01:00.402. Após sair/voltar à Hoje para atualizar o relógio da tela,
  cartão e mensagem de dia vazio foram conferidos. Entendi encerrou. Relógio real restaurado, cadastro de teste
  excluído e ambos os medicamentos originais reativados. Capturas `correction_midnight_ringing.png`,
  `correction_midnight_snoozed.png`, `correction_midnight_return.png`.
- **Câmera:** imagem `build/ocr.png` lida como Losartana potássica / 50 mg; dose continuou vazia, inclusive na voz.
  Completar por voz e salvar confirmou a origem CAMERA no log. Leitura, revisão, passagem para formulário e validação
  de dias conferidas; registro de teste removido. O OCR continuou sem inferir horários/dias/tratamento.
- **Regressão executada:** abas, cadastro manual/transferido, erros de etapa, formulário de edição sem salvar,
  revisão múltipla e exclusão com confirmação, liga/desliga dos lembretes, Agora/Adiar/retorno/Entendi depois do término,
  câmera e salvamento pela voz. A correção não altera receivers, alarmes ou manifest; ciclos completos de três
  repetições/deslizar já verificados na revisão anterior não foram repetidos nesta sessão.
- **Escopo:** sem nova dependência, permissão ou caminho de gravação; `Frequency` é lógica Kotlin pura compartilhada
  por voz/formulário. Escritas inspecionadas continuam passando pelo `DraftValidator`. Mantidas datas civis, ausência
  de histórico de ingestão e separação dos campos. Manifest final continua sem permissões de rede e sem os componentes
  `datatransport` removidos anteriormente.
- **Logcat:** nenhum `AndroidRuntime`/`FATAL EXCEPTION` capturado no filtro; às 13:07:27.763 ocorreu novamente a
  falha conhecida e contida de `TransportRuntime.Executor`, tentando agendar o componente de telemetria removido.
  A leitura da imagem funcionou. Evidências: `build/correction_genkieval.txt` e `build/correction_errors.txt`.

## Limpeza e limites

Usados os ajustes temporários de `SpeechInput.start` e `CameraScreen`, ambos **removidos**. Também removidos de
`app/src/test` os dois testes temporários e a cópia do parser anterior; reprodutores mantidos somente em `build/`.
`git diff -- app/src` vazio ao terminar. APK original recompilado/reinstalado; imagem de teste do emulador removida,
`debug.genki.voz` esvaziada, fonte `1.0`, relógio real/`auto_time=1`. Metformina 08:00 e Losartana 19:30 preservadas
e reativadas; demais registros/rascunhos de teste excluídos ou descartados. Alterações locais preexistentes de `.idea/`
e `.claude/` preservadas. Sem commit/push nesta sessão.

Não verificados: voz real, TTS audível, câmera/embalagens reais, aparelho físico, TalkBack, Doze, fuso e reinício
novamente. Não foi repetida toda a regressão dos marcos anteriores; a cobertura efetiva está listada acima.
O teste temporal usa saltos de relógio, não mede pontualidade de alarmes em uso real.

Próximo passo: corrigir os dois achados parcialmente resolvidos e os novos casos, com tratamento conservador das
formas não suportadas, e submeter os reparos à revisão. DEC-030 é decisão já aceita em 27/10; nenhuma decisão nova
foi solicitada/aceita nesta sessão. Compilação e testes no emulador não substituem o aceite do autor no celular.
