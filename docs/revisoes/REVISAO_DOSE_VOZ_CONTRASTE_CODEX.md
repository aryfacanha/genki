# Revisão — dose, relógio da voz, reparos A31 e contraste

28/10/2026 — Codex (GPT-6). Partida: `main`/`4b455e7de693fc4676106d7d027bd1352e78bf25`.
Alvo de código: `be24b9d..61d4c22` (`011d4e8`, `f5188a0`, `ce1aad7`, `61d4c22`), conforme roteiro `4b455e7`.

**Resultado: três S2 e dois S3 confirmados; nenhum S1 observado.** Build e 133 testes existentes passam.
Quatro reprodutores adicionais falham nos casos ausentes da suíte; outro achado foi confirmado na interface.
Sem correção permanente de código/configuração pelo revisor. Aceite no A31 permanece separado.

## Contexto e integridade

Reabertos a proposta oficial no Drive e `Requisitos!A1:F38` da planilha oficial; consultados ESPEC, DEC-036–039,
handoff, roteiro e resultado A31. A implementação permanece no escopo: sem banco/dependência nova ou novo caminho
de gravação, sem mistura de concentração/dose, inferência por OCR, tomada ou mudança de datas.

O `git pull` trouxe `794ac54`/`5bbacac` (documentação da monografia), com conflitos em `ENTREGAS`, `ESTADO` e
`REGISTRO_IA`. Ambos os conjuntos de registros foram preservados, sem marcadores restantes; o índice foi marcado
como resolvido. `docs/00_INDICE.md` veio do merge automático. **Merge preparado, commit de merge pendente**:
o roteiro proíbe commit/push. A revisão acadêmica da monografia não faz parte deste relatório de app.
As alterações locais preexistentes de `.idea/` e `.claude/settings.local.json` foram preservadas.

## Achados

| ID | Gravidade | Arquivo:linha | Problema e reprodução | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-DVC-01 | S2 | `app/src/main/java/br/edu/ifrs/genki/ui/form/DoseUnit.kt:54` | **Aumentar reduz quantidade digitada acima de 100.** Em Gota, digitar 120 e tocar + resulta em 100, pela aplicação de `minOf(MAX, ...)`. Confirmado em teste e no emulador. O campo aceita 120 e o validador não impõe esse teto; o botão pode diminuir uma dose já preenchida. Introduzido em DEC-038. | Garantir que + nunca reduza o valor. Se houver limite, impedir o aumento com estado/feedback coerente, sem substituir silenciosamente a quantidade atual. |
| RV-DVC-02 | S2 | `app/src/main/java/br/edu/ifrs/genki/ui/form/DoseUnit.kt:43` | **O contador descarta casas decimais fora de metades.** Em ml, digitar 0,25 e tocar + (passo 0,5) resulta em 0,5, em vez de 0,75. `amountOf` usa parte inteira e apenas um indicador de metade; 1,25 + também vira 1,5, e não 1,75. O campo aceita esses números sem aviso. Confirmado por reprodutor e interface. Introduzido em DEC-038. | Preservar a precisão da quantidade aceita ao somar/subtrair o passo; alternativamente, validar explicitamente formatos não suportados antes de acionar o contador. Não arredondar silenciosamente. |
| RV-DVC-03 | S2 | `app/src/main/java/br/edu/ifrs/genki/voice/ui/VoiceScreen.kt:586` (interação com `VoiceParser.kt:228–233`) | **Relógio permite confirmar horários que a frequência pendente substitui.** Com `everyHours = 8` (três vezes ao dia), escolher 07:30 e 15:00 produz a frase correta, mas o interpretador retorna 07:30/15:30/23:30: **15:00 desaparece**. Confirmado em teste do caminho `timesAnswer` → `VoiceParser`. O cálculo do parser é anterior; o novo seletor reutilizado na pergunta do primeiro horário expõe a confirmação de várias escolhas sem esclarecer esse descarte. | Na pergunta do primeiro horário, permitir escolher apenas um e mostrar a programação gerada. Manter o seletor múltiplo para a pergunta comum de horários; não confirmar escolhas que serão descartadas. |
| RV-DVC-04 | S3 | `app/src/main/java/br/edu/ifrs/genki/ui/form/DoseUnit.kt:33` | **Dose antiga/em voz `1/2 comprimido` não incrementa no primeiro toque.** `parse` reconhece a unidade, mas `value("1/2")` devolve null. Assim, + gera `meio`, preservando 0,5 em vez de avançar para 1. Confirmado em teste. Formato já suportado antes; regressão na nova operação do contador. | Converter também as frações já aceitas, ou normalizá-las para o contador preservando a dose. Cobrir `1/2` e `½`. |
| RV-DVC-05 | S3 | `app/src/main/java/br/edu/ifrs/genki/voice/ui/VoiceScreen.kt:351` | **Seletor de horários continua durante “Diga a correção”.** Na pergunta dos horários, abrir relógio, confirmar 08:00 e tocar “Algo errado? Corrigir por voz”: a pergunta vira correção, mas continuam o horário antigo e “Confirmar horário”. `picking` depende só de draft/campo e o ramo não testa `state.correcting`; ele também oculta a alternativa de digitar a correção. Confirmado no emulador, com a escuta simulada mantida aberta. A31-01 corrigido na passagem entre perguntas, residual nesta transição de modo. | Encerrar/ocultar o seletor ao entrar em correção; considerar o modo de correção no estado e na renderização. Conferir também voltar/Desfazer. |

RV-DVC-03 é a observação pedida pelo roteiro sobre “primeiro horário”: no cenário de três vezes ao dia, não é
apenas espaço extra na tela; um horário explicitamente confirmado muda sem aviso. A reprodução é da lógica com
dados equivalentes aos produzidos pelo seletor, não de uma sessão de fala real no aparelho.

## Verificação executada

- Build inicial: passou após executar o Gradle fora da restrição que impediu seu acesso à distribuição. Testes iniciais reutilizados.
- Reprodutores temporários: quatro testes compilados e executados, **quatro falhas esperadas** que confirmam RV-DVC-01/02/03/04. Houve primeiro uma falha de compilação no próprio teste temporário (faltava `source` no rascunho), corrigida antes da execução; não é falha do app.
- Após remover teste/entrada temporários: `.\gradlew.bat assembleDebug test` **passou, 133 testes executados, zero falhas/erros** nos XMLs finais. Evidência dos reprodutores preservada em `build/review_dvc_probes.xml` e `build/TemporaryDoseVoiceContrastReviewTest.kt`, ignorados pelo Git.
- Emulador Pixel_6_2 / API 35, sempre com `adb -s emulator-5554`. Fontes 1,0×, 1,5× e 2,0×. A31 não operado.
- **Dose:** menu/contador, teclado numérico, passos usuais, meia unidade, digitação e RV-DVC-01/02; T01–T10 (T07 transferido também a partir de Cápsula), incluindo limpeza e preservação ao passar por Outro. Quantidade vazia conferida nas árvores, sem salvar as alterações desses casos. Não se repetiu cada botão em cada fonte.
- **Persistência F01–F04:** criado `RevisaoDVC` exclusivamente no emulador, pelo formulário/revisão e `DraftValidator`; salvo e reaberto com `1 comprimido e meio`, depois 4 comprimidos, meio comprimido e `1 colher de sopa`. Unidades e quantidades/texto reabrem corretamente. Edição não rola automaticamente ao abrir; descarte confirmado. Registro de teste excluído ao terminar, preservando Metformina/Losartana anteriores.
- **Rolagem e títulos:** mensagens de dias e horários simultâneos e de término ausente visíveis em 2,0×; duração ausente também conferida. Menu e contador legíveis em 2,0×; título Novo medicamento em duas linhas sem quebrar palavra; Quantidade cabe. Detalhes/edição/revisão conferidos em 1,0×. Não foi auditoria de todos os títulos longos em todas as fontes.
- **Voz:** entrada inicial simulada, botões de dose/dias/horário/duração chegam à revisão correta; relógios abrem 08:00, 20:00 e 09:00. Depois de abrir o seletor, botão rápido 12:00 leva à duração sem o seletor, confirmando o reparo entre perguntas de A31-01. RV-DVC-05 confirmado ao entrar em correção; voz vista em 2,0×; Prefiro digitar transfere o rascunho. Não foi fala real nem TTS audível. Não repetidos todos G07–G10 com minutos específicos.
- Nenhum crash AndroidRuntime observado no log consultado. Regressão de navegação, cadastro/revisão, edição e exclusão executada; testes de domínio/voz existentes passam. Ciclo temporal de M2 não repetido.

## Contraste

Recalculados independentemente os **13 pares opacos** da tabela de `AVALIACAO_CONTRASTE_WCAG.md`: os números
conferem até duas casas decimais. Exemplos: texto principal 16,31:1, secundário 8,84:1, branco/verde 6,46:1,
erro/fundo 6,21:1, borda/fundo 3,64:1 e borda/branco 3,83:1. Os limites usados estão de acordo com os critérios
[1.4.3](https://www.w3.org/TR/WCAG22/#contrast-minimum) e [1.4.11](https://www.w3.org/TR/WCAG22/#non-text-contrast)
da WCAG 2.2. O código mantém texto, borda e erro do tema ao mudar o fundo dos campos para branco.

Nenhuma insuficiência confirmada nos pares recalculados. Não foi conferência completa de todas as cores/estados
renderizados, nem de conformidade WCAG do app inteiro: opacidades decorativas, foco, TalkBack, ordem de leitura e
interfaces do sistema não foram auditados nesta rodada. O resultado não comprova acessibilidade para usuários.

## Restauração, limites e próximo passo

Entrada temporária em `SpeechInput.start()` retirada; SHA-256 restaurado exatamente para
`15135EF60C0F7BF6BB3F82A5395C9388EB954A7B5C85B84A810B4AB89D0AF9DD`. `app/src` sem diff final.
APK original recompilado e reinstalado só no emulador. Fonte devolvida a **1,0×** e relida; mídia, relógio e
permissões não alterados. Rascunhos descartados e medicamento temporário excluído; emulador na Hoje.

Não verificados: aceite no A31, reconhecimento/TTS reais, TalkBack, câmera/OCR em tela, ciclo de alarmes,
reabertura após morte do processo em todos os estados, todos os erros da edição em 2,0× e todas as combinações
de unidades/doses. Os originais acadêmicos foram lidos como marco de escopo; esta revisão não é avaliação com
participantes nem resultado da monografia. Próximo: reparos pontuais dos achados e conferência, sem novo ciclo
amplo no interpretador. Sem commit/push; conclusão do merge documental permanece pendente de commit.
