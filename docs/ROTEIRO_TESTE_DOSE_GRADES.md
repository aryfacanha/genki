# Roteiro de teste manual — quantidade da dose, grades e aviso de volume (A31)

Criado em 28/10/2026 por Claude Code (Opus 5.5), a pedido do autor, para as mudanças de 27–28/10: quantidade em um
toque (`33a33ab`), opções em grade (`a96184b`), aviso de volume no meio da tela (`ee23734`) e quantidade ao trocar a
unidade (DEC-037, `a2680da`). Versão instalada no A31 em 28/10, 10:25. Os resultados esperados vêm do código e dos
testes; **não foi executado no aparelho.**

Atualizado em 28/10 a partir da revisão do Codex (`revisoes/REVISAO_TROCA_UNIDADE_CODEX.md`): T01–T07 conferidos
por ele no emulador; acrescentados T08–T09, do achado RV-UN-01, **corrigido no mesmo dia** (T08/T09 conferidos pelo
Claude no emulador; A31 atualizado às 11:09).

Atualizado de novo em 28/10 com dois pedidos do autor: **"Outra" com − e +** (DEC-038; Q06–Q07 e Q10–Q12, F01 e
F04) e **relógio na pergunta dos horários da voz** (G07–G10). Conferidos pelo Claude no emulador; versão instalada
no A31 às 12:09 (`011d4e8`).

**Executado pelo Claude no A31 em 28/10 (12:13–12:45):** resultado e achados A31-01…06 em
`docs/RESULTADO_TESTE_DOSE_GRADES_A31.md` (reparados e reinstalados às 12:51; o rótulo do contador agora é
"Quantidade"). Sessão de voz (12:56–13:02) fechou A31-01/06, G06, G10 e X02. Falta o autor: G06, G10 (Metformina), voz em 2,0×, voz audível, TalkBack.

**28/10, DEC-039:** a grade de unidades e os botões Meio/1/2/3/Outra saíram; a unidade fica num menu e a quantidade
no − número + logo abaixo. Seções A, B e F reescritas; G06 agora mostra Comprimido com quantidade "meio".

## Como usar

- Formulário: **Hoje → Adicionar medicamento → Digitar**. Voz: **→ Usar voz**.
- Nada precisa ser salvo, exceto em F01–F04 (edição). Para sair sem salvar: **Cancelar → Descartar**.
- Anotar por caso: **ok** ou **problema** (o que apareceu + print).
- "Grade" = caixas do mesmo tamanho, alinhadas em colunas, com ✓ só na marcada. Marcar ou desmarcar não pode
  mudar o tamanho nem a posição de nenhuma caixa.

## A. Unidade e quantidade (etapa 1 do formulário) — DEC-039: menu + − número +

| # | Faça | Deve acontecer |
|---|---|---|
| Q01 | Olhar "Quanto você toma por vez" antes de tocar | Campo "Unidade" vazio com seta; ainda sem quantidade |
| Q02 | Tocar **Unidade** → **Comprimido** | Menu com Comprimido, Cápsula, Gota, ml, Outro; depois "Comprimido" no campo e **− 1 +** ("Quantidade") logo abaixo, à vista |
| Q03 | Nome "Teste", sem unidade → **Continuar** | Não avança; a tela rola até "Informe quanto você toma por vez." |
| Q04 | Comprimido → **−** → … até a revisão | "meio"; revisão **"meio comprimido"** |
| Q05 | Comprimido → **+** duas vezes | "2"; revisão "2 comprimidos" |
| Q06 | Comprimido → **+** seis vezes | 1 e meio, 2, 2 e meio, 3, 3 e meio, 4; revisão "4 comprimidos" |
| Q07 | Comprimido → **+** uma vez | "1 e meio"; revisão **"1 comprimido e meio"** |
| Q08 | Escolher Cápsula, Gota e ml num cadastro novo (um de cada vez) | Começam em 1; + anda de 1 em 1 (cápsula, gota) e de 0,5 em 0,5 (ml) |
| Q09 | Unidade **Outro** e escrever "1 colher de sopa" | Campo "Como você toma"; revisão "1 colher de sopa" |
| Q10 | Meio comprimido; 1 cápsula; 1 gota; 0,5 ml | O **−** fica apagado |
| Q11 | ml → tocar no número → "5" → **−** | "4,5"; revisão "4,5 ml". Gota → "20" → + → "21 gotas" |
| Q12 | Gota → tocar no número, apagar e digitar "40" | Teclado numérico; revisão "40 gotas" |

## B. Trocar de unidade (DEC-037)

A quantidade só continua se for uma das comuns das duas unidades (1, 2 ou 3 entre comprimido e cápsula; 5, 10 ou 15
entre gota e ml); senão o campo fica vazio e é preciso escolher de novo (+ começa em um passo).

| # | Faça | Deve acontecer |
|---|---|---|
| T01 | Comprimido **2** → Cápsula | "2" continua; revisão "2 cápsulas" |
| T02 | Comprimido **meio** → Cápsula | Quantidade vazia (sem "meio cápsula"); Continuar → "Informe quanto você toma por vez." |
| T03 | Gota **20** → ml | Vazia (sem "20 ml") |
| T04 | ml **5** → Gota | "5" continua; "5 gotas" |
| T05 | Comprimido **4** → Cápsula | Vazia |
| T06 | Comprimido 4 → escolher **Comprimido** de novo no menu | Nada muda: "4" |
| T07 | Comprimido 2 → **Outro** | Campo livre já com "2 comprimidos" |
| T08 | Comprimido 2 → Cápsula → **Outro** → trocar o texto por "1 colher de sopa" → **Cápsula** | Quantidade vazia; Continuar → "Informe quanto você toma por vez." |
| T09 | Comprimido 2 → **Outro** → **apagar** todo o texto → **Comprimido** | Quantidade vazia |
| T10 | Comprimido 2 → Outro (sem mexer no texto) → Comprimido | "2" continua |

## C. Dias específicos (etapa 2 do formulário)

| # | Faça | Deve acontecer |
|---|---|---|
| D01 | Tocar **Dias específicos** | Grade com Dom, Seg, Ter, Qua, Qui, Sex, Sáb (até 4 por linha), nenhum marcado |
| D02 | Marcar Seg, Qua e Sex e depois desmarcar Qua | ✓ só nos marcados; as caixas não mudam de lugar nem de tamanho |
| D03 | Dias específicos sem nenhum dia → Continuar | "Selecione pelo menos um dia." |
| D04 | Na voz, "Escolher os dias" (G03) | Mesma grade, mas com os nomes inteiros (Segunda, Terça…) |

## D. Voz: respostas em grade

Diga só **"Losartana"** e responda pelos botões.

| # | Faça | Deve acontecer |
|---|---|---|
| G01 | Pergunta da dose | Grade: 1 comprimido, 2 comprimidos, meio comprimido, 1 cápsula, 10 gotas. Tocar **meio comprimido** → revisão "meio comprimido" |
| G02 | Pergunta dos dias | Grade: Todos os dias, Segunda a sexta, Fim de semana + "Escolher os dias" |
| G03 | **Escolher os dias** → Segunda e Sexta → **Confirmar dias** | "Confirmar dias" só aparece depois de marcar um dia; revisão "segunda e sexta" |
| G04 | Pergunta dos horários | Grade: 08:00, 12:00, 20:00, 08:00 e 20:00 |
| G05 | Pergunta da duração | Grade: Uso contínuo, Ainda não sei, Por 7 dias, Por 10 dias |
| G06 | **Prefiro digitar** na pergunta dos horários (com dose "meio comprimido" já entendida) | Formulário com Comprimido e quantidade "meio" |
| G07 | Pergunta dos horários → **Escolher no relógio** | Abre o relógio de 24 h em 08:00. Escolher 07:30 → OK: aparece a caixa "✓ 07:30", "Adicionar outro horário" e "Confirmar horário" |
| G08 | Em G07, **Adicionar outro horário** | O relógio já abre em 19:30 (12 h depois); OK → caixas 07:30 e 19:30; tocar numa delas a tira |
| G09 | Em G08, escolher de novo 07:30 no relógio | "Este horário já foi adicionado." e OK apagado |
| G10 | **Confirmar horários** | "Entendi até agora" com "07:30 e 19:30"; a voz passa à pergunta da duração. Diga "Metformina duas vezes ao dia" e, na pergunta do primeiro horário, escolha 07:30 no relógio → "07:30 e 19:30" |

## E. Aviso de volume (tela da voz)

O aviso aparece quando o volume de **mídia** está abaixo de 30 % (no A31, 4 de 15 ou menos). Para ver a barra de
mídia: botão de volume → abrir o painel (⋯) → barra com ♪.

| # | Faça | Deve acontecer |
|---|---|---|
| A01 | Mídia em 2 → entrar em Usar voz | Aviso no meio da tela: "O volume está baixo. Aumente para ouvir as perguntas."; some sozinho em ~4 s |
| A02 | Depois de A01, sem mexer no volume | O aviso não volta |
| A03 | Subir a mídia para 8 e baixar para 2 | O aviso volta e some de novo em ~4 s |
| A04 | Com o aviso na tela, subir a mídia para 8 | O aviso some na hora |
| A05 | Mídia em 8 → entrar em Usar voz | Nenhum aviso |

## F. Edição

Salve antes, pelo formulário, "Teste" com estas doses e depois abra **Medicamentos → Teste → Editar**.

| # | Dose salva | Ao editar, deve abrir com |
|---|---|---|
| F01 | 4 comprimidos | Comprimido no menu, quantidade "4" |
| F02 | meio comprimido | Comprimido, quantidade "meio" |
| F03 | 1 colher de sopa | **Outro**, campo "1 colher de sopa" |
| F04 | 1 comprimido e meio (Q07) | Comprimido, quantidade "1 e meio" |

Ao terminar, excluir "Teste".

## G. Fonte 2,0× (Configurações → Tela → Tamanho e estilo da fonte, no máximo)

| # | Faça | Deve acontecer |
|---|---|---|
| X01 | Repetir Q02 (menu aberto) e D01 | Menu e rótulos inteiros; título "Novo / medicamento" sem quebrar palavra |
| X02 | Repetir G01–G05, G07 e Q06 | Botões legíveis, nenhum texto cortado; relógio e − número + cabem na tela |
| X03 | Repetir A01 | Aviso inteiro e legível |
| X04 | Voltar a fonte ao normal. Em 1,0×, olhar Q02 | Menu e − número + inteiros |

## H. Opcional — TalkBack (ninguém conferiu ainda)

| # | Faça | Deve acontecer |
|---|---|---|
| L01 | Passar o dedo nas caixas de unidade e de dias | Lê o nome e se está marcada ou não marcada |
| L02 | Repetir A01 | Lê o aviso de volume |
