# Roteiro de revisão para o Codex — refinamento heurístico da 1.0.25 (P1–P29 e ajustes do autor)

Preparado em 03/11/2026 por Claude Code (Sonnet 5.5), a pedido do autor ("manda o Codex revisar essas alterações"). Quem revisa
**não altera código** (AGENTS.md, DEC-026). Responda em português do Brasil.
Leia antes: `AGENTS.md`, `docs/ESTADO.md` (topo), `docs/HANDOFF_APP.md` (§2, §3, §4, §5, §6, §7), `docs/DECISOES.md`
(DEC-004, DEC-014, DEC-041, DEC-044, DEC-049, DEC-052, DEC-055, DEC-057) e **`docs/REFINAMENTO_HEURISTICO_1.0.25.md`**
(tabela do que mudou, do que não foi corrigido e do que foi ou não visto em tela).

## Alvo

`main`, intervalo **`d292a2a..HEAD`** (1.0.24 → 1.0.25, versionCode 26). Na preparação (03/11, noite) o `HEAD` era **`6c02273`**; use o que estiver no `git log` e diga qual foi. Só experiência do usuário: a lógica de lembretes
e o esquema do banco **não** deveriam ter mudado (confirme: `git diff d292a2a HEAD --stat -- app/src/main/java/br/edu/ifrs/genki/reminders app/src/main/java/br/edu/ifrs/genki/data`).

Commits: `5470da3` (indicador de rolagem; botões da voz), `64e3037` (ordem alfabética), `53178fb` (aviso de quantidade alta),
`6ad2897` ("por quantos dias"), `d7aa8a0` (Sobre), `7353be1` (textos e indicações visuais), `60400a9` e `bd78b36`
(comportamentos; P4 revertido), `080a48c` (versão), `f97d969` (balão do tutorial com fonte grande), `2c10faf`
(introdução em páginas dinâmicas), `16db180` (som do lembrete mais curto, alarme falado na introdução, Adicionar sem título),
`7415ff1` (rótulos da barra iguais, botão Adicionar numa linha, alarme falado na mesma página), `8f4a340` (correções
RV-125-01 e RV-125-02 da revisão do Claude), `04d1c15` (botão "Adicionar medicamento" único, `AddMedicationButton`), mais
commits só de `docs/`.

## O que já foi feito e o que falta (03/11, noite)

- **Revisão do Claude** em `docs/revisoes/REVISAO_REFINAMENTO_1.0.25_CLAUDE.md` (leitura de diff; revisor = autor do código,
  portanto **não independente**). Achados: RV-125-01 e RV-125-02 (S3) **já corrigidos em `8f4a340`**: confirme as duas
  correções (`IntroScreen.kt` com `pageCount` em `rememberSaveable`; `DoseUnit.needsWarning` e `savedDose` no
  `FormViewModel`); RV-125-03 a 07 (S4) abertos: diga se discorda da gravidade ou se há algo pior.
- **A tentativa anterior do Codex** (03/11) parou por limite de uso sem relatório; não há achados dela.
- **Número esperado de testes:** 225, 0 falhas (`.\gradlew.bat assembleDebug test`).
- **Estado do A31:** a 1.0.25 está instalada por cima; a introdução só aparece com dados limpos. **Não opere o A31.**
- **Depois da sua revisão:** o autor e o Claude Code criam a tag e a Release `v1.0.25` (HANDOFF §4). Se houver S1/S2, não
  há tag; o autor decide entre corrigir ou deixar como limitação (corte da DEC-057: 05/11, 12:00).
- **Pontos extras de atenção** além dos da lista abaixo: `AddMedicationButton` (Hoje e Medicamentos; o alvo do tutorial
  `ADD_BUTTON` ainda funciona na Hoje); o texto curto de "Como alarme" omite "até 1 minuto", tela bloqueada e repetição
  (decisão do autor; diga se algum texto de outra tela ficou contraditório); introdução com 4 blocos medidos
  (`SubcomposeLayout`) em 1,0×, 1,3× e 2,0×, com "Como alarme" e "Como notificação".

## Execução

1. `git pull`; registre o commit de partida; `.\gradlew.bat assembleDebug test`: esperado **225 testes, 0 falhas**.
2. Regras do produto a conferir em todo o diff (`git diff d292a2a HEAD -- app`): toda gravação pelo `DraftValidator`
   (o aviso de quantidade e o "por quantos dias" não criam outro caminho até o banco); "Entendi" não registra ingestão,
   nada de "Tomado" (o aviso novo "Lembrete encerrado…" não pode sugerir que foi tomado); datas civis no fuso do
   aparelho; sem rede, Hilt, WorkManager, DataStore ou LLM; concentração ≠ dose; texto de Sobre sem promessa clínica nem
   de reconhecimento offline universal.
3. Pontos de atenção (onde um erro seria mais provável):
   - **`ui/components/Components.kt` — `ScrollHintColumn`:** `canScrollForward`/`maxValue` antes da primeira medição;
     `weight(1f, fill)` dentro de `Box`; o botão sobreposto cobre conteúdo tocável? semântica para o TalkBack
     (`clickable` + `contentDescription`); uso em `FormScreen` (`key(step)`), `VoiceScreen` (a variante com `scroll`
     externo que leva a pergunta à vista) e `AddChoiceScreen`.
   - **`ui/intro/IntroScreen.kt` e `IntroPagination.kt`:** `SubcomposeLayout` mede 4 blocos e compõe de novo a página;
     os blocos são compostos duas vezes (medida e página): há estado, `rememberSaveable`, lançador de permissão ou efeito
     colateral duplicado? `SideEffect { onPages(...) }` dentro do `subcompose` (escrita de estado durante a composição
     da medição; risco de laço ou piscada); `page` fora do intervalo quando a fonte muda; rotação (o app é só retrato);
     botão "Começar sem permitir" e a permissão relida em `ON_RESUME`; o bloco do alarme falado aparece só com "Como
     alarme"; Voltar do sistema.
   - **`ui/tutorial/Tutorial.kt`:** balão com `ScrollHintColumn(fill = false)` dentro de `Box` limitado, `FlowRow`,
     `safeDrawingPadding`/`navigationBarsPadding`; o "Voltar" só do passo 2 em diante; `controller.index`; a pergunta
     inicial (`Offer`) com fonte 2,0×; `TutorialStepsTest` ainda reflete os passos.
   - **`FormViewModel`/`FormScreen`:** `unusualDose` e `acceptedUnusualDose` (a ordem em relação ao aviso de nome
     repetido; edição de um remédio que já tinha dose alta; `FixDraft` não passa pelo aviso), `DoseUnit.isUnusual`
     (`UNUSUAL_AMOUNT = 20`, texto livre "Outro" nunca avisa; "1 e meio" etc.), `DurationDaysField` (texto × `endDate`,
     `LaunchedEffect(start, end)` sobrescrevendo o que a pessoa digita, zero, vazio, 999+), `NewTimeSuggestion`,
     campo do nome com `singleLine = false`, lápis no cartão do horário, `daysCapitalized`.
   - **`ui/home/HomeScreen.kt`:** `notice` (aviso de 6 s), `inCard` e `pastCount` (o horário em curso fora de "já
     passaram"; o `NowMarker`; índice `firstAhead`), `RepeatNote` com `repeatEnabled`, linhas clicáveis do cartão
     "Próximo lembrete", `PrimaryButton(singleLine = true)`.
   - **`ui/navigation/GenkiApp.kt` e `NavLabelSize.kt`:** rótulos iguais pela medição de "Medicamentos" (margem de 24 dp;
     o "s" final chegou a ser cortado com 12 dp), `softWrap = false`, `UNDO_MILLIS = 15 s` com `withTimeoutOrNull`
     (cancelar a coroutine retira o aviso? o "Desfazer" ainda restaura com o mesmo id?).
   - **`ui/medications/MedicationsScreen.kt` / `domain/MedicationOrder.kt`:** a ordenação só na lista (Hoje, alarme e
     edição seguem a ordem de cadastro); `Desfazer` (DEC-055) restaura o mesmo id e a lista reordena.
   - **`SettingsControls.kt`:** textos novos do som e do alarme falado; nada que contradiga o comportamento (o alarme
     toca por até 1 minuto e a repetição continua: o texto curto omite isso; diga se vê problema).
4. **Regressão rápida (HANDOFF §7)** no emulador `Pixel_6_2` (API 35): cadastro manual completo, edição, exclusão com
   Desfazer, voz até a conferência dos nomes (a fala real não é verificável), Hoje com vários remédios, Ajustes.
   Repita com fonte 1,0× e 2,0× (`adb shell settings put system font_scale 2.0`; restaure ao fim).

## Regras de segurança desta revisão

- **Não opere o A31** (celular do autor, serial `RX8N80313SX`) e **não use `installDebug`** com mais de um aparelho
  conectado (instala em todos): use `adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk`.
- Se mexer no emulador, restaure banco, preferências, fonte e permissões ao fim.
- Não altere código nem faça commit. O único arquivo novo é o relatório.

## Relatório

Grave em **`docs/revisoes/REVISAO_REFINAMENTO_1.0.25_CODEX.md`**: tabela de achados (ID `RV-125-NN`, gravidade S1–S4,
arquivo:linha, problema, correção sugerida), o que foi executado e o resultado (build, testes, emulador), e **o que não
pôde verificar** (A31, TalkBack, fala real, alarme real, instalação limpa). Diferencie achado confirmado de suspeita.
Acrescente uma linha ao Diário do `docs/HANDOFF_APP.md` e um bloco em `docs/ENTREGAS.md` (identificando o Codex), sem
sobrescrever o que outros assistentes escreveram.
