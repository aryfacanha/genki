# Teste de usabilidade antes do congelamento — Genki 1.0.22 no A31

Criado em 03/11/2026 por Claude Code (Opus 5.5), a pedido do autor: um passeio completo pelo app **como usuário**,
para decidir o congelamento hoje. Não é caça a defeitos: o objetivo é ver se cada tarefa se completa sem travar,
sem confundir e sem perder dados. Casos detalhados continuam em `ROTEIRO_BATERIA_COMPLETA.md` (consulte só se algo
parecer errado). **Ainda não executado.**

Tempo estimado: 60 a 90 minutos. Use nomes de teste ("Teste A", "Teste B" …) e exclua tudo no fim (passo 10).

## Regra do dia

Corrigir hoje **só o que for crítico** (S1/S2): o app fecha, perde ou grava dado errado, o lembrete não chega, uma
tarefa principal não se completa. Todo o resto (texto, visual, preferência) vai para a lista de limitações da
monografia, sem nova implementação.

Para cada passo, anote: **ok**, **confuso** (o que confundiu) ou **problema** (o que fez, o que apareceu, horário,
print).

## Antes de começar

- A31 com a **1.0.22** (Ajustes do Android → Apps → Genki → versão), fonte na sua escala habitual, mídia em 8 ou mais,
  "Não perturbe" desligado, "Alarmes e lembretes" permitido para o Genki.
- Anote a hora: os lembretes usam horários 3 a 5 minutos à frente (relógio → **Digitar o horário**).

## Tarefas

| # | Tarefa (como usuário) | Deve dar para… | Atenção a |
|---|---|---|---|
| 1 | Abrir o Genki e entender a tela Hoje; Ajustes → **Ver o tutorial** até o fim | Saber o que o app faz e onde adicionar um remédio | Texto claro; tutorial não trava; Voltar funciona |
| 2 | **Cadastrar digitando** "Teste A", 50 mg, meio comprimido, seg e qua, 08:00 e 20:00, uso contínuo | Chegar à revisão e salvar sem ajuda | Botões − e +, diálogo do horário, mensagens de erro ao esquecer algo |
| 3 | **Cadastrar por voz**: "Teste B 20 miligramas, um comprimido, todos os dias às 9 da noite, uso contínuo" | Salvar com no máximo uma correção | Espera de 3 s para encerrar; "Silenciar a voz"; "Prefiro digitar" como saída |
| 4 | **Voz com vários remédios**: "Tomo losartana e metformina", completar pelos botões | Salvar os dois de uma vez | Conferência dos nomes, uma pergunta por vez, revisão final |
| 5 | **Câmera** numa caixa de remédio real, depois Continuar digitando | Ver nome e concentração para conferir; dose, dias e horários nunca preenchidos | Etiqueta Experimental; falha de leitura com saída para digitar |
| 6 | Medicamentos → Teste A → **Editar** a quantidade → Salvar; **Desativar** e **Ativar** lembretes | Mudar sem perder nada | Aviso de descartar alterações; faixa de lembretes |
| 7 | **Lembrete como notificação** (Ajustes → Som do lembrete): "Teste C" daqui a 3–5 min, app fechado | Ver o aviso, **Adiar**, ver voltar, **Entendi** | Cartão Agora/adiado na Hoje; nada de "Tomado" |
| 8 | **Lembrete como alarme** com **"Falar o nome dos remédios no alarme" ligado** (só aparece com Como alarme): "Teste D" e "Teste E" no mesmo horário, celular bloqueado e no silencioso | Tela de alarme acende, som alto, fala "Lembrete das H: 2 remédios."; Entendi de cada um; som para no último | Fala audível; volume; Entendi no meio da fala cala tudo |
| 9 | **Fonte 2,0×** (Android) e passar rápido por Hoje, cadastro digitando, voz, revisão e tela de Ajustes; voltar à fonte normal | Ler tudo, nada cortado, rolar quando precisa | Botões e rótulos inteiros |
| 10 | **Modo avião**: abrir, cadastrar digitando "Teste F" e ver na lista; sair do modo avião | Funcionar sem internet | Voz offline depende do pacote pt-BR do Google (anotar se funcionou) |
| 11 | **Limpeza**: excluir todos os "Teste …" e os cadastros da voz e da câmera; voltar fonte, volume e som do lembrete ao seu uso | Lista só com seus remédios reais | Confirmação antes de excluir (não há Desfazer, DEC-027) |

## Depois do teste

- Nenhum problema crítico: **1.0.22 congelada** (a tag `v1.0.22` já existe; registrar no `ESTADO.md` a data e que é
  a versão da avaliação).
- Problema crítico: correção mínima, versão 1.0.23, build e testes, reinstalar no A31, repetir só a tarefa afetada e
  congelar a 1.0.23.
- Itens "confuso": registrar como limitação ou como achado para a revisão heurística (MET-3), sem implementar.
