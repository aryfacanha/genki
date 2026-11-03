# Revisão — chips e aviso de volume — Codex

**Atualização posterior em 27/10/2026:** `33a33ab` e `a96184b` revisados pelo Codex, sem achados na cobertura executada: `REVISAO_QUANTIDADE_GRADE_CODEX.md`. Grades e fonte ampliada conferidas nos grupos registrados; aviso ao entrar baixo e expirar observado em 2,0× no APK original. Os limites desta primeira rodada abaixo são históricos; TalkBack e demais transições de volume não foram inferidos como verificados.

Data: 27/10/2026. Assistente: Codex (GPT-6).
Partida e alvo: `main` / `ee237343c19b1d40564fb80377181fde743cc75f`.
Comparação: `15dc9ef..ee23734`. `git pull`: Already up to date; branch com três commits locais à frente.

## Resultado

**Nenhum achado S1–S4 confirmado no diff do commit.** A revisão de código e o build foram concluídos; a conferência independente de interface ficou parcial por alterações/instalações simultâneas no mesmo emulador. Este relatório não aprova as mudanças locais de quantidade que apareceram durante a sessão.

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| — | — | — | Nenhum defeito confirmado no escopo revisado | — |

S1 = bloqueia; S2 = erro relevante; S3 = menor; S4 = sugestão.

## Base e escopo

- Lidos: `AGENTS.md`, `docs/ESTADO.md`, decisões pertinentes (DEC-026/036), `docs/HANDOFF_APP.md`, `docs/ESPEC_ARQUITETURA.md`, índice e mapa de requisitos.
- Originais atuais reabertos pelo conector do Drive: [proposta](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view) e [requisitos oficiais](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit).
- **Compromisso da proposta:** interface legível, feedback perceptível, confirmação/correção do cadastro. Relacionados: RF03/05/18/19 e RNF01/02/04.
- **Decisão aceita:** DEC-036 e ajuste visual solicitado pelo autor, conforme entrega/Diário do Claude. Nenhuma decisão nova aceita nesta revisão.
- **Implementado:** reserva fixa de 18 dp para o ✓ em `Components.kt:224`, usada nas unidades e dias do formulário e no seletor de dias da voz; aviso central de volume baixo em `VoiceScreen.kt:160–238`, acionado ao entrar abaixo de 30%, temporizador de 4 s e saída animada de 800 ms.

Os reparos `d4d6350` já têm conferência anterior que fecha RV-VZ-01/02; não foram reabertos como pendências. O commit `15dc9ef` é documental.

## Inspeção e build

`CheckSlot` permanece no layout tanto marcado quanto desmarcado, mantendo a medida; o ícone é decorativo (`contentDescription = null`), e o estado continua no `FilterChip.selected`. Os três usos preservam a altura mínima de 48 dp. O contêiner da voz continua recebendo os mesmos callbacks e `imePadding`; a sobreposição não acrescenta ação ou gravação. O temporizador não reabre o aviso a cada consulta enquanto o volume continua baixo, pois `wasLow` só rearma após voltar ao volume suficiente. Os efeitos são cancelados quando a composição é retirada.

Sem alteração de dependências, manifest, banco, parser, validação ou agendamento no alvo. Não foi criado outro caminho de gravação, inferência por OCR ou indicação de ingestão. Não há lógica pura nova que exija outro teste unitário neste ajuste visual.

**Verificado:** `.\gradlew.bat assembleDebug test` passou antes das mudanças concorrentes serem observadas; tarefas `UP-TO-DATE`. XMLs naquele momento: **126 testes, zero falhas/erros**. Não significa nova execução dos 126 testes nesta sessão. `git diff 15dc9ef..ee23734 --check` sem erros.

## Emulador: observações e limites

APK inicialmente instalado com `adb -s emulator-5554 install -r`, retorno Success. Não foi operado o A31, embora estivesse conectado.

Antes de a interface nova ser percebida:

- Hoje abriu com Metformina e Losartana; adicionar → escolha → voz → Prefiro digitar abriu o formulário.
- Unidade Comprimido pôde ser marcada. As posições dos cinco rótulos de unidade foram iguais nas árvores antes/depois; captura mostrou ✓ e campo de quantidade. Evidências locais em `build/review_form_before.xml`, `review_form_after.xml` e `review_form_after.png`.
- Aviso central visível com mídia em **2/15**, depois ausente mantendo o volume baixo. Capturas `build/review_volume_shown.png` e `review_volume_hidden.png`. Isso confirma a aparência observada, sem cronometragem precisa dos 4 s nem isolamento completo da versão instalada.

**Interferência detectada:** `DoseUnit.kt`, `FormScreen.kt` e `DoseUnitTest.kt` passaram a ter mudanças locais de outro trabalho; a interface instalada passou a exibir botões de quantidade (Meio/1/2/3/Outra), ausentes no alvo. Houve também falha do `uiautomator` por serviço de automação já registrado. Assim, árvores posteriores de dias e captura de fonte 2,0× não são evidência válida de regressão deste commit. A navegação foi interrompida; nenhuma mudança local de outro trabalho foi desfeita.

O comando `cmd media_session volume --set` foi recusado pelo sistema (`SecurityException`, AUDIO_MEDIA_VOLUME); o volume foi ajustado pela tela de Som. Fonte **1,0×** e volume de mídia **8/15**, valores iniciais, foram restaurados e relidos. Nenhuma alteração de permissões ou relógio pelo revisor. Nenhum medicamento foi salvo, editado ou excluído pelo revisor. Pode restar um rascunho manual não salvo no emulador; não foi feita limpeza final de navegação após detectar o trabalho simultâneo.

## Pendente

Conferir a versão final em sessão exclusiva: marcar/desmarcar os sete dias no formulário e na voz, inclusive com fonte 2,0×; aviso ao entrar com volume baixo, ao baixar/subir e nova queda após expirar; TalkBack anunciando o aviso; teclado nas perguntas da voz. Não verificados nesta rodada: voz/TTS audíveis, câmera, ciclo temporal dos lembretes e aceite completo no A31. Sem falha de app confirmada nesta checagem parcial; erros de automação não foram classificados como defeitos do Genki.

Nenhum código/configuração do app foi alterado pelo Codex. Sem commit/push. As mudanças concorrentes de quantidade precisam de revisão própria após conclusão do implementador.
