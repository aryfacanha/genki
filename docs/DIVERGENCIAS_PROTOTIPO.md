# Divergências protótipo × ESPEC (dados e regras)

Levantado em 26/10/2026 por Claude Code (Opus 5.5). **Só levantamento; nada implementado.** Categoria: sugestão pendente.
Comparação: genki-prototype `295212e` (`src/data.ts`, `medicationValidation.tsx`, `reminders.ts`,
`CadastrarMedicamento.tsx`, `EditarMedicamento.tsx`, `AjustesScreen.tsx`; para a exclusão e os lembretes,
também `App.tsx` e `GenkiHome.tsx`) × `docs/ESPEC_ARQUITETURA.md` §3–§7.
Pela DEC-022, enquanto o autor não decidir cada item, **vale a ESPEC** para dados e regras; as telas seguem o protótipo.

**Decididos:** #2, #7 e #12 em 26/10 (DEC-027): erro para horário repetido, rótulos do protótipo, exclusão só com confirmação.

`CadastrarMedicamento.tsx` só tem a escolha Voz (destacada) / Digitar / Câmera, igual à ESPEC §5: nada a registrar.
`SIM_NOW` (relógio fixo da demonstração) não entra: o app usa o relógio e o fuso do aparelho.

| # | Campo / regra | Protótipo | ESPEC | Impacto M1/M2 |
| --- | --- | --- | --- | --- |
| 1 | Dias | `daysMode` "all" \| "specific" + `selectedDays` (Dom…Sáb, domingo primeiro). "Todos os dias" é diferente de marcar os 7 dias | `daysMask: Int` (bit = `DayOfWeek.value - 1`; 127 = todos) | M1: "Todos os dias" grava 127; marcar os 7 também grava 127 e reabre como "Todos os dias" na edição (perde a distinção, sem efeito nos lembretes). Exibir domingo primeiro como no protótipo |
| 2 | Horários repetidos | Erro "Este horário já foi adicionado." | Normalizador ordena e remove repetidos sem avisar; índice único (`medicationId`, `minuteOfDay`) | M1: escolher entre a ESPEC (sem aviso) ou um `Problem` novo (`DUPLICATE_TIME`). A ESPEC só prevê `MISSING` e `END_BEFORE_START` |
| 3 | Horário em branco | Campo novo começa vazio; salvar com campo vazio → "Preencha todos os horários antes de continuar." | `times: List<LocalTime>` não tem horário vazio | M1: o campo vazio é estado da tela; o formulário precisa bloquear ou descartar antes de montar o rascunho |
| 4 | Ordem dos horários | Mantém a ordem digitada | Ordenados | M1: Detalhes e Editar mostram em ordem crescente (melhora; sem decisão necessária) |
| 5 | Data de início | Só existe em "Tem data para terminar"; contínuo e "Ainda não sei" gravam sem data. Na edição, se não houver, sugere hoje | `startDate` obrigatório para todos (padrão: hoje) | M1: gravar hoje sem mostrar o campo fora de TEMPORARY. Ao trocar para "Tem data para terminar" na edição, o início vem com a data do cadastro, não hoje. M2: `OccurrenceCalculator` usa `startDate` em todos (sem efeito prático para quem cadastra hoje) |
| 6 | Tratamento ausente | `draftToMedication` usa `treatment ?? "Ainda não sei"` (padrão silencioso; a validação normalmente impede chegar lá) | Sem padrão silencioso; ausente → `MISSING` | M1: não portar o padrão; o `DraftValidator` bloqueia |
| 7 | Rótulos do tratamento | "Uso contínuo (sem data para parar)" ("Uso contínuo" na revisão da voz), "Tem data para terminar", "Ainda não sei" + ajuda "Os lembretes continuam até você alterar ou desativar este medicamento." | DEC-009: "Ainda não sei" aparece como "Duração a definir" | M1: só texto (dados iguais). Autor decide se o rótulo "Duração a definir" da DEC-009 continua ou se adota o do protótipo |
| 8 | "Por N dias" | Formulário e edição só têm datas de início e término | `durationDays` → `endDate = início + N - 1` (DEC-003) | M1: nenhum (formulário usa datas). M3: só a voz usa. Voz do protótipo não conferida para este item |
| 9 | Mensagens de erro | Uma frase por campo ("Informe o nome do medicamento.", "Informe quanto você toma por vez.", "Selecione pelo menos um dia.", "Adicione ao menos um horário.", "Selecione a duração do tratamento.", "Informe a data de término.", "A data de término não pode ser anterior à data de início.") com foco no 1º campo com erro | Só `Field` + `Problem`; texto não definido | M1: lacuna, não conflito; usar as frases do protótipo na tela |
| 10 | Validação parcial na correção por toque da voz | `validateMedication(…, unresolved)` aceita campos ainda não informados e pergunta depois | Voz usa os `MISSING` para perguntar; só `Valid` chega ao repositório | M3: equivalente se a correção trabalhar só com o rascunho; nenhum impacto no M1/M2 |
| 11 | Salvar edição sem mudanças / sair com mudanças | Sem mudança: só volta. Com mudança: "Descartar alterações?" | "Edição salva direto (valida, sem tela de revisão)" | M1: só tela; não muda dados |
| 12 | Excluir | Diálogo de confirmação + "Desfazer" no aviso, que restaura posição e respostas de lembrete (P4f, `App.tsx`) | "Excluir com confirmação"; `delete(id)` com `CASCADE` nos horários; nada sobre desfazer | M1: "Desfazer" exige reinserir (id novo) ou adiar a exclusão real. Recomendação: só a confirmação (ESPEC) e registrar a diferença |
| 13 | Resposta ao lembrete na Home | "Entendi" → "Visto às HH:MM" na lista Hoje (com "Desfazer"); "Adiado para HH:MM"; "Sem resposta. Vou avisar de novo às HH:MM." — estado por ocorrência guardado no `App.tsx` | ACK não registra nada (DEC-004); sem tabela de alertas, estado só nos extras do Intent (S2) | **M2: maior divergência.** A Hoje do app não sabe se um horário foi "visto" ou adiado. Opções: (a) ESPEC: Hoje só lista horários do dia; (b) estado do dia em memória (perde ao fechar o app). Decisão do autor |
| 14 | Adiar | "Lembrar novamente" abre escolha 10 / 20 / 30 min a cada vez (padrão = Ajustes) | S2: botão único "Adiar N min", N = intervalo dos Ajustes | M2: a notificação teria 3 ações ou uma tela própria. Recomendação: ESPEC; definir o rótulo ("Adiar 20 min" × "Lembrar novamente em 20 min") |
| 15 | Repetição sem resposta | Simula só a 1ª repetição; não define limite | Até 3 repetições (DEC-012, RF24) | M2: nenhum nos dados; o texto de Ajustes não deve sugerir uma única repetição |
| 16 | Ajustes | Repetição liga/desliga; intervalo 10 / 20 / 30 só aparece com a repetição ligada, embora também seja o padrão do adiar; seção "Sobre"; nada sobre permissões | `repeatEnabled` e `intervalMinutes` em SharedPreferences; status das permissões e botão para alarme exato (Android 14+) | M2: seção de permissões sem referência visual no protótipo. Com a repetição desligada, o adiar usaria um intervalo escondido: mostrar o seletor sempre ou aceitar |
| 17 | Ordem na lista Hoje | Horário, depois nome | Não definida | M2: lacuna; adotar a do protótipo |
