# Pré-revisão heurística geral — Genki 1.0.16 no emulador

30/10/2026, Claude Code (Opus 5.5), a pedido do autor ("revisão heurística total do aplicativo no estado atual").
Foco pedido pelo autor (30/10): **não** alarmes, notificações e prazos (requisitos já validados), e sim **requisitos
ocultos, transparência, legibilidade, clareza e capacidade** (muitos itens, nomes longos, vários horários).

**Categoria:** pré-revisão assistida por IA, **não é o MET-3** (que o autor faz, nos 4 fluxos) nem teste com usuários.
Bases: 10 heurísticas de Nielsen (H1–H10) e WCAG 2.2 AA. Severidades na escala do MET-3 (alta, média, baixa). As
correções são **sugestões pendentes**: nada foi alterado no app por causa desta revisão. Revisão anterior:
`PRE_REVISAO_HEURISTICA_A31.md` (29/10, 1.0.9).

## Ambiente e cobertura

- Emulador Pixel 6, API 35, 1080 × 2400, 420 dpi; build debug do código da 1.0.16 (alto-falante abaixo do microfone,
  leitura ligada ao abrir a voz). O A31 não foi usado.
- **Capacidade:** 13 medicamentos fictícios inseridos direto no banco do emulador, só para medir as telas (nomes de até
  66 caracteres, concentrações compostas, até 6 horários, dias específicos, tratamentos com fim e "Ainda não sei"),
  além dos 2 que já existiam: 15 no total, 33 horários na Hoje. Um 16º foi cadastrado pelo formulário com nome de
  180 caracteres e quantidade 999, e excluído pela interface.
- **Voz:** fala simulada temporária em `SpeechInput.start()` (lida de um arquivo); retirada depois, hash original
  `cf5d58d6…` conferido.
- **Fonte:** 1,0× e 2,0× (voz, escolha do cadastro); de volta a 1,0×.
- **Primeira abertura:** simulada apagando banco e preferências (introdução, oferta do tutorial, telas vazias).
- **Restauração:** banco e preferências idênticos à cópia (`build/heur1015/`, MD5 conferido), câmera e microfone
  revogados, só o alarme das 23:05 da Losartana agendado.
- Telas: Hoje, Medicamentos, Detalhes, escolha do cadastro, formulário (3 etapas, relógio, revisão, exclusão), voz
  (início, conferência dos nomes, aviso de nome repetido, perguntas, revisão, descarte), câmera (falha de leitura),
  Ajustes, tutorial (7 passos), introdução (2 páginas), telas vazias.
- Não cobertos: TalkBack real, A31, fala real, leitura de embalagem real.

## O que atende (sem problema observado)

- **Nomes longos (66 caracteres)** quebram em linhas sem cortar na Hoje, em Medicamentos, nos Detalhes e no diálogo
  de exclusão.
- **Transparência na introdução:** "Ele não registra se você tomou e não substitui a orientação do médico ou do
  farmacêutico. Tudo fica só neste celular."
- **"Ainda não sei"** é explicado na hora ("Os lembretes continuam até você alterar ou desativar este medicamento.")
  e repetido na revisão e nos Detalhes. Nos Detalhes, o tratamento com fim mostra o período ("De 30/10/2026 até
  05/11/2026").
- **Telas vazias** dizem o que fazer ("Cadastre um medicamento e seus horários…").
- **Nome repetido:** aviso claro com o porquê ("Dois cadastros iguais geram lembretes repetidos…") e saída
  ("adicionar o horário no cadastro que já existe").
- **Voz:** "Você disse" nas perguntas; "Entendi até agora" acumula o que foi entendido; "São 3 remédios? Separar"
  recupera nomes juntados; o alto-falante pisca durante a fala e fica parado quando calado.
- **Câmera:** etiqueta "Experimental" na escolha; a falha de leitura diz o que tentar ("mais luz, com o nome do
  medicamento de frente").
- **Exclusão:** confirma com o nome e explica que os lembretes serão cancelados.

## Situação dos achados de 29/10

Corrigidos na 1.0.10: 1, 2, 3 e 4. **Continuam:** 5 (a conferência mostra só os nomes), 6 (sem "Você disse" na
conferência dos nomes), 11 (sem "Voltar" no balão do tutorial), 12 (cartão do horário sem sinal de que é tocável),
18 (sem versão em Sobre) e 20 ("•" e "·"). Os demais não foram conferidos de novo.

## Achados novos

Formato do MET-3: fluxo | tela | problema | heurística | severidade | correção (sugestão). Evidência: **O** observado
no emulador; **C** inferido do código.

| # | Fluxo | Tela | Problema | Heurística | Severidade | Correção sugerida | Ev. |
| --- | --- | --- | --- | --- | --- | --- | --- |
| N1 | Capacidade | Hoje | A lista começa sempre em 00:00. Com 15 remédios, às 18:55 havia **19 linhas "Passou"** antes de "Agora", e os próximos horários só apareciam depois de 3 rolagens. O público-alvo tem justamente muitos remédios | H1, H8 | **alta** | Abrir a lista já rolada em "Agora", ou recolher os que passaram ("19 horários já passaram · Mostrar") | O |
| N2 | Capacidade, voz | Voz, fonte 2,0× | O painel fixo de baixo (microfone, instrução, "Silenciar a voz", "Prefiro digitar" e, na conferência, "Continuar") ocupa cerca de **metade da tela**. Na conferência cabem 2 nomes. Nas perguntas, com 3 remédios, a **pergunta atual ficou cortada** e as opções fora da tela, com o microfone já aberto. Na abertura, "Silenciar a voz" fica abaixo da dobra. O botão novo da 1.0.15 aumentou o painel em cerca de 70 dp | H1, H8; WCAG 1.4.4/1.4.10 | **alta** | Em fonte grande, juntar alto-falante e "Prefiro digitar" numa linha (ou só ícones com rótulo de acessibilidade); rolar até a pergunta atual; "Entendi até agora" com uma linha por remédio, expansível | O |
| N3 | Capacidade, voz | Perguntas, fonte 1,0× | "Entendi até agora" mostra cada remédio por inteiro acima da pergunta. Com 2 remédios, as opções de horário já ficam parcialmente sob o painel; com mais, a pergunta desce para fora da tela | H1, H8 | média | Mesma do N2 | O |
| N4 | Transparência | Medicamentos, Detalhes | Depois da data de término, o remédio continua como "Tem data para terminar" e os Detalhes dizem **"Lembretes ativos"**, mas nenhum lembrete toca mais (`OccurrenceCalculator` filtra pela data) | H1 (estado falso) | média | "Tratamento terminou em 05/11" e, nos Detalhes, "Sem lembretes: o tratamento terminou" | C |
| N5 | Transparência | Hoje | Remédios com lembretes desligados somem da Hoje sem aviso. Se todos estiverem desligados, a Hoje diz "Você ainda não possui lembretes. Cadastre um medicamento…", mesmo com remédios cadastrados | H1, H2 | média | Linha "2 remédios com lembretes desligados" com atalho para Medicamentos; outro texto para o vazio quando há remédios | O, C |
| N6 | Transparência, voz | Perguntas | Partes da fala são **descartadas sem aviso**: "de manhã" (Losartana), "850" sem unidade (Metformina) e "depois do almoço e do jantar" não aparecem em "Entendi até agora" e não mudam as perguntas. O "Você disse" mostra a frase, mas não o que ficou de fora | H1, H9 | média | Dizer o que não foi usado ("Não usei: 'de manhã'") ou perguntar com a pista ("De manhã: a que horas?") | O |
| N7 | Prevenção de erro, voz | Conferência dos nomes | Uma lista falada sem "e" entre todos os nomes vira **um nome só** ("Metformina sinvastatina omeprazol"). "São 3 remédios? Separar" aparece, mas "Continuar" segue com o nome juntado sem perguntar | H5 | média | Ao continuar com um nome de 3 ou mais palavras não separado, perguntar "É um remédio só?" | O |
| N8 | Prevenção de erro | Formulário, quantidade | **"999 comprimidos"** foi aceito e salvo sem aviso (digitar 10 em vez de 1 passa igual) | H5 | média | Confirmar quantidades fora do comum ("999 comprimidos por vez. Está certo?"), sem bloquear e sem prometer precisão clínica | O |
| N9 | Requisito oculto | Formulário, etapa 3 | Duração só por data de término. A forma da receita ("por 7 dias") existe na voz, mas no formulário a pessoa precisa calcular a data | H2 | média | Opção "Por quantos dias", que calcula a data (o `DraftValidator` já aceita `durationDays`) | O, C |
| N10 | Transparência, privacidade | Introdução, Sobre | "Tudo fica só neste celular" aparece só na introdução e não pode ser revisto. O app não diz que a voz passa pelo reconhecedor do sistema (Google), que pode processar o áudio fora do aparelho quando o reconhecimento no aparelho não está disponível, nem que **não há cópia de segurança** (trocar de celular ou desinstalar apaga tudo) | H10 | média | Seção "Privacidade e dados" em Sobre: onde ficam os dados, a voz pelo reconhecedor do sistema, sem cópia de segurança | O, C |
| N11 | Capacidade | Medicamentos | Ordem de cadastro (`ORDER BY id`), sem ordem alfabética nem busca. Com 15 remédios, achar um exige ler a lista | H6, H7 | média | Ordem alfabética | O, C |
| N12 | Clareza | Ajustes, introdução | Desde 30/10 a voz lê as perguntas sempre, e "Ativar assistente de voz" passou a ler **só o tutorial**. O nome promete mais do que faz | H2, H4 | baixa | "Ler o tutorial em voz alta" | O |
| N13 | Capacidade | Formulário, nome | Sem limite de tamanho: 180 caracteres aceitos. O campo tem uma linha (vê-se só o fim do texto) e, no "Próximo lembrete", o nome ocupou 7 linhas | H5, H8 | baixa | Limite (por exemplo, 60) ou campo de várias linhas | O |
| N14 | Prevenção de erro | Formulário, horários | "Adicionar horário" sempre começa em 08:00. O segundo horário já abre com o erro "Este horário já foi adicionado" e OK desativado | H5, H9 | baixa | Começar no próximo horário livre (por exemplo, 12 horas depois do anterior) | O |
| N15 | Clareza | Medicamentos | "Tem data para terminar" sem a data; a data só aparece nos Detalhes | H6 | baixa | "Até 05/11" na lista | O |
| N16 | Prevenção de erro, voz | Revisão da voz | "Confirmar e salvar todos" fica à vista sem que a pessoa tenha rolado pelos outros cartões, e nada diz quantos remédios há | H5 | baixa | "Confira os 2 remédios" no topo | O |
| N17 | Consistência | Hoje | As linhas da lista abrem os Detalhes; o cartão "Próximo lembrete" não | H4 | baixa | Tocar no remédio do cartão abre os Detalhes | O |
| N18 | Consistência | Vários | Dias em minúsculas ("domingo", "segunda e sexta") ao lado de "Todos os dias"; "ml" no menu de unidade e "mL" nas concentrações | H4 | baixa | Maiúscula inicial; "mL" | O |
| N19 | Transparência | Revisão, Detalhes | Sem concentração, a linha some, em vez de dizer que não foi informada | H1 | baixa | "Concentração: não informada" | O |

## Prioridade sugerida antes do congelamento (02/11)

1. **N2/N3** (voz em fonte grande): a pergunta e as opções somem da tela, e o botão novo piorou o espaço.
2. **N1** (Hoje aberta em "Agora"): efeito grande com muitos remédios, mudança pequena.
3. **N4/N5** (estados falsos ou ocultos de lembrete).
4. **N6/N7** (o que a voz ignorou ou juntou).
5. **N8** (quantidade fora do comum).

Os demais podem ficar como limitações ou trabalho futuro; o N10 pode ir só para a monografia, se o autor preferir não
mexer no app.
