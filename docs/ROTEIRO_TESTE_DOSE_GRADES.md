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

## Como usar

- Formulário: **Hoje → Adicionar medicamento → Digitar**. Voz: **→ Usar voz**.
- Nada precisa ser salvo, exceto em F01–F04 (edição). Para sair sem salvar: **Cancelar → Descartar**.
- Anotar por caso: **ok** ou **problema** (o que apareceu + print).
- "Grade" = caixas do mesmo tamanho, alinhadas em colunas, com ✓ só na marcada. Marcar ou desmarcar não pode
  mudar o tamanho nem a posição de nenhuma caixa.

## A. Unidade e quantidade (etapa 1 do formulário)

| # | Faça | Deve acontecer |
|---|---|---|
| Q01 | Olhar "Quanto você toma por vez" antes de tocar | Grade com Comprimido, Cápsula, Gota, ml, Outro; nenhuma marcada; ainda não aparece "Quantos…" |
| Q02 | Tocar **Comprimido** | ✓ só em Comprimido; aparece "Quantos comprimidos" com Meio, 1, 2, 3, Outra (nenhuma marcada) |
| Q03 | Nome "Teste", sem quantidade → **Continuar** | Não avança: "Informe quanto você toma por vez." |
| Q04 | Tocar **Meio** → Continuar → … até a revisão | Revisão mostra dose **"meio comprimido"** |
| Q05 | Comprimido → **2** | Revisão: "2 comprimidos" (plural) |
| Q06 | Comprimido (nada marcado) → **Outra** | ✓ em Outra; aparece **− 1 +** ("Outra quantidade"). Tocar + seis vezes: 1 e meio, 2, 2 e meio, 3, 3 e meio, 4; revisão "4 comprimidos" |
| Q07 | Comprimido → marcar **1** → **Outra** → **+** uma vez | O contador começa em 1 (a marcada) e vai para "1 e meio"; revisão **"1 comprimido e meio"** |
| Q10 | Comprimido → Meio → Outra; Cápsula → 1 → Outra | O **−** fica apagado (não desce de meio comprimido nem de 1 cápsula) |
| Q11 | ml → **5** → Outra → **−** | "4,5"; revisão "4,5 ml". Gota → 20 → Outra → + → "21"; revisão "21 gotas" |
| Q12 | Gota → Outra → **tocar no número**, apagar e digitar "40" | Abre o teclado numérico; revisão "40 gotas" |
| Q08 | Tocar **Cápsula**, **Gota** e **ml** (um de cada vez) | Títulos "Quantas cápsulas", "Quantas gotas", "Quantos ml". Quantidades: Cápsula 1/2/3/Outra; Gota 5/10/15/20/Outra; ml 5/10/15/Outra |
| Q09 | Tocar **Outro** e digitar "1 colher de sopa" | Campo "Como você toma"; revisão "1 colher de sopa" |

## B. Trocar de unidade (DEC-037)

A quantidade só continua se a nova unidade tiver o mesmo botão; se não tiver, fica sem marcação e é preciso
escolher de novo. T01–T07 já passaram no emulador (Codex, 28/10); no A31, ainda não.

| # | Faça | Deve acontecer |
|---|---|---|
| T01 | Comprimido + **2** → tocar Cápsula | "2" continua marcado; revisão "2 cápsulas" |
| T02 | Comprimido + **Meio** → tocar Cápsula | **Nenhuma quantidade marcada** (sem "meio cápsula"); Continuar → "Informe quanto você toma por vez." |
| T03 | Gota + **20** → tocar ml | Nenhuma quantidade marcada (sem "20 ml") |
| T04 | ml + **5** → tocar Gota | "5" continua; "5 gotas" |
| T05 | Comprimido + Outra "4" → tocar Cápsula | Nenhuma quantidade marcada, sem o campo "Outra quantidade" |
| T06 | Comprimido + Outra "4" → tocar **Comprimido** de novo | Nada muda: "4" continua no contador |
| T07 | Comprimido + 2 → tocar **Outro** | Campo livre já com "2 comprimidos" |
| T08 | Comprimido + 2 → Cápsula → **Outro** → trocar o texto por "1 colher de sopa" → fechar o teclado → tocar **Cápsula** | Nenhuma quantidade marcada; Continuar → "Informe quanto você toma por vez." (antes do reparo de RV-UN-01, o "2" voltava e a dose virava "2 cápsulas") |
| T09 | Comprimido + 2 → **Outro** → **apagar** todo o texto → tocar **Comprimido** | Nenhuma quantidade marcada (antes do reparo, o "2" voltava) |

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
| G06 | **Prefiro digitar** na pergunta dos horários (com dose "meio comprimido" já entendida) | Formulário com Comprimido + **Meio** marcados |
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
| F01 | 4 comprimidos | Comprimido + **Outra** marcados, contador em "4" |
| F02 | meio comprimido | Comprimido + **Meio** |
| F03 | 1 colher de sopa | **Outro**, campo "1 colher de sopa" |
| F04 | 1 comprimido e meio (Q07) | Comprimido + **Outra**, contador em "1 e meio" (antes abria em Outro, texto livre) |

Ao terminar, excluir "Teste".

## G. Fonte 2,0× (Configurações → Tela → Tamanho e estilo da fonte, no máximo)

| # | Faça | Deve acontecer |
|---|---|---|
| X01 | Repetir Q02, Q08 e D01 | Rótulos inteiros (sem cortar "Comprimido" nem os dias); menos colunas é aceitável |
| X02 | Repetir G01–G05, G07 e Q06 | Botões legíveis, nenhum texto cortado; relógio e − número + cabem na tela |
| X03 | Repetir A01 | Aviso inteiro e legível |
| X04 | Voltar a fonte ao normal. Em 1,0×, olhar Q01 | Comprimido/Cápsula/Gota em 3 colunas. Se cair para 2, anotar: decidir rótulo menor ou aceitar |

## H. Opcional — TalkBack (ninguém conferiu ainda)

| # | Faça | Deve acontecer |
|---|---|---|
| L01 | Passar o dedo nas caixas de unidade e de dias | Lê o nome e se está marcada ou não marcada |
| L02 | Repetir A01 | Lê o aviso de volume |
