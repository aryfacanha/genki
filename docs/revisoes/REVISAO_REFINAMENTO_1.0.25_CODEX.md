# Revisão independente do refinamento heurístico 1.0.25 — Codex

04/11/2026. Revisor: Codex. Escopo: P1–P29 e ajustes posteriores do autor, conforme o roteiro recebido e DEC-026/057. Nenhum código alterado; sem commit, push, tag ou Release.

## Parecer e limites

**Nenhum S1/S2 confirmado na leitura e nos testes executados.** Dois achados novos S3 (RV-125-08/09) e uma observação nova S4 (RV-125-10). As correções RV-125-01/02 estão presentes; RV-125-03 a 07 permanecem S4, com as ressalvas abaixo. As sugestões deste relatório **não são decisões aceitas pelo autor**.

**A regressão no emulador não foi executada.** Portanto, este parecer não certifica a apresentação em 1,0×/1,3×/2,0× nem conclui o roteiro visual para publicação. Os resultados de tela registrados pelo Claude em 03/11 são evidências anteriores, não reproduções desta revisão. Build/testes não substituem essa verificação.

## Alvo e execução

- Partida e término: `main`, `b2b7111ac5a36feb81f225594cc8943dbc157515`; intervalo revisado: `d292a2a..b2b7111`. O HEAD é posterior a `6c02273`: o commit adicional prepara o handoff/roteiro, sem código novo. Última alteração de código: `04d1c15`.
- Árvore inicial: somente `.claude/settings.local.json` não rastreado; preservado. Não li nem alterei essa configuração local.
- `git pull`: **não realizado com sucesso** — `cannot open '.git/FETCH_HEAD': Permission denied`. Falta permissão de escrita nesse caminho; não é uma conclusão sobre o estado remoto. A revisão usa o HEAD local acima.
- **`.\gradlew.bat assembleDebug test`**: `BUILD SUCCESSFUL`, 46 tarefas `UP-TO-DATE`.
- Para não atribuir execução a resultados reutilizados, executei **`.\gradlew.bat testDebugUnitTest --rerun-tasks`**: `BUILD SUCCESSFUL`, 28 tarefas executadas. Soma dos XMLs em `app/build/test-results/testDebugUnitTest/TEST-*.xml`: **225 testes, 0 falhas, 0 erros, 0 ignorados**. São testes unitários de debug; não testes instrumentados de interface.
- Avisos observados na recompilação: API `vibrate` obsoleta em `AlarmSoundService.kt:98` e condição sempre falsa em `VoiceParser.kt:361`, ambos em arquivos não modificados pelo intervalo. Não foram tratados como regressões deste refinamento.
- `git diff d292a2a HEAD --stat -- app/src/main/java/br/edu/ifrs/genki/reminders app/src/main/java/br/edu/ifrs/genki/data`: **vazio**. Também vazio para `DraftValidator.kt`, `AndroidManifest.xml` e `app/schemas`. Gradle muda somente `versionCode` 25→26 e `versionName` 1.0.24→1.0.25.

Fontes abertas: `AGENTS.md`, topo do estado, handoff e roteiro de regressão, decisões solicitadas (considerando também os ajustes posteriores DEC-043/050), especificação técnica, mapa de requisitos, tabela do refinamento e revisão do Claude. A proposta PDF e a planilha oficial de requisitos foram abertas com sucesso pelo conector Google Drive, pelos links de `docs/00_INDICE.md`; leitura, sem escrita. A especificação contém trechos históricos superados pelas decisões posteriores; não usei essas diferenças para reabrir funcionalidades já aceitas.

## Achados novos

Caminhos Kotlin abaixo relativos a `app/src/main/java/br/edu/ifrs/genki/`. S1 bloqueia; S2 erro relevante; S3 menor; S4 sugestão. **Confirmado por leitura** significa que a condição e sua consequência estão no código; não significa reproduzido em tela.

| ID | Gravidade / evidência | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-125-08 | **S3 — confirmado por leitura**, sem reprodução visual | `ui/form/FormScreen.kt:706` e `:708` | O campo “Por quantos dias?” aceita texto que não representa a data persistida. Depois de 7 dias válidos, substituir por `0` mantém `endDate` anterior, pois `onEnd` só é chamado para valores válidos. O texto passa a dizer para escolher a data, sem erro; a edição pode salvar a data anterior. Além disso, digitar/colar `1000` é convertido silenciosamente em `100` pelo `take(3)`, recalculando o término para cem dias. O validador recebe apenas a data válida e não detecta essa divergência da entrada. | Rejeitar ou sinalizar valores fora de 1–999, sem converter um número maior em outro válido. Distinguir vazio opcional de zero inválido; com entrada inválida, impedir conclusão ou explicar explicitamente que a data anterior foi mantida. Manter um único caminho pelo `DraftValidator`. |
| RV-125-09 | **S3 — confirmado por leitura**, sem ciclo de alarme reproduzido | `ui/home/HomeScreen.kt:457` e `:477`; contraponto em `reminders/ReminderReceiver.kt:71` | O cartão continua dizendo “avisa de novo em N min” depois da terceira repetição, quando nenhuma próxima repetição é agendada. `RepeatNote` depende só de `repeatEnabled` e de não estar adiado; o aviso continua no `AlertStore` sem resposta e não carrega o contador. A frase também apresenta N como tempo futuro sem acompanhar o tempo já transcorrido. O agendamento não está errado: o novo texto promete mais do que a interface sabe. | Trocar por descrição geral da regra, por exemplo “A repetição está ligada: até 3 novos avisos, com intervalo de N min.” Não alterar o agendamento nem criar estado novo só para esse texto. |
| RV-125-10 | **S4 — confirmado por leitura** | `ui/settings/SettingsControls.kt:107`; contraponto em `reminders/AlarmSpeech.kt:18` | A descrição abreviada promete horário e nome, mas, com vários remédios, `AlarmSpeech.text` fala a quantidade; se os horários diferirem, também não enumera horários. A descrição anterior explicitava a exceção dos vários medicamentos. Vale para Ajustes e introdução, que compartilham o componente. | Se o autor quiser manter a descrição precisa e curta: “Com um remédio, diz o horário e o nome; com vários, diz quantos são.” É sugestão de texto, não mudança da fala nem reversão da decisão de encurtar. |

**Gravidade do RV-125-08:** S3 porque a data de término continua visível e, no cadastro, aparece novamente na revisão; não constatei perda do banco nem alteração do agendador. Na edição, o salvamento direto torna a inconsistência mais fácil de deixar passar. Não interpreto o limite de 999 nem o aviso de dose como validação clínica.

**Reprodutores para a conferência visual pendente:**

1. RV-125-08: em tratamento temporário, informar início 04/11/2026 e 7 dias; verificar término 10/11. Substituir os dias por `0` e tentar concluir/salvar: pela leitura, o término continua 10/11. Repetir colando `1000`: o campo deverá exibir `100` e o término corresponder a cem dias. Não registrar esses resultados como já vistos.
2. RV-125-09: repetição ligada, deixar um lembrete completar suas três repetições sem resposta. Abrir Hoje: pela leitura, o texto continua prometendo novo aviso em N minutos, embora o receptor não tenha agendado a quarta repetição.

## Conferência da revisão do Claude

| ID | Conclusão independente |
| --- | --- |
| RV-125-01 | **Correção confirmada no código**: `pageCount` usa `rememberSaveable` (`IntroScreen.kt:72`), assim como `page`; o corpo usa `coerceIn` antes de indexar as páginas. Resolve a causa apontada, de começar com 2 na recriação. Não repeti mudança de fonte/morte do processo. |
| RV-125-02 | **Correção confirmada no código e na suíte unitária**: `savedDose` é preenchida ao carregar a edição (`FormViewModel.kt:190`); `needsWarning` compara dose salva e aceita antes de avisar (`:336`, `DoseUnit.kt:122`). Os dois testes novos cobrem dose alta inalterada, alterada e já aceita. A comparação é textual; não é equivalência clínica. Não reproduzi a edição em tela. |
| RV-125-03 | **Mantenho S4**. Reatribuir `ACK_NOTICE` enquanto ele já está em `notice` não reinicia `LaunchedEffect(notice)` (`HomeScreen.kt:196`); a segunda confirmação pode ter menos de 6 s de aviso. Sem efeito no reconhecimento do lembrete. |
| RV-125-04 | **Mantenho S4, agora confirmado por leitura quanto à persistência**: `DraftValidator.clean()` (`domain/DraftValidator.kt:61`) só faz `trim` e remove vazio; não remove quebra interna. Um nome recebido como `Nome\nComplemento` pode chegar ao banco assim. Não reproduzi colagem pelo teclado. Não há limite de tamanho do nome nesse validador; não repetir a afirmação do refinamento de que um limite de tamanho foi preservado. |
| RV-125-05 | **Mantenho S4 / escolha aceita de texto curto**. O autor explicitou a omissão de duração máxima, tela bloqueada e repetição em “Como alarme”. O texto restante não afirma duração ilimitada. Não encontrei outra tela prometendo que o som dura até tocar “Entendi”. RV-125-09 e 10 são inconsistências distintas, descritas acima. |
| RV-125-06 | **Mantenho S4**. README/guia e roteiro de bateria ainda contêm versão/textos/paginação antigos; atualizar na preparação da Release. Não interpretei registros históricos de entregas como instruções vigentes. |
| RV-125-07 | **Mantenho S4, com precisão**. Há imports residuais em arquivos alterados (por exemplo, rolagem/heading em `AddChoiceScreen`, autoSize/overflow em `GenkiApp`, ícone Add nas listas). Porém `verticalScroll`, `rememberScrollState` e `TextAutoSize` **são usados** no atual `Components.kt`; não removê-los em lote. Os avisos realmente emitidos pelo build foram os dois preexistentes descritos acima. |

## Cobertura dos pontos pedidos

| Área | O que a leitura/testes permitem concluir | Limite de verificação |
| --- | --- | --- |
| `ScrollHintColumn` | `showScrollHint` exclui `Int.MAX_VALUE` antes da medição e desaparece no fim; três testes passam. O `weight` está no modificador do **Box filho da Column**, via `ColumnScope`, não em um filho arbitrário do Box. O alvo tem 48 dp, papel Button, descrição e rótulo da ação. Formulário conserva `key(step)`; voz conserva o `ScrollState` externo e os efeitos de trazer a pergunta/teclado à vista. | O indicador se sobrepõe de fato ao centro inferior do conteúdo e pode interceptar um toque nesse trecho; rolar muda a área e no fim ele desaparece. Não confirmei controle inacessível. Legibilidade, TalkBack, foco e alvos sob a seta pendentes. |
| Introdução / medição dupla | Quatro blocos, mesma ordem e conteúdo; estados de permissão, voz e som ficam em `IntroScreen`. Os blocos medidos não possuem lançador de permissão ou escrita de preferências por simples composição; setters são callbacks de interação. “Como alarme” inclui o interruptor de fala no mesmo bloco. `ON_RESUME` relê permissões; `asked` impede redirecionamento automático na primeira recusa. Voltar decrementa página; na primeira segue o comportamento do sistema. | `SideEffect { onPages(...) }` publica a contagem medida; valor igual não exige mudança de estado. Não há laço demonstrado por leitura, mas os cinco testes de `IntroPagination` só empacotam alturas artificiais. Não provam ausência de piscada/laço, paginação real ou posição dos botões em 1,0×/1,3×/2,0×. |
| Tutorial | Texto em região rolável com `fill=false`; botões fora dela em `FlowRow`; recuos adicionais em Offer/posição inferior/sem alvo. `index` deriva de `step`, botão Voltar só para índice > 0; sete passos e sequência HOME/ADD/HOME permanecem coerentes com `TutorialStepsTest` (seis testes passam). | Não certifica geometria real, todos os balões com fonte grande, TTS ou TalkBack. App permanece restrito a retrato no manifest inalterado. |
| Dose / gravação | Validação precede aviso de quantidade. Ao aceitar, `save()` revalida; o aviso de duplicidade continua antes do repositório. `FixDraft` devolve rascunho validado à sessão, sem gravar, antes do aviso; ausência de aviso no fluxo final de voz já está registrada como limitação. `isUnusual` usa valor numérico de frações/decimais e `>20`; texto não reconhecido fica em Outro. | Não é controle clínico. O modo Outro não fica armazenado separadamente: se alguém digitar nele exatamente `30 gotas`, o texto será reconhecido como unidade conhecida e poderá avisar. Portanto, “Outro nunca avisa” deve ser entendido como texto não reconhecido, não como garantia pelo seletor escolhido. |
| Duração | Cálculo inclusivo `início + N − 1` e testes de equivalência com validador passam. Calendário atualiza dias; trocar início preserva término e recalcula dias, conforme limitação explicitada no refinamento. Efeito depende de início/término, não de cada alteração textual. Vazio é opcional e mantém data; zero e truncamento estão em RV-125-08. | Não reproduzi a interação com teclado/calendário. Datas escolhidas no calendário podem representar mais de 999 dias; a restrição se aplica à entrada numérica, não ao domínio nem ao calendário. |
| Horários/nome/textos | `NewTimeSuggestion` procura hora cheia livre a partir de 08:00 e dá a volta; cinco testes passam. Com as 24 horas cheias ocupadas volta a 08:00, caso-limite explícito no teste, ainda ajustável manualmente. Lápis é decorativo e o cartão mantém a descrição de alteração; nome admite até três linhas visíveis; `daysCapitalized` afeta apresentação. | Nenhuma prova visual de alvo/teclado; quebra interna do nome tratada em RV-125-04. |
| Hoje | `pastCount` e linhas passadas excluem os avisos correspondentes ao cartão; `firstAhead` e `NowMarker` permanecem coerentes com a lista original. Próximo lembrete e próximo dia repassam `onOpen`; cada remédio abre seus detalhes, enquanto o cartão inteiro não ganha clique. Aviso após Entendi **nega ingestão**. | Fluxos com vários medicamentos, expansão, aviso de 6 s e cartão tocando/adiado não reproduzidos. Texto de repetição: RV-125-09. |
| Botão Adicionar / barra | Hoje e Medicamentos usam `AddMedicationButton`; seu `modifier` é encaminhado ao `PrimaryButton`, preservando `tutorialTarget(ADD_BUTTON)` em `HomeScreen.kt:319`. Mesmo tamanho calculado para os três rótulos, medição com semibold, margem de 24 dp e `softWrap=false`. Quatro testes de `NavLabelSize` passam. | Testes da fórmula não medem glifos reais nem atestam que o “s” final cabe com escala 2,0×. Tamanho visual do botão e alvo real do tutorial pendentes. |
| Desfazer / ordenação | Timeout explícito de 15 s; só `ActionPerformed` restaura. Timeout/dismiss não passam pela restauração. `restore` revalida e grava **o mesmo id do medicamento**; ids dos horários são recriados pelo DAO já existente. `MedicationOrder` só é chamado na lista de Medicamentos e ordena por nome normalizado/id, inclusive após restauração. | Não confundir mesmo id do medicamento com ids dos horários ou recuperação de alertas pendentes. Clique dentro do prazo, remoção visual do snackbar ao cancelar e restauração na tela não executados. |
| Ajustes / Sobre | Compartilha textos do som com a introdução. Versão vem de `BuildConfig`. Texto de privacidade diferencia Genki do reconhecedor do aparelho, sem prometer reconhecimento offline universal ou precisão clínica. | Som audível e leitura real não verificados; exceção de fala em grupo em RV-125-10. |

## Regras do produto

- **Proposta / RF/RNF:** o diff trata de apresentação e facilitação dos fluxos já existentes; não introduz conta, rede, anúncios, nuvem própria, LLM, Hilt, WorkManager ou DataStore. Introdução/tutorial e alarme falado são decisões posteriores aceitas e documentadas como fora da proposta; este relatório não os converte em compromisso original.
- **Validação:** nenhuma nova escrita de medicamentos contorna `DraftValidator`. Duração produz `endDate`; aviso de dose precede o mesmo caminho de gravação. Restaurar exclusão também valida. O problema do RV-125-08 é a entrada descartada/truncada antes da validação, não um acesso paralelo ao banco.
- **Ingestão:** Entendi encerra o aviso; não cria tomada nem histórico. “O Genki não registra se o remédio foi tomado” é negação explícita, não confirmação.
- **Datas/concentração/OCR:** nenhuma conversão civil nova para UTC, nenhuma inferência de dose/horários/duração por OCR, nenhuma mistura de concentração com dose. `TreatmentDuration` usa `LocalDate` fornecida pelo formulário; o relógio do formulário continua usando a data civil do aparelho.
- **Escopo DEC-057:** nenhuma alteração no agendador, receptor, persistência ou esquema. O uso de `ReminderReceiver.MAX_REPEATS` na UI lê a constante existente; não muda a lógica.

## Emulador e o que não pôde ser verificado

`adb devices -l` mostrou apenas o A31 (`RX8N80313SX`). **Nenhum comando direcionado a esse serial foi executado**; não houve `installDebug`, instalação ou operação do telefone.

`Pixel_6_2` existe em `emulator -list-avds`, mas estava fechado. Tentei iniciá-lo sem janela, sem áudio, com `-read-only -no-snapshot-save -port 5554`, para não persistir os testes no AVD original. A inicialização foi bloqueada: `Unexpected error while creating: D:\Android\avd\Pixel_6_2.avd\snapshot.lock.lock (error: 5)`. O processo criado para a tentativa foi encerrado. Solicitei ao autor a abertura pelo Device Manager enquanto prosseguia na análise.

Assim, **não executados nesta sessão**: instalação do APK; cadastro manual completo; edição; exclusão/Desfazer; voz até conferência dos nomes; Hoje com vários remédios; Ajustes; introdução com os dois modos de som; tutorial e fontes 1,0×, 1,3× e 2,0×; mudança de fonte durante introdução; permissões/retorno do sistema; instalação limpa. Banco, preferências, fonte e permissões do Android **não chegaram a ser alterados**, portanto não houve restauração de dados a executar. Não alego comparação byte a byte nem verificação visual de preservação.

Também não verificados: A31 (proibido nesta tarefa), TalkBack, fala real/reconhecedor, TTS audível, alarme real, tela bloqueada, Doze, instalação release assinada. Nenhuma simulação de voz foi inserida no código, em respeito à proibição de alterá-lo.

## Próximo passo

Autor/Claude avaliar RV-125-08/09 e a sugestão RV-125-10, repetir a regressão visual pendente no `Pixel_6_2` e atualizar guia/README antes da tag/Release. Respeitar o corte de 05/11/2026, 12:00 (DEC-057). **Não há S1/S2 confirmado, mas não marcar o roteiro inteiro como verificado** com base nesta revisão. A decisão de publicar ou aceitar limitações é do autor; nenhuma nova decisão foi registrada como aceita.


## Correções do Claude depois deste relatório (04/11/2026)

- **RV-125-08 corrigido:** `TreatmentDuration.parse` separa vazio, válido (1 a 999) e inválido; o campo aceita até 4 dígitos, de modo que "1000" aparece como erro e não vira "100"; "0" também é erro, com a mensagem "Digite de 1 a 999 dias. A data de término continua a de antes.". Um segundo defeito apareceu no emulador: com digitação rápida o efeito que sincroniza o número com a data apagava dígitos; corrigido com `typedPair`. **Visto no emulador (fonte 2,0×):** 7 → "Termina em 10/11/2026"; 0 e 1000 → campo em erro. Teste unitário novo.
- **RV-125-09 corrigido:** o cartão "Agora" diz "Sem resposta, o Genki repete o aviso até 3 vezes, a cada N min." (regra geral, sem prometer novo aviso). Não visto em tela (exige lembrete disparado).
- **RV-125-10 corrigido:** "Com um remédio, diz o horário e o nome; com vários, diz quantos são." Visto só no código.
- 226 testes, 0 falhas. Falta a conferência do Codex sobre estas três correções.
