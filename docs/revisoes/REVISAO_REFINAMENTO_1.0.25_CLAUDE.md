# Revisão do refinamento heurístico 1.0.25 — Claude Code (no lugar do Codex)

03/11/2026, Claude Code (Sonnet 5.5), a pedido do autor ("faça a revisão no lugar do Codex"). Roteiro:
`ROTEIRO_REVISAO_REFINAMENTO_1.0.25_CODEX.md`. Alvo: `main`, `d292a2a..14b2a4e` (1.0.24 → 1.0.25, versionCode 26).

**Limite desta revisão.** Quem revisa é o mesmo assistente que escreveu o código (DEC-026 prevê Claude implementa e Codex
revisa). A tentativa com o Codex (CLI, `codex exec`) rodou o build e começou o emulador, mas parou por limite de uso da
conta ("You've hit your usage limit", liberação anunciada para 04/11, 00:59) **sem gravar relatório nem achados**. Esta é
uma leitura de diff e uma conferência de regras, **não** uma segunda opinião independente: o Codex deve repetir a revisão
depois do limite, com o mesmo roteiro. Nada foi alterado no código.

## Executado

- `.\gradlew.bat assembleDebug test assembleRelease`: passou; **223 testes, 0 falhas** (rodei eu; o log do Codex também
  mostra dois `BUILD SUCCESSFUL`).
- `git diff d292a2a 14b2a4e --stat` em `reminders/`, `data/`, `domain/DraftValidator.kt`, `AndroidManifest.xml` e
  `app/schemas`: **vazio**. A lógica de lembretes, o validador, o manifest e o esquema não mudaram.
- Busca no diff (`app/src/main`) por `Tomado`, `INTERNET`, WorkManager, Hilt, DataStore, `LocalDate.now`, rede: só duas
  ocorrências, ambas esperadas: "O Genki não registra se o remédio foi tomado." (negação, P6) e "não acessa a internet"
  (Sobre, P21).
- Emulador Pixel_6_2 (API 35), durante o desenvolvimento, fonte 1,0×, 1,3× e 2,0×: P1, P2, P3, P19, P20, P21, P27;
  introdução em 2, 3, 4 e 5 páginas; balão do tutorial, rótulos da barra, botão Adicionar e Adicionar medicamento sem
  rolar. A31: balão do tutorial e os 7 passos com fonte 2,0× (instalação limpa).

## Regras do produto

| Regra | Resultado |
| --- | --- |
| Toda gravação pelo `DraftValidator` | OK. O aviso de quantidade e o "por quantos dias" ficam na interface; o término sai como `endDate` e passa pelo validador (`TreatmentDuration` repete a regra e há teste de equivalência) |
| "Entendi" não é ingestão | OK. O aviso novo nega o registro; sem "Tomado" nem histórico |
| Concentração ≠ dose; OCR | Sem mudança |
| Datas civis no fuso do aparelho | `TreatmentDuration` e `NewTimeSuggestion` não usam relógio; sem `LocalDate.now` novo |
| Sem rede, Hilt, WorkManager, DataStore, LLM | OK |
| Sem promessa clínica | OK. O aviso diz que o Genki não sabe a quantidade certa. O texto de Sobre diz "o que o reconhecedor faz com o áudio depende do aparelho" |

## Achados

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-125-01 | S3 (confirmado por leitura; **não reproduzido**) | `ui/intro/IntroScreen.kt:70-71` | `pageCount` começa em 2 e não é salvo. Se a Activity é recriada com a introdução numa página ≥ 3 (mudar o tamanho da fonte do sistema enquanto a introdução está aberta, ou a morte do processo), o `LaunchedEffect(pageCount)` roda com `pageCount = 2` antes da primeira medição e joga `page` para 1. A pessoa volta à página 2 | `pageCount` com `rememberSaveable`, ou só limitar `page` depois da primeira medição |
| RV-125-02 | S3 | `ui/form/FormViewModel.kt:333` | O aviso de quantidade alta abre toda vez que se salva, inclusive ao editar um remédio que já tem dose acima de 20 sem mudar a dose | Na edição, avisar só se a dose mudou em relação à gravada |
| RV-125-03 | S4 | `ui/home/HomeScreen.kt:196-201` | `LaunchedEffect(notice)` não reinicia a contagem se o mesmo texto é definido de novo: dois "Entendi" em menos de 6 s deixam o aviso do segundo sumir antes | Chave com um contador, ou `notice` com id |
| RV-125-04 | S4 (suspeita, **não verificada**) | `ui/form/FormScreen.kt:484` + `DraftValidator.clean()` | O nome agora é multilinha (`singleLine = false`); o teclado mostra "Próximo" e não insere quebra de linha, mas **colar** um texto com quebra pode gravar `\n` no nome, se `clean()` não a trata (não li `clean()` por inteiro) | Conferir `clean()`; trocar `\n` por espaço no nome |
| RV-125-05 | S4 | `ui/settings/SettingsControls.kt` | O texto curto de "Como alarme" não diz mais que o alarme toca por até 1 minuto, que aparece na tela bloqueada e que a repetição continua (decisão do autor: "poucas palavras"). A informação sumiu da interface | Se quiser, uma linha em Sobre ou no tutorial; decisão do autor |
| RV-125-06 | S4 | `docs/ROTEIRO_BATERIA_COMPLETA.md:96`, `docs/GUIA_INSTALACAO_TESTE.md` | Citam o texto antigo de "Como alarme"/"Como notificação" e a introdução de 2 páginas; README e guia ainda falam da 1.0.24 | Atualizar junto com a Release 1.0.25 |
| RV-125-07 | S4 | `ui/components/Components.kt`, `ui/add/AddChoiceScreen.kt`, `ui/form/FormScreen.kt` | Imports sem uso depois das trocas (`verticalScroll`, `rememberScrollState`, `heading`, `TextAutoSize`, `TextOverflow`) | Limpeza; só avisos do compilador |

Pontos que olhei e **não** achei problema (por leitura, sem prova no aparelho): `canScrollForward` antes da medição
(`maxValue == Int.MAX_VALUE` tratado); `weight(1f, fill = false)` com o `Box` limitado no balão e na pergunta do
tutorial; blocos da introdução compostos duas vezes (medida e página) sem lançador de permissão nem `rememberSaveable`
dentro deles (o estado foi içado para `IntroScreen`); `SideEffect` dentro do `subcompose`; Desfazer com
`withTimeoutOrNull` (cancelar retira o aviso; o `Dismissed` de um aviso novo não restaura nada); ordem entre aviso de
dose e de nome repetido no `FormViewModel.save`; `DurationDaysField` com zero, vazio, 999 e escolha pelo calendário;
`inCard`/`pastCount` na Hoje; `MedicationOrder` só na lista; `NavLabelSize` (margem de 24 dp depois do corte do "s").

## Não verificado

- **A31:** só o balão do tutorial e os passos com fonte 2,0×; não vi os itens P6–P8 (precisam de lembrete disparado),
  P9–P11 (instalação limpa com permissão negada), P13 (Desfazer na tela), P15–P16, P18, P22–P26, P28–P29 em tela.
- Alarme real e som, TalkBack (indicador de rolagem, "Voltar" do tutorial, lápis do horário, ordem de leitura da voz),
  fala real da voz, Doze, rotação.
- Regressão completa da seção 7 do handoff e voz até a conferência dos nomes: **não repeti** nesta revisão.
- Independência: ver "Limite desta revisão".

## Próximo passo

Autor decide se RV-125-01 e RV-125-02 entram numa 1.0.26 ou ficam como limitação (corte da DEC-057: 05/11, 12:00);
reenviar o roteiro ao Codex depois de 04/11, 00:59.
