# Revisão de M3/M4 e melhorias de M1/M2 — Codex

**Atualização de 27/10/2026 — revisão dos reparos:** seis achados abaixo foram fechados; RV-M3-02/04 seguem abertos por correção parcial e três novos S2 foram encontrados. Situação atual e evidências em [REVISAO_CORRECAO_M3_M4_CODEX.md](REVISAO_CORRECAO_M3_M4_CODEX.md). O resultado e a tabela abaixo preservam a revisão original, anterior aos reparos.

Data: 27/10/2026. Assistente: Codex (GPT-6).
Partida: `main`, `7fd7c8906acc558f059129915f33f5f4b744e8cc`; `git pull`: atualizado.
Alvo: `a590fa5..9a235fc`, conforme `ROTEIRO_REVISAO_M3_M4_CODEX.md`; o HEAD acrescenta documentação ao alvo.

## Resultado

**Revisão concluída com correções pendentes: seis S2 e dois S3; nenhum S1 identificado na cobertura executada.**
Build e 87 testes existentes passam. Os casos adicionais expõem problemas do parser e da passagem para o formulário
que o corpus existente não cobre. Não aprovar M3 como encerrado antes de corrigir/revisar os S2.
A regressão temporal pendente de M2 (retorno de Adiar, três repetições e deslizar) passou no emulador.
O novo achado RV-M2B-01 é da apresentação do aviso na Hoje, distinto dos RV-M2-01/02 já fechados.

Código do app e configuração permanecem iguais aos da partida. Somente documentação foi alterada de forma permanente.
Não houve commit nem push. Não houve decisão nova aceita pelo autor.

## Fontes e critérios

Lidos: AGENTS, ESTADO, DECISOES (em especial DEC-004/012/014/016/026/028/029), HANDOFF_APP, ESPEC §4–§8,
REQUISITOS_MAPA, roteiro e revisões anteriores; diff do alvo e código relacionado. Consultados os arquivos locais
pertinentes do protótipo em `D:\dev\genki-prototype` (referência visual, sem tratá-lo como especificação dos dados).

Proposta oficial reaberta pelo Google Drive e planilha oficial lida diretamente em `Requisitos!A1:H50`.
A exportação da planilha pelo Drive retornou 403; a leitura das células pela API de Sheets funcionou.
Requisitos centrais: RF06, RF08–RF25, RNF02/04/05/07/09/10/11. Não se inventaram resultados de reconhecimento real.

## Achados

S1 = bloqueio; S2 = erro relevante; S3 = menor; S4 = sugestão. Todos os itens abaixo permanecem **abertos**.
As correções sugeridas são recomendações do revisor, ainda não implementadas nem aceitas como novas decisões de produto.
Linhas referem-se ao código original, sem os ajustes temporários.

| ID | Gravidade | Arquivo:linha | Problema e evidência | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-M3-01 | S2 | `voice/VoiceParser.kt:307` | **Concentração composta vira dose.** “amoxicilina 250 mg por 5 ml” resulta em concentração `250 mg` e dose `5 ml`, embora nenhuma dose de uso tenha sido informada. Reproduzido em teste Kotlin e na tela: a próxima pergunta já é sobre dias, pulando dose. Viola a separação concentração/dose e RF15. | Consumir a expressão de concentração composta antes da regra de dose; se não a suportar, manter dose vazia e perguntar. Acrescentar caso com e sem uma dose explícita após a concentração. |
| RV-M3-02 | S2 | `voice/VoiceParser.kt:386` | **Complementar dias apaga os anteriores.** Após “losartana segunda e quarta”, a fala “e sexta” substitui o conjunto por sexta. Teste independente esperava preservar segunda/quarta e falhou. Não há distinção entre complemento e correção nesse setter. Afeta RF12/13 e pode retirar lembretes. | Unir os dias em uma continuação aditiva; reservar substituição para correção explícita ou perguntar quando ambíguo. Testar complemento e substituição. |
| RV-M3-03 | S2 | `ui/form/FormViewModel.kt:63` | **“Prefiro digitar” perde frequência pendente.** “metformina 1 comprimido duas vezes ao dia” produz `everyHours=12` e pergunta o primeiro horário. Ao mudar para digitação, o formulário recebe nome/dose/dias, mas perde `everyHours` e não mostra a frequência. Na etapa 2, adicionar um horário deixa de gerar/pedir o segundo; o usuário precisa reconstruir a informação. Reproduzido em teste Kotlin e na interface. RF06/13/18. | Preservar a frequência até obter horários concretos, ou apresentar explicitamente a informação e solicitar sua conversão; não descartá-la silenciosamente na mudança de modalidade. |
| RV-M3-04 | S2 | `voice/VoiceParser.kt:247` | **Negação de horário é aceita como mais um horário.** O corpus adicional “losartana às 8 e às 20, não, às 21” resulta em `08:00, 20:00, 21:00`. `não` está entre as palavras ignoradas; não há pedido de esclarecimento. Pode criar lembrete que o usuário acabou de negar (RF15/16). Resultado conferido no parser, sem voz real. | Reconhecer esse caso simples de correção ou recusar a interpretação ambígua e pedir o horário correto. Não é necessário ampliar para uma gramática geral. |
| RV-M3-05 | S2 | `voice/ui/VoiceScreen.kt:364` | **Revisão da voz omite a data final calculada.** Cadastro completo “Revtest … por 1 dia” mostra somente “Por 1 dias a partir de hoje”; a data final aparece apenas depois de abrir e concluir a correção por toque. Confirmado em captura. RF08/DEC-003 exigem apresentar a data convertida na confirmação; RF17 exige conferir os dados antes de gravar. | Mostrar início/fim normalizados pelo mesmo validador na revisão da voz, inclusive na entrada direta e para cada medicamento. Corrigir também o singular. |
| RV-M2B-01 | S2 | `ui/home/HomeScreen.kt:156` | **Aviso pendente fica oculto quando não há ocorrência no dia.** `s.today.isEmpty()` é tratado antes de renderizar `s.alerts`. Exemplo: único tratamento acaba às 23:50, usuário adia e retorna após meia-noite; o aviso continua pendente, mas Hoje mostra “Nenhum lembrete para hoje” e omite o cartão/ações da DEC-029. Confirmado por inspeção dos ramos e dos fluxos de estado; **a virada do dia com essa configuração não foi executada** nesta sessão. | Renderizar avisos pendentes independentemente de `today` estar vazia; tratar a lista do dia separadamente. Verificar tratamento encerrado e medicamento de dias específicos no dia seguinte. |
| RV-M3-06 | S3 | `voice/VoiceParser.kt:36` | **Número integrante do nome é descartado.** “vitamina B12 um comprimido às 8 todos os dias uso contínuo” produz nome `Vitamina B`, com os outros campos preenchidos. O tokenizador separa letras e números; `12` é perdido. Reproduzido no corpus adicional. O nome aparece para revisão, mas não é preservado (RF12). | Preservar tokens alfanuméricos de nome ou pedir confirmação explícita quando não suportados; adicionar `B12` ao corpus. |
| RV-M4-01 | S3 | `voice/ui/VoiceViewModel.kt:166` | **Origem da câmera registrada como VOICE ao salvar pela voz.** Câmera → revisão OCR → completar por voz → salvar gera `medicamento_salvo fonte=VOICE`, embora o rascunho continue `source=CAMERA`. Reproduzido (`id=10`, depois excluído). É erro de instrumentação, não de persistência dos campos; interfere na atribuição dos ensaios da câmera (DEC-015). | Registrar `valid.draft.source`, como já faz o formulário, para cada item salvo. |

## Verificação independente

- **Build/testes existentes:** `.\gradlew.bat assembleDebug test` passou inicialmente com tarefas `UP-TO-DATE`.
  Após remover todos os ajustes temporários, `.\gradlew.bat assembleDebug test installDebug` passou com execução
  de `testDebugUnitTest`. XMLs finais: **87 testes, zero falhas/erros**. O APK original foi reinstalado.
- **Casos adicionais:** criado temporariamente `TemporaryReviewTest` com sete testes, incluindo um corpus exploratório.
  Três testes falharam nos contratos de concentração/dose, complemento dos dias e preservação da frequência.
  Quatro passaram (corpus sem asserções, dose antes do nome, relógio inválido e correção de horário durante pergunta
  de duração). Esses quatro não demonstram cobertura geral. Fonte e XML de reprodução preservados somente em
  `build/TemporaryReviewTest.kt` e `build/review_parser.xml`; teste temporário removido de `app/src/test`.
- **Instalado em:** Pixel_6_2, Android 15/API 35, `emulator-5554`. Nenhum aparelho físico conectado foi usado.
- **M0/M1 e integrações:** abas, escolha Voz/Digitar/Câmera, formulário preenchido, cadastro em etapas, revisão,
  detalhes/exclusão com confirmação, correção por toque e remoção de um item da revisão múltipla conferidos.
  “Revtest” foi cadastrado com dois horários 13:00/13:01, tratamento só em 27/10, e persistiu após reinstalação do APK.
  Concluir a correção por toque normalizou e exibiu o término. O relógio recusou trocar 13:01 por 13:00 (erro e OK
  visualmente desabilitado); calendário do término exibiu dias anteriores a 27/10 desabilitados. A validação de
  início mudado para depois do fim permanece no `DraftValidator` (inspeção/testes existentes).
- **M2/DEC-029:** relógio do emulador controlado pelo §5 passo 8 do handoff, sem alterar o relógio do computador.
  Às 13:00:00.072, `FIRE timeId=20`; segundo `FIRE timeId=21` às 13:01:10.179 após avanço do relógio.
  Dois cartões/notificações no último dia; primeiro cartão mostrava “Depois: 13:01 · Revtest”. Adiar o primeiro às
  13:01:26.319 exibiu “Lembrete adiado / Novo alerta às 13:11 / Horário original: 13:00”.
  Ao avançar para 13:12, retornou `FIRE_SNOOZED timeId=20` e ocorreu `REPEAT 1 timeId=21`.
  Às 13:23, repetição do adiado manteve “(adiado)” na notificação e ocorreu `REPEAT 2 timeId=21`.
  Às 13:34, `REPEAT 3 timeId=21`; salto para 13:45 não gerou quarta repetição desse aviso.
  Entendi no cartão de `timeId=20` retirou só esse aviso; deslizar a notificação de `timeId=21` retirou o outro cartão
  e registrou `lembrete_reconhecido`. Sem “Tomado” ou histórico. Estes saltos verificam o fluxo, não precisão temporal real/Doze.
- **Atualização com avisos pendentes:** dois avisos ativos e seus `REPEAT` foram conferidos antes de `adb install -r`.
  Após instalar, notificações foram retiradas pelo sistema, cartões continuaram na Hoje e ambos os alarmes `REPEAT`
  permaneceram em `dumpsys alarm`. Não foi confirmada entrada órfã nesse caso; não confundir ausência temporária
  de notificação com perda dos próximos alertas. Os cartões foram encerrados e o cadastro de teste excluído.
- **M3:** texto injetado substituiu apenas o reconhecedor. Conferidos frase completa, pergunta de dose/dias/horário,
  dois rascunhos, revisão, correção por toque, nova fala na revisão, remoção de um rascunho e salvamento; troca para
  digitação expõe RV-M3-03. Corpus sugerido no roteiro executado: dose antes do nome e intervalo 12/12 geram
  `08:00/20:00`; “metformina 850 duas vezes ao dia às 7” gera `07:00/19:00`, sem inventar dose/concentração;
  “AAS 100 mg depois do almoço” e “sinvastatina à noite” deixam horários vazios; “vitamina D uma vez por semana”
  deixa dias/horários vazios; “omeprazol … às 6 e meia” gera `06:30`; “remédio da pressão às 8” deixa nome vazio;
  “puran 50 microgramas” gera concentração `50 mcg`; “insulina 10 unidades às 7” gera dose `10 unidades`;
  `sim` e `não` isolados não criam rascunho. Casos problemáticos estão na tabela.
- **M4:** cena virtual sem texto → falha compreensível → tentar de novo. Com `build/ocr.png`, ML Kit leu
  `Losartana potássica` / `50 mg`; `30 comprimidos` não preencheu dose. “Continuar por voz” perguntou dose;
  completar por texto injetado e salvar passou pelo validador. Com imagem sintética própria (laboratório em destaque,
  `DIPIRONA / 100 ml / 500 mg por ml`), sugeriu `Laboratorio exemplo` / `100 ml`, apresentados para revisão.
  “Continuar digitando” preservou essas sugestões e deixou dose vazia; o restante foi preenchido manualmente e
  descartado sem salvar. A leitura errada do laboratório/volume corresponde à heurística explicitamente prevista
  na ESPEC §8; fica como **limitação verificada**, não como demanda para ampliar a PoC.
- **Manifest final:** conferido o artefato em `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`:
  sem `INTERNET`, `ACCESS_NETWORK_STATE` e componentes `datatransport` removidos. Dependências novas correspondem
  ao CameraX/ML Kit previstos. Sem novo caminho de escrita contornando `DraftValidator`; nenhum áudio/foto salvo
  pelo fluxo inspecionado. Datas de domínio continuam civis; UTC no DatePicker apenas desfaz a representação exigida pelo componente.
- **Fonte:** capturas da lista/barra inferior em 1,5× e 2,0× inspecionadas; rótulo “Medicamentos” inteiro e indicador
  da aba ativo. O autosize reduz proporcionalmente esse rótulo; isso não comprova acessibilidade com TalkBack.
- **Logcat:** sem `AndroidRuntime`/`FATAL EXCEPTION` capturado no filtro. Reproduzido o erro conhecido
  `TransportRuntime.Executor` tentando agendar o serviço removido, às 12:27:45 no processo do teste da câmera.
  A falha foi contida e a leitura funcionou. Aceitável nesta PoC mínima, registrada como limitação, sem sugerir
  reintroduzir rede/telemetria para eliminar a mensagem.

## Limpeza e limites

Usados **ambos** os ajustes locais autorizados no roteiro §4.2/§4.3 (`SpeechInput.start` e captura da câmera).
Ambos foram removidos. Teste temporário também removido; `git diff -- app/src` vazio ao terminar.
Removido `/sdcard/Android/data/br.edu.ifrs.genki/files/ocr.png`; propriedade `debug.genki.voz` esvaziada.
Fonte restaurada a `1.0`, relógio real restaurado e `auto_time=1`. Medicamentos de teste (`Revtest`, `Losartana potássica`)
excluídos pela interface; `Auxiliar` removido antes de salvar. Metformina 08:00 e Losartana 19:30 preservadas;
app deixado em Hoje. Ajustes locais preexistentes de `.idea/` e `.claude/` preservados.
Capturas/dumps e os reprodutores em `build/review_*` são evidências locais não versionadas.

**Não verificado:** voz real, TTS audível, aparelho físico/câmera real, embalagens reais ou os ensaios formais MET-1/2,
TalkBack, Doze, mudança de fuso, reinício novamente nesta sessão, negação inicial de permissões, toque duplo/corridas
concorrentes entre ações e emissão. Não se repetiu toda a regressão de M0/M1 (por exemplo, todos os erros de campo
vazio, edição persistida e todos os estados vazios); a cobertura acima é a efetivamente executada.
RV-M2B-01 foi verificado por inspeção, com reprodução de virada do dia ainda pendente.

**Ponto para decisão do autor, sem nova aceitação:** `FormFields.from` continua convertendo dias ausentes da voz/câmera
em “Todos os dias”. O padrão é visível no formulário/revisão e coincide com o manual/protótipo, portanto não é
inferência do OCR. Recomenda-se confirmar explicitamente se deve permanecer assim ou se dias precisam ficar sem
seleção na transferência. A limitação já registrada no handoff continua vigente até decisão/correção.

Próximo passo: Claude Code corrigir os achados aceitos para correção, com testes dos casos de parser/conversão e
verificação do cartão fora do dia programado; Codex revisar os reparos. Depois, teste do autor no celular com
reconhecedor pt-BR e embalagens reais. Implementado/compilado/testado no emulador não equivale a aceite no aparelho.
