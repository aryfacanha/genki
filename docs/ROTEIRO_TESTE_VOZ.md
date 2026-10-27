# Roteiro de teste manual — cadastro por voz (A31)

Criado em 27/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Os resultados esperados foram conferidos
rodando cada frase no interpretador (`VoiceParser`) com data de 27/10/2026; o que depende da tela (perguntas,
botões, mensagens) foi tirado do código. **Não foi executado no aparelho.**

## Como usar

- Sempre começar em **Hoje → Adicionar medicamento → Usar voz → microfone**.
- Falar a frase da coluna **Diga** em ritmo normal. Se o reconhecedor ouvir outra coisa, anote o "Você disse"
  (é limite do reconhecedor do Google, não do Genki) e repita.
- **Não salvar** nos casos marcados "não salvar": tocar em voltar → **Descartar**. Nos outros, salvar e
  depois excluir em Medicamentos.
- Anotar por caso: **ok** ou **problema** (o que apareceu + print).

Na revisão, os campos aparecem como: dias "Todos os dias" ou "segunda, quarta e sexta"; horários
"08:00 e 20:00"; duração "Uso contínuo", "Ainda não sei" ou "De 27/10/2026 até …".

## A. Caminho principal

| # | Diga / faça | Deve acontecer |
|---|---|---|
| V01 | "Losartana 50 miligramas, um comprimido, todos os dias às 8 da manhã, uso contínuo" | Vai direto para "Confira antes de salvar", sem perguntas: Losartana · 50 mg · 1 comprimido · Todos os dias · 08:00 · Uso contínuo. **Confirmar e salvar** → "Medicamento adicionado" e aparece na Hoje |
| V02 | "Tomo losartana 50 mg um comprimido às 8 e metformina 850 mg um comprimido às 8 e às 20" | "Entendi até agora" com os dois. Pergunta dias e duração de cada um (4 perguntas). Responda "todos os dias" / "uso contínuo". Revisão com os dois; Metformina com 08:00 e 20:00. **Confirmar e salvar todos** → "2 medicamentos adicionados" |
| V03 | "Losartana" e depois responder **falando**: "um comprimido" → "todos os dias" → "às 8" → "uso contínuo" | Uma pergunta por vez, lida em voz alta: quanto toma, dias, horários, por quanto tempo. Revisão completa no fim. Não salvar |
| V04 | "Losartana" e responder **só pelos botões**: 1 comprimido → Todos os dias → 08:00 e 20:00 → Uso contínuo | Mesmo resultado, com 08:00 e 20:00. Não salvar |
| V05 | "Losartana"; na pergunta da dose, tocar **Digitar a resposta**, digitar "2 comprimidos" e **Enviar** | Dose = 2 comprimidos; segue para a próxima pergunta. Não salvar |

## B. Jeitos diferentes de falar (interpretador)

Todos "não salvar". Responda às perguntas que aparecerem com os botões.

| # | Diga | Deve entender |
|---|---|---|
| V06 | "Amoxicilina 500 mg um comprimido de 8 em 8 horas por 7 dias" | Pergunta "Qual é o primeiro horário de Amoxicilina no dia?" → diga "às 6" → **06:00, 14:00 e 22:00**; todos os dias; De 27/10/2026 até 02/11/2026 |
| V07 | "Metformina 850 mg um comprimido duas vezes ao dia" | Pergunta o primeiro horário → "às 8" → **08:00 e 20:00** |
| V08 | "Atenolol um comprimido de segunda a sexta às 8" | segunda a sexta (5 dias) · 08:00 |
| V09 | "Atenolol um comprimido segunda, quarta e sexta às 8" | segunda, quarta e sexta |
| V10 | "Atenolol um comprimido no fim de semana às 8" | sábado e domingo |
| V11 | "Atenolol um comprimido nos dias úteis às 8" | segunda a sexta |
| V12 | "Sinvastatina um comprimido às 8 da noite" | **20:00** |
| V13 | "Omeprazol um comprimido ao meio-dia" | 12:00 |
| V14 | "Omeprazol um comprimido às 8 e meia" | 08:30 |
| V15 | "Losartana um comprimido às 8 e às 20" | 08:00 e 20:00 |
| V16 | "Losartana um comprimido às 8 e 20" | **08:20** (regra da especificação: "8 e 20" é oito e vinte) |
| V17 | "Losartana um comprimido umas 9 da noite" | 21:00 |
| V18 | "Losartana meio comprimido às 8" | dose "meio comprimido" |
| V19 | "Losartana 1 comprimido e meio às 8" | dose "1 comprimido e meio" |
| V20 | "Dipirona 10 gotas às 8" | dose "10 gotas", sem concentração |
| V21 | "Xarope uma colher de sopa às 8" | dose "1 colher de sopa" |
| V22 | "Amoxicilina 250 mg por 5 ml, 5 ml de 8 em 8 horas" | **concentração 250 mg/5 ml; dose 5 ml** (concentração ≠ dose) |
| V23 | "Vitamina D3 uma cápsula às 8" | nome "Vitamina D3", dose "1 cápsula" |
| V24 | "Carbonato de cálcio um comprimido às 8" | nome "Carbonato de cálcio" inteiro |
| V25 | "Losartana um comprimido às 8 por 2 semanas" | De 27/10/2026 até 09/11/2026 |
| V26 | "Losartana um comprimido às 8 até dia 15" | até 15/11/2026 |
| V27 | "Losartana um comprimido às 8 até 30 de novembro" | até 30/11/2026 |
| V28 | "Losartana um comprimido às 8 ainda não sei" | duração "Ainda não sei" |

## C. Correções

| # | Faça | Deve acontecer |
|---|---|---|
| V29 | "Losartana um comprimido às 8 e às 20" e, numa **segunda fala**, "só às 8" | Horários = só 08:00 |
| V30 | "Losartana um comprimido às 8"; numa **segunda fala**, "na verdade é às 9" | 08:00 vira **09:00** |
| V31 | "Losartana às 8 e às 20, não, às 21" (uma fala só) | 08:00 e **21:00** |
| V32 | Numa fala só: "Losartana às 8, na verdade às 9" | **Achado conhecido:** hoje fica com 08:00 **e** 09:00 (a correção na mesma fala não troca). Confirme e anote; ver nota no fim |
| V33 | "Losartana"; na pergunta da dose, diga "o nome é atenolol" | Nome passa a Atenolol; continua perguntando a dose. Nenhum medicamento novo |
| V34 | "Losartana"; na pergunta dos horários, diga "umas 9 da noite" | 21:00 para Losartana; **não** cria medicamento novo |
| V35 | Numa pergunta, toque **Algo errado? Corrigir por voz** e diga "atenolol" | "Confira a correção" com o nome trocado. Toque **Desfazer** → volta igual, na mesma pergunta. Repita e toque **Manter correção** → nome trocado, segue as perguntas |
| V36 | Faça V02 até a revisão; toque **Corrigir por voz** e diga "na verdade a metformina é às 9" | "Confira a correção": Metformina 09:00 e 20:00; Losartana intacta. **Manter correção** |
| V37 | Na revisão, toque **Corrigir** num medicamento, mude a dose no formulário e conclua | Volta à revisão com a dose nova; o outro intacto |
| V38 | Na revisão com dois, toque **Remover da lista** num deles | Revisão fica com um só. Remover o último volta ao início da voz |

## D. O que o Genki não entende (deve perguntar, não chutar)

| # | Diga | Deve acontecer |
|---|---|---|
| V39 | "Losartana um comprimido às 8 por um mês" | Não preenche a duração; **pergunta** por quanto tempo |
| V40 | "Losartana um comprimido dia sim dia não às 8" | Não preenche os dias; **pergunta** os dias |
| V41 | "Losartana um comprimido às 8 até 31 de fevereiro" | Data impossível ignorada; **pergunta** a duração |
| V42 | "Losartana um comprimido às 8 até dia 1 de outubro" | Data já passou este ano → entende **01/10/2027**. Confira se isso faz sentido para você |
| V43 | "Bom dia" | "Não entendi. Tente de novo." |

## E. Saídas, erros e permissões

| # | Faça | Deve acontecer |
|---|---|---|
| V44 | Toque no microfone e fique em silêncio | "Não ouvi nada. Toque no microfone e fale de novo." |
| V45 | Toque no microfone e, enquanto ouve, toque de novo (**Parar de ouvir**) | Para sem travar; dá para falar de novo |
| V46 | Com **um** medicamento falado, toque **Prefiro digitar** | Abre o formulário (etapa 1) com o que foi dito preenchido |
| V47 | Com **dois** falados, toque **Prefiro digitar** | Vai para a revisão, com "Corrigir" em cada um |
| V48 | No meio das perguntas, toque em voltar | "Descartar cadastro?" → **Continuar cadastro** mantém tudo; **Descartar** sai e nada aparece em Medicamentos |
| V49 | Configurações → Apps → Genki → Permissões → Microfone: **Negar**; abra a voz e toque no microfone | "Sem acesso ao microfone. Você pode permitir nas configurações ou digitar." Digitar funciona. **Depois devolva a permissão** |
| V50 | Desligue a internet (modo avião) e fale V01 | Se o pacote offline de português estiver instalado, funciona; senão aparece erro do reconhecedor e dá para digitar. Anote qual aconteceu |
| V51 | Com o volume de mídia baixo e depois alto, abra a voz | As perguntas são lidas em voz alta em português (com volume alto) |

## Nota — achado ao preparar o roteiro (V32)

"Losartana às 8, na verdade às 9" **numa fala só** fica com 08:00 e 09:00. "Na verdade" só troca horários de falas
anteriores (V30 funciona); dentro da mesma fala os horários se somam. Os testes do interpretador não cobriam esse
caso. Correção ainda **não feita**: aguarda decisão do autor.
