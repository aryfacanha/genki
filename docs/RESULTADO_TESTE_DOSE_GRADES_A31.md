# Resultado — roteiro da dose, grades e aviso de volume no A31 (Claude Code, 28/10/2026)

Roteiro: `docs/ROTEIRO_TESTE_DOSE_GRADES.md`. Aparelho: Samsung A31 (SM-A315G, Android 12), versão `011d4e8`
(instalada às 12:09). Operado por Claude Code (Opus 5.5) pelo ADB, das 12:13 às 12:45, com toques simulados e leitura
da árvore de acessibilidade e de capturas (`build/a31_*.png`, não versionadas). Autorizado pelo autor, que falou
"Losartana" para a parte da voz. **Não substitui o aceite do autor** (ESPEC S5): o autor revê os pontos marcados.

Estado do aparelho: fonte do autor **1,5×** (testes principais nela), 2,0× e 1,0× para G; mídia 10/15. No fim, fonte
1,5×, mídia 10/15 e "tela ligada no carregador" desligada, como estavam. "Teste" salvo para F e excluído. Rascunhos de
voz descartados. Nenhum crash do app no log.

## Resultado por caso

| Caso | Resultado | Observação |
|---|---|---|
| Q01 | ok | Em 1,5×: 2 colunas |
| Q02 | ok | Meio/1/2/3/Outra |
| Q03 | **ok com achado** | A mensagem existe, mas a tela não rola até ela (A31-02) |
| Q04, Q05 | ok | "meio comprimido", "2 comprimidos" na revisão |
| Q06 | **ok com achado** | Começa em 1; + vai de meio em meio até 4; "4 comprimidos". O − 1 + aparece abaixo da borda da tela (A31-03) |
| Q07 | ok | 1 → "1 e meio" → "1 comprimido e meio" |
| Q08 | ok | "Quantas cápsulas", "Quantas gotas", "Quantos ml" e quantidades certas |
| Q09 | ok | "1 colher de sopa" |
| Q10 | ok | − apagado em meio comprimido e em 1 cápsula |
| Q11 | ok | "4,5 ml"; "21 gotas" |
| Q12 | ok | Teclado numérico com vírgula; "40 gotas" |
| T01–T09 | ok | Todos como esperado |
| D01–D03 | ok | Caixas não mudam de lugar; "Selecione pelo menos um dia." |
| D04, G03 | ok | Domingo…Sábado por extenso; "Confirmar dias" só depois de marcar; "segunda e sexta" |
| G01, G02, G04, G05 | ok | Grades certas; "meio comprimido" entendido |
| G07 | ok | Relógio abre em 08:00; caixa marcada + "Adicionar outro horário" + "Confirmar horário" |
| G08 | ok | Segundo relógio abre 12 h depois (20:00) |
| G09 | ok com observação | "Este horário já foi adicionado." e OK apagado; o terceiro relógio já abre num horário da lista (A31-06) |
| G10 | **achado** | Com o relógio aberto, um toque no botão rápido "20:00" respondeu a pergunta, mas o seletor de horários continuou embaixo da pergunta seguinte (A31-01) |
| G06, G10 (Metformina) | não feito | Precisa de nova fala do autor |
| A01–A05 | ok | Aviso aparece, some em ~4 s, não volta sem mexer, volta após subir e baixar, some ao subir; sem aviso com mídia 8 |
| F01–F04 | ok | Outra/4; Meio; Outro "1 colher de sopa"; Outra/"1 e meio" |
| X01 | **ok com achado** | Unidades, quantidades e dias cabem em 2,0×; o título "Novo medicamento" quebra no meio da palavra (A31-04) |
| X02 | parcial, com achado | Relógio e contador cabem em 2,0×; o rótulo "Outra quantidade" quebra e invade a borda (A31-05). Grades da voz em 2,0× não feitas (precisam de fala) |
| X03 | ok | Aviso inteiro e legível em 2,0× |
| X04 | ok | Em 1,0×, unidades em 3 colunas; "✓ Comprimido" cabe |
| L01, L02 | não feito | TalkBack: só o autor ouve |

## Achados

| ID | Gravidade | Onde | Problema | Correção sugerida |
|---|---|---|---|---|
| A31-01 | S2 | `VoiceScreen.kt` (`picking`/`typing` do `Asking`) | Respondida a pergunta por outro meio (botão rápido ou fala) com o relógio aberto, o seletor de horários fica na pergunta seguinte; confirmar ali manda horários como resposta da pergunta errada. Novo (DEC-038) | Zerar `picking`/`typing` quando a pergunta muda |
| A31-02 | S3 | `FormScreen.kt` (rolagem até o erro só na edição) | No cadastro novo com fonte grande, Continuar com erro não mostra a mensagem; parece que nada aconteceu. Preexistente (M1) | Levar a mensagem de erro até a tela também nas etapas |
| A31-03 | S3 | `FormScreen.kt` (`AmountStepper`) | Em 1,5×, ao tocar em Outra o − número + fica abaixo da borda; é preciso rolar para ver. Novo (DEC-038) | Rolar até o contador quando ele aparece |
| A31-04 | S3 | `GenkiTopBar` | Em 2,0×, título "Novo medicamento" com "Cancelar" ao lado quebra no meio da palavra ("medicament / o"). Preexistente (DEC-036) | Título sem quebrar palavra (encolher fonte, como na barra inferior) |
| A31-05 | S4 | `FormScreen.kt` (`AmountStepper`) | Em 2,0×, o rótulo "Outra quantidade" quebra em duas linhas sobre a borda do campo. Novo (DEC-038) | Rótulo curto ("Quantidade") ou fora do campo |
| A31-06 | S4 | `VoiceScreen.kt` (`TimesPicker`) | Com 08:00 e 20:00 escolhidos, o próximo relógio abre em 08:00, já com o aviso de repetido. Novo (DEC-038) | Abrir num horário livre |

## Reparos (Claude Code, 28/10, instalados no A31 às 12:51)

| ID | Reparo | Conferido |
|---|---|---|
| A31-01 | `typing`/`typed`/`picking` da voz guardados por pergunta (`rememberSaveable(draftId, field)`) | Não em tela (precisa de fala); conferir com o autor |
| A31-02 | Cada mensagem de erro do formulário tem um `BringIntoViewRequester`; nas etapas, o Continuar leva a primeira até a tela | A31 1,5×: "Informe quanto você toma por vez." visível após Continuar |
| A31-03 | Tocar em Outra rola até o − número + (não rola ao abrir uma edição já em Outra) | A31 1,5×: contador à vista |
| A31-04 | Título da barra do topo em até 2 linhas, encolhendo só o necessário (como a barra inferior) | A31 2,0×: "Novo / medicamento"; 1,5×: uma linha |
| A31-05 | Rótulo do contador "Quantidade", uma linha | A31 2,0×: inteiro |
| A31-06 | `VoiceQuestions.nextClockTime`: pula horário já escolhido (teste) | Teste unitário; em tela com o autor |

## Sessão de voz com o autor (28/10, 12:56–13:02, versão `f5188a0`)

O autor falou; o Claude conduziu os toques. Reconhecedor do aparelho: "Losartana" e "metformina duas vezes ao dia"
entendidos (latência ~110 ms).

| Caso | Resultado | Observação |
|---|---|---|
| A31-06 | **fechado** | Com 08:00 e 20:00 escolhidos, o terceiro relógio abre às 09:00, sem aviso |
| A31-01 | **fechado** | Relógio aberto + botão rápido "12:00": a voz passa à duração sem o seletor de horários |
| G10 (Metformina) | ok | Em 2,0×: pergunta do primeiro horário → relógio 07:30 → "07:30 e 19:30" |
| G06 | ok | "Prefiro digitar" → formulário com Comprimido + Meio |
| X02 (voz) | ok | Em 2,0×: dose, primeiro horário e duração em uma coluna (horários em 3), legíveis; é preciso rolar para "Confirmar horário" e para parte das respostas |

Fonte 1,5×, mídia 10/15 e tela restaurados; rascunhos descartados; sem crash.

## Falta (autor)

Perguntas faladas em voz alta (o autor responde); TalkBack (L01–L02).
