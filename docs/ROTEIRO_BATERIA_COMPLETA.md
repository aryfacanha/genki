# Bateria completa de testes manuais — Genki no A31

Criado em 28/10/2026 por Claude Code (Opus 5.5), a pedido do autor: os casos de uso notáveis do app, do começo ao
fim, para um teste manual completo. Os resultados esperados vêm do código, da ESPEC e das revisões; **não foi
executado assim, de ponta a ponta, no aparelho.** Versão de referência: `main`/`8fe9e49` (código de `61d4c22`,
instalado no A31 em 28/10). Roteiros detalhados que esta bateria resume: `ROTEIRO_TESTE_DOSE_GRADES.md` (dose,
grades, volume) e `ROTEIRO_TESTE_VOZ.md` (frases da voz).

## Antes de começar

- Fonte do aparelho em 1,5× (a sua), mídia em 8 ou mais, "Não perturbe" desligado, A31 desbloqueado.
- Ajustes do Android → Apps → Genki → **Alarmes e lembretes: permitido** (sem isso o aviso pode atrasar).
- Para os lembretes (seção D), use um horário **3 a 5 minutos à frente** do relógio: no relógio do app, toque nos
  minutos e arraste o ponteiro até o minuto exato; confira na revisão antes de salvar.
- Por caso, anote **ok** ou **problema** (o que apareceu + print). Exclua no fim tudo o que for de teste (seção J).
- Os achados RV-DVC-01…05 da revisão do Codex foram corrigidos em 28/10 (casos E12, E13, G5–G7 marcados "corrigido"); se algum deles falhar, anote como achado.

## A. Abertura e navegação (M0)

| # | Faça | Deve acontecer |
|---|---|---|
| A0 | Primeira abertura depois de instalar esta versão | Introdução "1 de 4": Boas-vindas → Como cadastrar → Do seu jeito (som, avisar de novo, tempo, ouvir sozinho) → Permitir avisos → **Começar** leva à Hoje. Ao reabrir, não aparece de novo; Ajustes → "Ver a introdução de novo" mostra outra vez (DEC-041) |
| A1 | Abrir o Genki | Abre na **Hoje**, com a data por extenso; barra inferior Hoje, Medicamentos, Ajustes |
| A2 | Tocar nas três abas | Cada uma abre sua tela; a aba ativa tem fundo verde-claro atrás do ícone e rótulo em negrito |
| A3 | Medicamentos sem nenhum cadastro (se for o caso) | "Nenhum medicamento cadastrado."; o aviso fica na mesma altura do "Você ainda não possui lembretes." da Hoje (DEC-040) |

## B. Cadastro digitando (M1, RF01–RF03)

| # | Faça | Deve acontecer |
|---|---|---|
| B1 | Adicionar medicamento | Tela "Como você quer cadastrar?" com Usar voz, Digitar, Usar câmera |
| B2 | Digitar → Continuar sem preencher nada | Não avança; mensagem de erro do nome visível |
| B3 | Nome "Teste A", concentração "50 mg", Unidade **Comprimido**, **+** uma vez | Quantidade "1 e meio"; campos brancos sobre o fundo; pergunta "Quanto você toma por vez" em destaque |
| B4 | Continuar → **Dias específicos** sem marcar → Continuar | "Selecione pelo menos um dia." |
| B5 | Marcar Seg e Qua; Adicionar horário 08:00; Adicionar horário de novo e tentar 08:00 | O relógio avisa "Este horário já foi adicionado." e o OK fica apagado |
| B6 | Trocar o segundo horário para 20:00 → Continuar | Etapa 3, duração |
| B7 | "Tem data para terminar" e tentar escolher um término antes de hoje | Dias anteriores ao início desabilitados no calendário |
| B8 | Uso contínuo → Continuar para revisão | Revisão: Teste A · 50 mg · **1 comprimido e meio** · segunda e quarta · 08:00 e 20:00 · Uso contínuo |
| B9 | Na revisão, **Alterar** em Dias e horários → Todos os dias → Concluir alteração | Volta à revisão com "Todos os dias" |
| B10 | **Confirmar e salvar** | No primeiro cadastro sem permissão: "Permitir notificações?" (permita). Aviso "Medicamento adicionado" e o cartão aparece na lista |
| B11 | Cancelar no meio de um cadastro novo já preenchido | "Descartar cadastro?" — Continuar editando mantém; Descartar sai |

## C. Lista, detalhes, edição e exclusão (M1)

| # | Faça | Deve acontecer |
|---|---|---|
| C1 | Medicamentos → Teste A | Detalhes com quanto toma, dias, horários, duração, "Lembretes: Ativos" |
| C2 | Editar → trocar a quantidade para **2** → voltar sem salvar | "Descartar alterações?" |
| C3 | Editar → quantidade 2 → Salvar alterações | Detalhes com "2 comprimidos" |
| C4 | Editar de novo | Abre com Comprimido e quantidade 2, sem rolar sozinho |
| C5 | Desativar lembretes | Lista mostra "Lembretes desligados"; **Ativar lembretes** volta ao normal (deixe ativo) |
| C6 | Fechar o app (tirar dos recentes) e abrir de novo | Teste A continua lá, igual (dados locais, RNF06) |

## D. Lembretes (M2, RF09–RF11) — use um horário 3–5 min à frente

Crie "Teste B", 1 comprimido, todos os dias, um horário H = agora + 3 a 5 min, uso contínuo.

| # | Faça | Deve acontecer |
|---|---|---|
| D1 | Esperar até H com o app fechado | Notificação "Lembrete das H" / "Teste B · 1 comprimido" com **Entendi** e **Adiar N min**; som de notificação comum |
| D2 | Abrir a Hoje durante o aviso | Cartão **Agora** com Entendi e Adiar; abaixo, o próximo horário |
| D3 | Não responder por N min (Ajustes: 10 min por padrão) | O aviso repete, até 3 vezes |
| D4 | **Adiar** (na notificação ou no cartão) | Cartão "Lembrete adiado" / "Novo alerta às …" / "Horário original: H"; o aviso volta no horário novo com "(adiado)"; o cartão adiado é **verde-claro com borda**, diferente do verde-escuro do que está tocando (DEC-040) |
| D5 | **Entendi** | Notificação e cartão somem; nada de "Tomado" nem histórico de tomada |
| D6 | Tocar numa notificação de lembrete | Abre a Hoje (se estiver no meio de um cadastro, ele é preservado) |
| D7 | Hoje depois de H | H aparece como "Passou"; linha "Agora · HH:MM" separa passados e próximos; tocar num horário abre os Detalhes |
| D8 | Ajustes: desligar a categoria "Lembretes de medicamentos" nas notificações do Android | A Hoje mostra o aviso de notificações bloqueadas com **Resolver em Ajustes**, que leva ao ajuste certo; religar faz o aviso sumir |
| D9 | Ajustes do app | Repetição ("Avisar de novo se eu não responder"), "Tempo para avisar de novo" 10/20/30 e o estado das permissões ("Permitidas") |
| D10 | Opcional: reiniciar o A31 com um horário futuro cadastrado | O lembrete seguinte ainda chega (alarmes reagendados ao ligar) |
| D11 | Ajustes → Som do lembrete **Como alarme**; celular no silencioso; lembrete 3–5 min à frente | Toca com o som de alarme mesmo no silencioso (Não perturbe pode bloquear). Voltar para **Como notificação** depois (DEC-041) |

## E. Cadastro por voz (M3, RF12–RF19)

Frases completas e variações em `ROTEIRO_TESTE_VOZ.md`; aqui, o essencial.

| # | Faça / diga | Deve acontecer |
|---|---|---|
| E1 | Usar voz → microfone → "Losartana 50 miligramas, um comprimido, todos os dias às 8 da manhã, uso contínuo" | Vai direto para "Confira antes de salvar", sem perguntas |
| E2 | "Losartana" e responder **pelos botões** (dose, dias, horários, duração) | Uma pergunta por vez, **falada em voz alta**; botões em grade; revisão completa no fim |
| E3 | Na pergunta dos horários, **Escolher no relógio** → 07:30 → Adicionar outro horário → OK → Confirmar horários | Relógio abre em 08:00 e depois 12 h após o último; "07:30 e 19:30" no resumo |
| E4 | Na pergunta dos dias, **Escolher os dias** → Segunda e Sexta → Confirmar dias | "segunda e sexta" |
| E5 | "Tomo losartana 50 mg um comprimido às 8 e metformina 850 mg um comprimido às 8 e às 20" | Dois medicamentos; perguntas dos dois; **Confirmar e salvar todos** → "2 medicamentos adicionados" |
| E6 | Durante uma pergunta: "Algo errado? Corrigir por voz" → "na verdade às 10 da noite" | "Confira a correção" com 22:00; **Manter correção** aplica, **Desfazer** (ou voltar) desfaz |
| E7 | Na revisão, **Corrigir** num medicamento | Abre o formulário preenchido; "Concluir correção" volta à revisão |
| E8 | Só palavras sem dado: "oi, tudo beleza, obrigado" | "Não entendi. Tente de novo." (palavra desconhecida, como "banana azul", vira nome: limitação conhecida, a revisão mostra) |
| E9 | **Prefiro digitar** numa pergunta | Formulário com o que já foi entendido |
| E10 | Voltar com algo já entendido | "Descartar cadastro?" |
| E11 | Mídia em 2 ao entrar na voz | Aviso "O volume está baixo…" no meio da tela, some em ~4 s |
| E12 | "Metformina três vezes ao dia" → na pergunta do primeiro horário, "Escolher no relógio" → 07:30 | Aparece "Horários do dia: 07:30, 15:30 e 23:30" com os três horários em caixas; tocar num deles (23:30) e escolher 22:00 troca só esse; "Escolher outro primeiro horário" recomeça. Confirmar → a revisão mostra os três como ficaram. (RV-DVC-03; DEC-040) |
| E13 | Abrir o relógio na pergunta dos horários e tocar "Algo errado? Corrigir por voz" | O seletor some e aparece "Diga a correção"; depois de Desfazer ou voltar, a pergunta volta sem o seletor aberto. (RV-DVC-05, corrigido) |
| E14 | "Losartana 50 miligramas, um comprimido, todos os dias às 8 da manhã, **use** contínuo" | Um medicamento só, com Uso contínuo (não cria "Use"). (DEC-040) |
| E15 | Na pergunta da dose, **Escolher a quantidade** → Unidade ml → + → Confirmar quantidade | Menu e − número + como no formulário; o cartão mostra "1,5 ml" e segue para os dias (DEC-040) |
| E16 | Com dois medicamentos entendidos, tocar no **X** de um deles | "Tirar …?" com Tirar/Cancelar; Tirar remove só esse (DEC-040) |
| E17 | Responder uma pergunta da voz e **não tocar no microfone** | Ao terminar de falar a pergunta seguinte, o app começa a ouvir sozinho ("Ouvindo…"). Se ninguém falar, aparece "Não ouvi nada…" e ele **não** volta a ouvir sozinho (DEC-040) |
| E18 | Durante a escuta automática, tocar num botão de resposta ou em "Escolher…" | A escuta para; a resposta tocada vale e nada é ouvido para a pergunta seguinte (DEC-040) |
| E19 | Ouvir as perguntas com atenção (volume alto) | Antes de cada pergunta, o app fala o que acabou de anotar: "Anotei 2 comprimidos. Em quais dias…"; na revisão, "Anotei … Confira antes de salvar." (DEC-040) |
| E20 | Ajustes → desligar "Na voz, ouvir sozinho depois da pergunta" e cadastrar por voz | O microfone só abre com toque (DEC-041) |

## F. Cadastro pela câmera (M4, RF20–RF23)

| # | Faça | Deve acontecer |
|---|---|---|
| F1 | Usar câmera → permitir → apontar para uma caixa de remédio → **Tirar foto** | "Lendo a embalagem…" e depois "Confira o que eu li" com nome e concentração editáveis |
| F2 | Conferir o que foi lido | **Nunca** preenche dose, dias, horários ou duração (só nome e concentração) |
| F3 | **Continuar por voz** | A voz começa pela pergunta da dose |
| F4 | Repetir e **Continuar digitando** | Formulário com nome e concentração preenchidos |
| F5 | Foto de algo sem texto (parede) | "Não consegui ler a embalagem." com Tentar de novo e Digitar |
| F6 | Anotar quantas embalagens reais leram certo o nome e a concentração (meta da ESPEC: 20 ensaios) | Registro para a monografia; não é aprovado/reprovado aqui |

## G. Dose (DEC-037–039) — detalhado em `ROTEIRO_TESTE_DOSE_GRADES.md` §A/B

| # | Faça | Deve acontecer |
|---|---|---|
| G1 | Comprimido → − | "meio"; − apagado; revisão "meio comprimido" |
| G2 | ml → + várias vezes | 1, 1,5, 2…; revisão "2,5 ml" etc. |
| G3 | Comprimido 2 → Cápsula; Comprimido meio → Cápsula | 2 continua; meio fica vazio |
| G4 | Outro → "1 colher de sopa" | Revisão "1 colher de sopa" |
| G5 | Gota → tocar no número → 120 → **+** | Continua 120 e o + fica apagado (o limite do + é 100; nada diminui). − vai a 119. (RV-DVC-01, corrigido) |
| G6 | ml → tocar no número → 0,25 → **+**, depois **−** | 0,75 e de volta 0,25; com 0,25 o − fica apagado. (RV-DVC-02, corrigido) |
| G7 | Medicamento antigo com "1/2 comprimido" → editar → **+** | Vira 1 ("1 comprimido"). (RV-DVC-04, corrigido) |

## H. Acessibilidade e legibilidade (RNF01–RNF04)

| # | Faça | Deve acontecer |
|---|---|---|
| H1 | Fonte do Android no máximo (2,0×): Hoje, lista, formulário (menu aberto, contador), voz, revisão | Nada cortado; título "Novo medicamento" em duas linhas sem partir palavra; rolar quando necessário |
| H2 | Fonte em 1,0× nas mesmas telas | Tudo inteiro, sem encolher sem necessidade |
| H3 | TalkBack ligado: barra inferior, caixas de dias, menu de unidade, − e + | Lê nomes, "marcado/não marcado", "Diminuir"/"Aumentar" e o valor; anote o que soar confuso |
| H4 | TalkBack na voz com mídia baixa | Lê o aviso de volume |
| H5 | Sob luz forte, olhar campos, títulos e botões | Campos (brancos com borda) distintos de títulos; textos legíveis |

## I. Robustez

| # | Faça | Deve acontecer |
|---|---|---|
| I1 | Sem internet (modo avião): cadastro digitando e lembretes | Funcionam (app não usa rede); a voz pode depender do reconhecedor do Google |
| I2 | Girar a tela ou trocar de app no meio do formulário e voltar | O que foi preenchido continua |
| I3 | Negar a permissão do microfone ou da câmera | "Sem acesso ao microfone. Você pode permitir nas configurações ou digitar." / "Sem acesso à câmera. …" com Digitar e Voltar; o app não fecha |

## J. Limpeza

Excluir Teste A, Teste B e os cadastros da voz e da câmera: Detalhes → **Excluir medicamento** → "Excluir …?" →
Excluir → "Medicamento excluído". Voltar fonte, volume e permissões ao normal.

## Fora desta bateria

Precisão clínica (o app não promete), reconhecimento offline universal, "dia sim, dia não" e "por um mês" na voz (a
tela pergunta), avaliação com participantes (feita pelo protocolo do TCC, não por este roteiro).
