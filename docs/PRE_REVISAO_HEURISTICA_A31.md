# Pré-revisão heurística de ponta a ponta — Genki 1.0.9 no A31

29/10/2026, Claude Code (Opus 5.5), a pedido do autor. Roteiro: `docs/ROTEIRO_REVISAO_PONTA_A_PONTA.md`.

**Categoria:** pré-revisão assistida por IA, **não é o MET-3** (que o autor faz, nos 4 fluxos, com a lista de Galavi et
al., 2024, ainda não conferida) nem teste com usuários. Bases: 10 heurísticas de Nielsen (H1–H10), WCAG 2.2 AA e
diretrizes de acessibilidade do Android/Material. Severidades na escala do MET-3. Correções são **sugestões
pendentes**: nada foi alterado no app.

## Ambiente e cobertura

- Samsung A31, Android 12, 1080 × 2400, 420 dpi, fonte do usuário 1,3× (e 2,0× por alguns minutos, restaurada),
  Genki 1.0.9 (release, `main`/`6c04eb6`), operado só pelo ADB. Medicamento fictício "Teste Heuristica" criado e
  excluído no fim; os medicamentos do usuário não foram alterados.
- Emulador Pixel 6, API 35, debug do mesmo código, só para a voz, com fala simulada temporária em `SpeechInput.start()`
  (lida de um arquivo; removida depois, hash original `77717ab1…` conferido, `git diff -- app` vazio) e banco
  restaurado byte a byte.
- Telas auditadas (captura + árvore de acessibilidade): Hoje, escolha do cadastro, formulário (3 etapas, relógio,
  menu de unidade), revisão, Medicamentos, Detalhes, edição, Ajustes, tutorial (7 passos), voz (início, conferência
  dos nomes, revisão, correção, "Não entendi"), notificação do lembrete (só a do Genki), Hoje com lembrete adiado,
  e fonte 2,0× em Hoje, Medicamentos, escolha, formulário, diálogo de descarte e Ajustes.
- Não cobertos: câmera, TalkBack real, som audível, Não perturbe, reinício do aparelho, primeira abertura no A31
  (exigiria apagar os dados; conferida pelo Codex no emulador).

## O que atende (sem problema observado)

- **Alvos de toque e rótulos:** a auditoria não achou clicável sem rótulo em nenhuma tela, nem alvo abaixo de 48 dp
  fora do relógio do sistema (ver achado 3) e dos rodapés (achado 7, só na árvore).
- **Escala de texto (WCAG 1.4.4):** em 2,0×, Hoje, Medicamentos, escolha, formulário, descarte e Ajustes não cortam
  texto; o conteúdo rola.
- **Erros (WCAG 3.3.1):** "Continuar" vazio mostra o erro junto do campo ("Informe o nome do medicamento.",
  "Informe quanto você toma por vez.").
- **Estado e progresso (H1):** "Etapa N de 3", título de cada etapa, revisão completa antes de salvar com "Alterar"
  por seção, aviso "Medicamento adicionado" e volta à Hoje com o novo horário em destaque.
- **Controle e liberdade (H3):** "Cancelar" em todas as telas de cadastro, com "Descartar cadastro?" quando há dados;
  voltar entre etapas; "Prefiro digitar" na voz.
- **Regras do produto visíveis:** "Concentração (opcional)" separada de "Quanto você toma por vez"; nada de "Tomado";
  "Excluir medicamento" em vermelho e separado.
- **Lembrete:** a notificação chegou no minuto marcado com o app em segundo plano (16:10 e, depois da edição, 16:40),
  prioridade alta, categoria lembrete, conteúdo oculto na tela bloqueada, título "Lembrete das HH:MM", texto com nome,
  concentração e dose, botões **Entendi** e **Adiar N min** com o intervalo dos Ajustes. "Adiar" na notificação
  tirou a notificação e mostrou na Hoje "Lembrete adiado · Novo alerta às … · Horário original: …". Editar o horário
  reagendou o lembrete.
- **Voz (emulador):** fala completa com dois remédios → conferência dos nomes → perguntas só do que faltava (uma por
  vez, falada, com escuta automática) → revisão; correção por voz do horário com "Manter correção/Desfazer"; fala sem
  sentido → "Não entendi".
- **Ajustes:** cada opção com explicação em linguagem simples ("Vale para a repetição e para o botão Adiar").

## Achados

Formato do MET-3: fluxo | tela | problema | heurística | severidade | correção (sugestão). Evidência: **O** observado
no aparelho/emulador; **C** inferido do código; **I** inconclusivo (precisa de TalkBack ou de pessoa).

| # | Fluxo | Tela | Problema | Heurística | Severidade | Correção sugerida | Ev. |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | F3 Lembrete | Hoje, cartão adiado | Depois de uma parada forçada do app, o cartão continua prometendo "Novo alerta às 16:30", mas o alarme não existe: nada tocou e às 16:31 o cartão seguia igual. A parada foi provocada nesta revisão (`am force-stop` para trocar a fonte), mas equivale a "Forçar parada" pelo usuário ou por otimizador de bateria. O reinício já é tratado (cartões somem, DEC-029); a abertura do app reagenda só os horários normais | H1 (estado falso) | **alta** | Ao abrir o app, reagendar adiamentos e repetições pendentes ainda no futuro; se o horário já passou, avisar na hora ou trocar o cartão por "Lembrete pendente" sem horário prometido | O, C |
| 2 | F1/F2 Cadastro | Salvar | Remédio com o mesmo nome de um já cadastrado é salvo sem aviso (no emulador, segunda "Losartana 50 mg", em outro horário). Lembretes duplicados podem levar a tomar duas vezes. Limitação já registrada na ESPEC (duplicidade "sem tratamento") | H5 (prevenção de erro) | **alta** (decisão do autor manter) | Na revisão, aviso "Você já tem Losartana cadastrada. Cadastrar mesmo assim?" | O |
| 3 | F1/F4 Horário | Relógio | No toque, o minuto arredonda para múltiplos de 5 (mirados 9, 37 e 38 → 10, 35 e 40); minuto exato só arrastando o ponteiro, e não há como digitar o horário | H7 (flexibilidade); **WCAG 2.5.7** (arrastar exige alternativa de toque simples) | média | Opção "Digitar horário" (entrada de teclado do `TimePicker`/`TimeInput` do Material 3) ou − / + de minuto | O |
| 4 | F1/F4 Horário | Relógio | Dois anéis (0–11 fora, 12–23 dentro) com números pequenos; troca automática hora → minuto: um toque no minuto caiu no anel das horas e virou 07:10. Quatro posições (0, 3, 6, 9 h) têm 32 dp numa dimensão | H5; diretriz de 48 dp (WCAG 2.5.8 de 24 px é atendido) | média | Mesma do 3 (entrada digitada como padrão para o público do app) | O |
| 5 | F2 Voz | Conferência dos nomes | Só os nomes aparecem; o que já foi entendido do resto (50 mg, 1 comprimido, 08:00, uso contínuo) não aparece até a revisão. Quem falou tudo de uma vez não sabe se foi anotado | H1 | média | Linha curta sob cada nome ("50 mg · 1 comprimido · 08:00") | O |
| 6 | F2 Voz | Conferência e início | A primeira fala não mostra "Você disse: …" (as perguntas mostram); depois de "Não entendi" na tela inicial, também não | H1, H9 | média | Mostrar "Você disse" sempre que houver fala reconhecida | O, C |
| 7 | F2 Voz | Revisão, "Confira a correção" | O campo alterado (Sinvastatina 21:00 → 22:00) fica fora da tela, no segundo cartão, sem destaque; é preciso rolar e comparar | H1, H6 | média | Rolar até o remédio alterado e destacar o campo, ou resumo no topo ("Mudou: Sinvastatina, horário 22:00") | O |
| 8 | F2 Voz | Início, "Não entendi" | A tela diz só "Não entendi. Tente de novo."; o exemplo de como falar existe só no áudio | H9; WCAG 3.3.3 | baixa | Repetir na tela o exemplo falado | O |
| 9 | F3 Lembrete | Hoje, lista | Com o lembrete adiado, o horário original aparece como "Passou" na lista, enquanto o cartão diz "adiado" | H1, H4 | baixa | "Adiado para 16:30" na linha (divergência #13 do protótipo) | O |
| 10 | Tutorial | Passo 2 com aviso na Hoje | Sem cartão de próximo lembrete, o balão "Aqui aparecem o próximo horário…" fica no centro, sem destaque, e cobre o cartão do aviso | H2 | baixa | Destacar o cartão de aviso (ou adaptar o texto) quando houver | O |
| 11 | Tutorial | Balão | Não há "Voltar" no balão; só o botão Voltar do sistema volta um passo | H3 | baixa | Botão "Voltar" a partir do passo 2 | O |
| 12 | F1 | Etapa 2 | O cartão "Horário 1 · 16:10" é tocável para alterar, mas sem sinal visual (ícone ou seta); só o leitor de tela anuncia "Toque para alterar" | H6 | baixa | Ícone de lápis ou texto "Alterar" no cartão | O |
| 13 | F1/A | Etapas 2 e 3, Ajustes | Escolha única em três estilos (rádio solto, rádio em cartão, cartão com ✓); a etapa 2 vem marcada ("Todos os dias"), a 3 não | H4 | baixa | Um estilo só para escolha única | O |
| 14 | F2 | Revisão da voz | "Corrigir" por remédio e "Remover da lista" (recuado em relação ao texto do cartão) × "Alterar" por seção na revisão manual | H4 | baixa | Alinhar rótulos e recuo | O |
| 15 | F2 | Conferência dos nomes | Exemplo "trocar Simvastatina por Sinvastatina" usa dois nomes quase iguais | H2 | baixa | Exemplo com nomes distintos ("trocar Losartana por Atenolol") | O |
| 16 | F1 | Etapa 3 (1,3×) | "Uso contínuo (sem data para parar)" em duas linhas encosta na borda do cartão; "Continuar para revisão" quebra alinhado à esquerda | H8 | baixa | Mais respiro vertical; texto centralizado no botão | O |
| 17 | A | Barra inferior (2,0×) | "Medicamentos" encolhe para caber e fica menor que "Hoje" e "Ajustes" (solução deliberada para não cortar) | H4 | baixa | Aceitar ou usar o mesmo tamanho reduzido nos três | O |
| 18 | A | Sobre | Sem versão do app; útil para relatar problema no teste informal | H10 | baixa | "Versão 1.0.9" em Sobre | O |
| 19 | Geral | Todas | Só tema claro; com o sistema em escuro/automático, o app segue claro (decisão: como o protótipo) | H7 | baixa | Manter (registrar como limitação) | O, C |
| 20 | Medicamentos | Lista | Separador "•" na lista e "·" no resto do app | H4 | baixa | Um separador só | O |
| 21 | Vários | Rodapés | A árvore de acessibilidade informa a área dos botões de rodapé cortada (Continuar 38 dp, Voltar 36 dp), embora na tela tenham 56/52 dp; pode afetar o contorno de foco do TalkBack | WCAG 4.1.2 | — | Conferir com TalkBack | I |
| 22 | Vários | Escolhas únicas | A árvore não expõe a opção marcada (rádios e cartões com ✓ aparecem "não marcados", inclusive "Todos os dias"); pode ser limite do `uiautomator` com Compose | WCAG 4.1.2 | — | Conferir com TalkBack se anuncia "selecionado" (H3 da bateria) | I |
| 23 | F1 | Salvar | O aviso "Medicamento adicionado" não aparece na árvore capturada logo depois; pode não ser anunciado | WCAG 4.1.3 | — | Conferir com TalkBack | I |

## Lembrete: adiar de ponta a ponta

- **16:10, sem interferência:** notificação no horário; "Adiar 20 min" na notificação → notificação some, cartão
  "Lembrete adiado · Novo alerta às 16:30" na Hoje, alarme do adiamento agendado.
- **16:30:** não tocou, por causa da parada forçada feita nesta revisão às 16:18 (achado 1).
- **16:40, depois de editar o horário (F4):** notificação no horário; "Adiar 10 min" (intervalo trocado só para o teste)
  agendou o alarme para 16:50:31. **A volta do adiamento não foi aguardada** (o autor pediu para encerrar antes); a volta
  do adiado com o app sem parada forçada continua conferida só no emulador pelo Codex.
- **F4, fim:** "Desativar lembretes" → Detalhes "Lembretes: Desativados" e "Ativar lembretes"; "Excluir medicamento" →
  "Excluir Teste Heuristica?" com explicação → "Medicamento excluído"; o alarme do adiamento pendente saiu junto (só
  restaram os alarmes dos medicamentos do usuário).

## Prioridade sugerida antes do congelamento (02/11)

1. Achado 1 (estado falso do adiado): é o único que faz um lembrete prometido não tocar.
2. Achado 2 (duplicidade): decisão do autor, já registrada como limitação; aviso simples na revisão resolve.
3. Achados 3–4 (horário digitado): também resolvem a não conformidade com a WCAG 2.5.7.
4. Achados 5–7 (o que a voz entendeu e o que mudou).

Os demais são de consistência e podem ficar como limitações ou trabalho futuro.

## Restauração

Medicamento de teste excluído pela interface; fonte do A31 de volta a 1,3×; intervalo do adiamento de volta a 20 min;
emulador com banco idêntico ao backup, código original e microfone revogado. Uma notificação de outro app de
lembretes apareceu no A31 durante o teste; a captura foi apagada e o conteúdo não foi registrado.
