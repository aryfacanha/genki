# Bateria completa de testes manuais — Genki no A31

Criado em 28/10/2026 e **reescrito em 30/10/2026** por Claude Code (Opus 5.5), a pedido do autor: todos os casos de
uso do app, do começo ao fim, para um teste 100% manual. Versão de referência: **1.0.16** (lote 1 do retorno de 30/10, DEC-049 e DEC-050:
`RESULTADO_BATERIA_A31_2026-10-30.md`), modo "Como alarme" no A31. Os resultados esperados vêm do código, das decisões (até a DEC-048) e
das revisões; **esta bateria ainda não foi executada assim, de ponta a ponta, no aparelho.** Resultado da rodada
anterior (28/10): `RESULTADO_BATERIA_COMPLETA_A31.md`. Frases da voz em detalhe: `ROTEIRO_TESTE_VOZ.md`; dose em
detalhe: `ROTEIRO_TESTE_DOSE_GRADES.md`.

Marcação: **[novo]** = mudou depois da bateria de 28/10 (vale testar primeiro se o tempo for curto). A seção K (alarme)
vem logo depois da D, na ordem de execução; os números dos casos mudaram em relação à bateria de 28/10.

## Antes de começar

- Fonte do aparelho em 1,3× ou 1,5× (a sua), mídia em 8 ou mais, "Não perturbe" desligado, A31 desbloqueado.
- Android → Apps → Genki → **Alarmes e lembretes: permitido** (sem isso o aviso pode atrasar).
- Lembretes (seções D e K): use um horário **3 a 5 minutos à frente**. No relógio, toque em **Digitar o horário** e
  digite a hora e o minuto exatos; confira na revisão antes de salvar.
- Use nomes de teste ("Teste A", "Teste B" …) para não confundir com a Losartana e a Rivaroxabana; exclua tudo no fim
  (seção M).
- Por caso, anote **ok** ou **problema** (o que apareceu + print). Para mim ou para o Codex, o mais útil é: número do
  caso, o que você fez, o que apareceu, horário.

## A. Primeira abertura, introdução e tutorial [novo]

A primeira abertura só reaparece reinstalando do zero (apagar dados do app), o que **apaga seus medicamentos**. Por
isso A1–A3 usam os atalhos de Ajustes; A0 é opcional.

| # | Faça | Deve acontecer |
|---|---|---|
| A0 | (Opcional, apaga os dados) Android → Apps → Genki → Armazenamento → Limpar dados; abrir o Genki | Introdução em 2 páginas: **Boas-vindas ao Genki** (o que faz e não faz; "Tudo fica só neste celular") e a dos avisos (som do lembrete Como notificação/Como alarme e **Permitir notificações**). **Começar** → pergunta **"Quer ver um tutorial rápido?"** na tela **e falada em voz alta**, com Agora não / Ver tutorial (DEC-045) |
| A0b | Ajustes → Voz → **Ativar assistente de voz** [novo] | Vem **desligado** e vale só para o tutorial: desligado, o tutorial não fala nem mostra alto-falante; ligado, lê cada passo e o alto-falante do balão **pisca enquanto fala** (DEC-049/050). A voz lê as perguntas de qualquer jeito |
| A1 | Ajustes → **Ver o tutorial** | Tela escurece, recorta o alvo e mostra balão "1 de 7". Passos: Adicionar medicamento, próximo lembrete, Usar voz, Digitar, Usar câmera (indo sozinho para a tela Adicionar e voltando), aba Medicamentos, aba Ajustes |
| A2 | No tutorial: **Próximo** em todos (inclusive arrastando um pouco o dedo ao tocar); tocar na área escura; Voltar do Android no passo 3 | Próximo sempre avança; toque na área escura não faz nada; Voltar volta um passo (3 → 2) |
| A3 | Abrir o tutorial de novo e tocar **Pular tutorial** no meio | Sai na tela em que estava, sem nada travado |
| A3b | Com o assistente ligado, abrir o tutorial [novo] | Cada passo é lido em voz alta; tocar no alto-falante cala e o silêncio continua nos passos seguintes; tocar de novo relê o passo. Ao abrir o tutorial outra vez, a voz volta |
| A4 | Abrir o Genki normalmente | Abre na **Hoje** com a data por extenso; barra inferior Hoje, Medicamentos, Ajustes; a aba ativa tem fundo verde-claro e rótulo em negrito |
| A5 | Ir em Medicamentos → Ajustes → Voltar do Android | Volta para a Hoje (não acumula telas) |

## B. Cadastro digitando (M1, RF01–RF03)

| # | Faça | Deve acontecer |
|---|---|---|
| B1 | Adicionar medicamento | "Como você quer cadastrar?" com Usar voz, Digitar, Usar câmera; **Cancelar** no topo volta para a Hoje [novo] |
| B2 | Digitar → Continuar sem preencher nada | Não avança; mensagem de erro do nome |
| B3 | Nome "Teste A", concentração "50 mg", Unidade **Comprimido**, **+** uma vez | Quantidade "1 e meio"; campos brancos; pergunta "Quanto você toma por vez" em destaque |
| B4 | Continuar → **Dias específicos** sem marcar → Continuar | "Selecione pelo menos um dia." |
| B5 | Marcar Seg e Qua; Adicionar horário → **Digitar o horário** → 08:00 → OK [novo] | Campos Hora e Minuto separados, rótulos inteiros; digitar "0","8" não pula sozinho para o minuto de forma errada; 08:00 aparece na lista |
| B6 | Adicionar horário → Digitar → tocar no meio do minuto já preenchido e digitar "30" [novo] | O minuto é **substituído** por 30 (não vira "0030" nem "003") |
| B7 | Ainda no diálogo, digitar 20:15 e **girar o celular** (rotação automática ligada) [novo] | 20:15 continua depois de girar |
| B8 | Digitar hora 24 ou minuto 60, ou deixar vazio | OK fica apagado |
| B9 | Adicionar horário igual a um já adicionado (08:00) | "Este horário já foi adicionado." e OK apagado |
| B10 | Deixar 08:00 e 20:00 → Continuar → "Tem data para terminar" e tentar um término antes de hoje | Dias anteriores ao início desabilitados |
| B11 | Uso contínuo → Continuar para revisão | Teste A · 50 mg · **1 comprimido e meio** · segunda e quarta · 08:00 e 20:00 · Uso contínuo |
| B12 | **Alterar** em Dias e horários → Todos os dias → Concluir alteração | Volta à revisão com "Todos os dias" |
| B13 | **Confirmar e salvar** | Aviso "Medicamento adicionado"; aparece na lista |
| B14 | Novo cadastro com nome **"teste a"** (minúsculas), unidade e quantidade → **Continuar** da etapa 1 [novo] | Logo ali: "**Teste A já está cadastrado**" com **Voltar e conferir** (fica na etapa 1) e **Continuar mesmo assim** (segue; ao salvar não pergunta de novo, grava uma vez só). Exclua a cópia |
| B15 | Cancelar no meio de um cadastro novo já preenchido | "Descartar cadastro?" — Continuar editando mantém; Descartar sai |

## C. Lista, detalhes, edição e exclusão (M1)

| # | Faça | Deve acontecer |
|---|---|---|
| C1 | Medicamentos → Teste A | Detalhes: logo abaixo do nome, faixa verde com sino "**Lembretes ativos**" [novo]; depois quanto toma, dias, horários, duração |
| C2 | Editar → quantidade **2** → voltar sem salvar | "Descartar alterações?" |
| C3 | Editar → quantidade 2 → Salvar alterações | Detalhes com "2 comprimidos" |
| C4 | Editar de novo sem mudar o nome → Salvar | Salva **sem** o aviso de nome repetido (o próprio não conta) |
| C5 | Desativar lembretes → Ativar lembretes | Faixa cinza "Lembretes desativados"; lista mostra "Lembretes desligados"; depois volta ao normal (deixe ativo) |
| C6 | Tirar o app dos recentes e abrir de novo | Teste A igual (dados locais, RNF06) |

## D. Lembretes como notificação (M2, RF09–RF11, RF24, RF25)

Ajustes → Som do lembrete → **Como notificação**. Crie "Teste B", 1 comprimido, todos os dias, horário H = agora + 3
a 5 min, uso contínuo.

| # | Faça | Deve acontecer |
|---|---|---|
| D1 | Esperar até H com o app fechado | Notificação "Lembrete das H" / "Teste B · 1 comprimido" com **Entendi** e **Adiar N min**; som de notificação comum; no silencioso, não toca |
| D2 | Abrir a Hoje durante o aviso | Cartão **Agora** (verde-escuro) com Entendi e Adiar; abaixo, o próximo horário |
| D3 | Não responder por N min (Ajustes: intervalo) | O aviso repete, até 3 vezes |
| D4 | **Adiar** | Cartão "Lembrete adiado" **verde-claro com borda**, "Novo alerta às …", "Horário original: H"; o aviso volta no horário novo com "(adiado)" |
| D5 | Adiar de novo → tirar o Genki dos recentes → **Android → Apps → Genki → Forçar parada** → abrir o Genki [novo] | O cartão "Lembrete adiado" continua e o aviso **ainda chega** no horário prometido, **uma vez só** (DEC-047) |
| D6 | **Entendi** | Notificação e cartão somem; nada de "Tomado" nem histórico de tomada |
| D7 | Tocar numa notificação de lembrete: (a) fora de um cadastro; (b) no meio de um cadastro | (a) Abre a Hoje; (b) continua no cadastro, sem perder nada (de propósito, DEC-032/033) |
| D8 | Hoje depois de H | H como "Passou"; linha "Agora · HH:MM"; tocar num horário abre os Detalhes |
| D9 | Dois ou três medicamentos de teste no **mesmo horário futuro** [novo] | Hoje: "Próximo lembrete · N medicamentos" com **Ver mais/Ver menos**; "Depois: … e mais N" |
| D10 | Esse horário chegar | Um cartão só com os N avisos e **Ver um por um / Ver juntos**; Entendi de um tira só ele |
| D11 | Desligar a categoria "Lembretes de medicamentos" nas notificações do Android | Hoje mostra "Os lembretes não vão aparecer" com **Resolver em Ajustes**, que abre Ajustes **já rolado até Permissões** [novo]; religar faz o aviso sumir. Abrir Ajustes pela aba continua no topo |
| D12 | Ajustes do app | "**Repetir o lembrete**" com "Se o lembrete ficar sem resposta, o Genki avisa outra vez depois do tempo escolhido abaixo, até 3 vezes." [novo]; "Tempo para avisar de novo" 10/20/30; Permissões "Permitidas"/"Permitidos" |
| D13 | Com um adiado pendente e um horário futuro, **reiniciar o A31** [novo] | Depois de ligar (sem abrir o app): o adiado ainda chega e o próximo horário também |

## K. Lembretes como alarme (DEC-048, reparo 1.0.12) [novo]

Ajustes → Som do lembrete → **Como alarme**. Texto da opção: "Toca como despertador por até 1 minuto … Sem resposta, o
aviso fica e a repetição toca de novo." Celular **no silencioso** (Não perturbe desligado). Crie "Teste C" e "Teste D"
no **mesmo horário** H (3–5 min à frente) e "Teste E" em outro horário.

| # | Faça | Deve acontecer |
|---|---|---|
| K2 | Apagar a tela e **bloquear** o A31; esperar H | A tela acende sozinha com a **tela de alarme** por cima do bloqueio: horário, Teste C e Teste D, "Entendi" de cada, **Entendi todos** e **Adiar todos N min**; **som de alarme alto, em loop, com vibração, mesmo no silencioso** |
| K3 | Na tela de alarme, **Entendi** só do Teste C | Teste C some da tela; **o som continua**; Teste D segue na tela |
| K4 | **Entendi** do Teste D | O som para e a tela fecha; na barra de notificações não sobra nada desses dois (nem "Alarme do Genki tocando") |
| K5 | Repetir com dois horários iguais; desta vez **puxar a barra de notificações** com o alarme tocando | Duas notificações de lembrete (Teste C e Teste D) **e** uma discreta "Alarme do Genki tocando". Entendi numa notificação tira só aquela; Entendi na outra para o som e não sobra nenhuma (RV-048-01/02) |
| K6 | Deixar tocar **sem responder** | O som para em ~1 minuto; as notificações e o cartão Agora da Hoje continuam; a repetição volta a tocar depois do intervalo |
| K7 | **Adiar todos** | Os dois viram "Lembrete adiado" na Hoje; voltam a tocar como alarme no horário novo |
| K8 | Com um **vídeo tocando** (YouTube ou galeria) no horário do Teste E | O vídeo **pausa** (ou abaixa), o alarme é ouvido e aparece um aviso no topo, sem fechar o vídeo; tocar no aviso abre a tela de alarme |
| K9 | Com o celular em uso (desbloqueado, em outro app) | Aviso no topo; tocar abre a tela de alarme; responder lá para o som |
| K10 | Com um horário futuro marcado: trocar Ajustes para **Como notificação** e olhar a barra de status | O ícone de despertador **some** (RV-048-03); voltar para **Como alarme** e ele **volta** |
| K11 | Girar a tela de alarme com fonte 2,0× | Nomes quebram em linhas, botões visíveis; em paisagem a lista rola |

## E. Cadastro por voz (M3, RF12–RF19)

Frases e variações em `ROTEIRO_TESTE_VOZ.md`. A voz junta trechos: depois de falar, pare **1,5 s** e ela entrega;
pausas curtas no meio da frase não cortam. Pausa para entregar: **2 s** (era 1,5 s) [novo].

| # | Faça / diga | Deve acontecer |
|---|---|---|
| E0 | Usar voz e **não tocar em nada** [novo] | A tela mostra e **fala** o exemplo "losartana, 50 miligramas…", com o assistente ligado ou não. Abaixo do microfone, o botão **"Silenciar a voz"** com o alto-falante **piscando** enquanto fala, parado no fim. O microfone só abre no toque |
| E0b | Na voz, tocar em **"Silenciar a voz"** [novo] | A fala para na hora, o microfone **não** abre sozinho, o botão vira "Ouvir as perguntas em voz alta" (ícone cortado, parado) e as próximas perguntas não são lidas. Tocar de novo lê a pergunta da tela e volta a ler as seguintes. O botão fica abaixo do microfone em todas as telas da voz; vale só para este cadastro (DEC-050) |
| E1b | Responder com pausas curtas para pensar, sem tocar no microfone | A fala continua sendo juntada (até 2 s de pausa); tocar no microfone entrega na hora. Pausas mais longas podem cortar: limite do reconhecedor do Android 12 |
| E1 | Usar voz → microfone → "Losartana 50 miligramas, um comprimido, todos os dias às 8 da manhã, uso contínuo" | Primeiro **Confira os remédios** com "Losartana"; "pronto" ou **Continuar** → direto para "Confira antes de salvar" |
| E2 | Falar em pedaços, com pausa curta: "Losartana… (pausa) 50 miligramas… (pausa) às 8" [novo] | Um só cadastro com tudo; não corta no primeiro pedaço |
| E3 | "Tomo losartana, metformina e sinvastatina"; depois "**Tomou** losartana" e "Eu **tomei** losartana" [novo] | **Confira os remédios** com os três nomes; com o assistente ligado, falado: "Entendi 3 remédios: Losartana, Metformina e Sinvastatina. Se estiver certo, toque em Continuar." — **sem** abrir o microfone sozinho [novo]; "tomou"/"tomei" não entram no nome |
| E4 | Na conferência: "trocar metformina por atenolol"; "não é sinvastatina, é simvastatina"; "remover atenolol" [novo] | Cada comando muda a lista na hora; nomes parecidos casam |
| E5 | Na conferência: tocar no nome → dizer ou digitar "Rivaroxabana, Metoprolol" [novo] | Vira dois remédios separados |
| E6 | Um nome de duas palavras sem "de" (ex.: "Losartana Metformina") [novo] | Aparece "São 2 remédios? Separar"; Separar divide |
| E7 | Na conferência: X num nome; "Prefiro digitar" com um remédio e com vários [novo] | X pede confirmação e tira; Prefiro digitar com um → formulário; com vários → revisão |
| E8 | "pronto" → responder **pelos botões** (dose, dias, horários, duração) | Uma pergunta por vez (falada em voz alta só com o assistente de voz ligado, com "Anotei …" antes da seguinte); revisão completa no fim |
| E9 | Pergunta dos horários → **Escolher no relógio** → 07:30 → Adicionar outro → Confirmar | "07:30 e 19:30" |
| E10 | "Metformina três vezes ao dia" → primeiro horário no relógio 07:30 | "Horários do dia: 07:30, 15:30 e 23:30"; tocar em 23:30 e escolher 22:00 troca só esse |
| E11 | Pergunta dos dias → **Escolher os dias** → Seg e Sex | "segunda e sexta" |
| E12 | Pergunta da dose → **Escolher a quantidade** → ml → + | "1,5 ml" |
| E13 | Durante uma pergunta: "Algo errado? Corrigir por voz" → "na verdade às 10 da noite" | "Confira a correção" com 22:00; Manter correção aplica, Desfazer desfaz |
| E14 | Na revisão, **Corrigir** num medicamento | Formulário preenchido; "Concluir correção" volta à revisão |
| E15 | Dois remédios de uma vez até salvar | **Confirmar e salvar todos** → "2 medicamentos adicionados" |
| E16 | Dizer pela voz um nome já cadastrado (ex.: "Losartana") e "pronto" na conferência dos nomes [novo] | Aviso "Losartana já está cadastrado" **já na conferência**: Voltar e conferir (fica na lista) / Continuar mesmo assim (segue; ao salvar não pergunta de novo) |
| E17 | "oi, tudo beleza, obrigado" | "Não entendi. Tente de novo." |
| E18 | Não falar nada depois do microfone abrir | "Não ouvi nada. Toque no microfone e fale de novo."; não reabre sozinho |
| E19 | Responder e **não tocar no microfone** | Com a leitura ligada, o microfone abre sozinho ao terminar de ler a pergunta ("Ouvindo…"). Com ela desligada, **não** abre: leia e toque no microfone [novo] |
| E20 | Durante a escuta, tocar num botão de resposta | A escuta para e a resposta tocada vale |
| E21 | X no topo ou Voltar durante a fala da pergunta e durante a escuta [novo] | Fala e escuta param; nada do que estava sendo dito entra no cadastro; com algo entendido, "Descartar cadastro?" |
| E22 | Ajustes → desligar "Na voz, ouvir sozinho depois da pergunta" | Mesmo com a leitura ligada, o microfone só abre com toque (religue depois) |
| E23 | Mídia em 2 ao entrar na voz | "O volume está baixo…" no meio da tela, some em ~4 s |
| E24 | Modo avião e cadastrar por voz | Funciona se o reconhecedor do Google tiver o pacote pt-BR offline; senão, mensagem de erro e opção de digitar (anote qual) |

## F. Cadastro pela câmera (M4, RF20–RF23)

| # | Faça | Deve acontecer |
|---|---|---|
| F0 | Adicionar medicamento [novo] | "Usar câmera" com a etiqueta **Experimental**; no tutorial e em "Confira o que eu li", aviso de que a leitura pode errar |
| F1 | Usar câmera → permitir → caixa de remédio → **Tirar foto** | "Lendo a embalagem…" → "Confira o que eu li" com nome e concentração editáveis; **Cancelar** no topo [novo] |
| F2 | Conferir | **Nunca** preenche dose, dias, horários ou duração |
| F3 | **Continuar por voz** | A voz começa pela pergunta da dose |
| F4 | Repetir e **Continuar digitando** | Formulário com nome e concentração |
| F5 | Foto de uma parede | "Não consegui ler a embalagem." com Tentar de novo e Digitar |
| F6 | Anotar quantas embalagens reais leram certo nome e concentração (meta: 20 ensaios) | Registro para a monografia, não aprovado/reprovado aqui |

## G. Dose — detalhado em `ROTEIRO_TESTE_DOSE_GRADES.md`

| # | Faça | Deve acontecer |
|---|---|---|
| G1 | Comprimido → − | "meio"; − apagado; revisão "meio comprimido" |
| G2 | ml → + várias vezes | 1, 1,5, 2… |
| G3 | Comprimido 2 → Cápsula; Comprimido meio → Cápsula | 2 continua; meio fica vazio |
| G4 | Outro → "1 colher de sopa" | Revisão "1 colher de sopa" |
| G5 | Gota → digitar 120 → + | Continua 120, + apagado; − vai a 119 |
| G6 | ml → 0,25 → + e − | 0,75 e de volta 0,25 |

## H. Acessibilidade e legibilidade (RNF01–RNF04)

| # | Faça | Deve acontecer |
|---|---|---|
| H1 | Fonte 2,0×: Hoje, lista, formulário, diálogo **Digitar o horário**, voz, Confira os remédios, revisão, tutorial, tela de alarme | Nada cortado; "Minuto" inteiro; rola quando precisa |
| H2 | Fonte 1,0× nas mesmas telas | Tudo inteiro |
| H3 | TalkBack: barra inferior, caixas de dias, menu de unidade, − e +, campos Hora/Minuto | Lê nomes, "marcado/não marcado", "Diminuir"/"Aumentar" e valores; anote o que soar confuso |
| H4 | TalkBack no tutorial e na tela de alarme | Lê só o balão do tutorial; na tela de alarme lê nomes e botões |
| H5 | TalkBack na voz com mídia baixa | Lê o aviso de volume |
| H6 | Sob luz forte | Campos, títulos e botões distintos e legíveis |

## I. Robustez

| # | Faça | Deve acontecer |
|---|---|---|
| I1 | Modo avião: cadastro digitando e lembretes | Funcionam (o app não usa rede) |
| I2 | Girar a tela ou trocar de app no meio do formulário | O que foi preenchido continua |
| I3 | Negar microfone ou câmera | Mensagem "Sem acesso ao microfone/à câmera…" com Digitar e Voltar; o app não fecha |
| I4 | Atualizar o app por cima (próxima versão de teste) [novo] | Medicamentos e ajustes continuam (DEC-042) |

## M. Limpeza

Excluir todos os "Teste …" e os cadastros da voz e da câmera: Detalhes → **Excluir medicamento** → Excluir →
"Medicamento excluído". Conferir que Losartana (08:00/20:00) e Rivaroxabana (08:00) continuam. Voltar fonte, volume,
TalkBack, rotação e o som do lembrete ao que você usa.

## Fora desta bateria

Precisão clínica (o app não promete), reconhecimento offline universal, alarme com o celular desligado (não é
possível), Android 14+ (o A31 é Android 12: a permissão de tela cheia não aparece), avaliação com participantes (pelo
protocolo do TCC). A verificação técnica da voz (24 ensaios) e da câmera (20 ensaios) e o MET-3 têm roteiros próprios.
