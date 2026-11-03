# Segunda revisão da correção de M3/M4 — Codex

**Atualização em 27/10/2026:** a [terceira revisão](REVISAO_CORRECAO3_M3_M4_CODEX.md) fechou RV-M3C-03/RV-M3D-01; RV-M3-02 mantém um S2 residual na lista de dias com “na verdade”. Novo RV-M3E-01 S3: correção explícita de nome ignorada durante pergunta, com alternativa pelo botão. O texto abaixo preserva a situação histórica da segunda revisão.

Data: 27/10/2026. Assistente: Codex (GPT-6).
Partida: `main`, `cb98e3c02d20ed1c5bbe0a9a8532c000c966cfa5`; `git pull`: atualizado.
Alvo: `69711ce`, apenas `VoiceParser.kt` e `VoiceParserTest`, conforme `ROTEIRO_REVISAO_CORRECAO2_M3_M4_CODEX.md`.

## Resultado

**Três achados fechados: RV-M3-04, RV-M3C-01 e RV-M3C-02. RV-M3-02 e RV-M3C-03 permanecem parcialmente
corrigidos e abertos (S2). Novo RV-M3D-01 (S2): ano explícito ignorado. Total: três S2 abertos.**

Os 21 reprodutores locais das revisões anteriores passam. Os casos vizinhos desta rodada mostram uma regressão na
correção de dias dentro da mesma fala e uma falha preexistente ao responder uma pergunta com data inválida.
O ano explícito é intencionalmente descartado pelo código, mas essa regra não foi aceita pelo autor e pode alterar
o término informado; a recomendação mínima é pedir esclarecimento quando não houver suporte ao formato.

Build e **101 testes existentes passam**. Código/configuração sem alteração permanente. Não houve testes de tela,
instalação ou interação com o emulador nesta rodada (opcionais no roteiro); nenhum dado/relógio/fonte do aparelho alterado.

## Fontes e execução

Lidos AGENTS, ESTADO, decisões pertinentes (especialmente DEC-003/014/026/030), handoff §2/§3/§6,
especificação §4/§7, roteiro, revisão anterior e diff completo do commit. Proposta oficial reaberta no Google Drive;
planilha oficial consultada em `Requisitos!A7:F18` (RF06–RF17). Não houve bloqueio de acesso aos originais.

- `assembleDebug test` inicial passou com tarefas `UP-TO-DATE`.
- Copiados, como solicitado, os reprodutores de `build/TemporaryReviewTest.kt` (7 testes) e
  `build/TemporaryCorrectionReviewTest.kt` (14 testes), com a cópia antiga do parser necessária a `compareBaseline`:
  **21/21 passam**, incluindo o teste de comparação, que é observacional e não tem asserção.
- Criado `TemporarySecondCorrectionTest` com 15 testes (incluindo um probe observacional). **11 passam/4 falham**;
  quatro falhas correspondem a três problemas, pois a data inválida falha tanto em TREATMENT quanto em END_DATE.
- Para atribuir a origem, comparado também o parser imediatamente anterior ao reparo (`69711ce^`), copiado apenas
  para os testes como `VoiceParserBeforeSecond`. A correção de dias na mesma fala funcionava antes e regrediu;
  datas inválidas na pergunta já produziam o mesmo erro. Nenhum parser de comparação entrou no código do app.
- A execução conjunta com o filtro `*Temporary*` selecionou também um método de `DraftValidatorTest` cujo nome contém
  Temporary (passou): 37 testes selecionados, quatro falhas. Os números de reprodutores acima usam seus XMLs separados.
- Após remover todos os arquivos temporários de `app/src/test`, `assembleDebug test` passou com execução de
  `testDebugUnitTest`. XMLs finais: **101 testes, zero falhas/erros**. `git diff -- app/src` vazio.

Evidências locais não versionadas: `build/second_correction_TEST-br.edu.ifrs.genki.voice.TemporaryReviewTest.xml`,
`build/second_correction_TEST-br.edu.ifrs.genki.voice.TemporaryCorrectionReviewTest.xml`,
`build/second_correction_TEST-br.edu.ifrs.genki.voice.TemporarySecondCorrectionTest.xml`,
`build/TemporarySecondCorrectionTest.kt` e `build/VoiceParserBeforeSecond.kt`.

## Situação dos cinco achados anteriores

| ID | Gravidade | Situação e evidência |
| --- | --- | --- |
| RV-M3-02 | S2 | **Aberto, correção parcial.** Os dias de trechos aditivos agora se somam, inclusive “e sexta e também sábado”; dois medicamentos mantêm seus próprios dias. Porém, `losartana segunda e quarta na verdade só sexta` termina com segunda/quarta/sexta. Antes de `69711ce`, terminava só com sexta. Em `voice/VoiceParser.kt:442`, `spokenDays` é sempre unido no ramo aditivo; `correction`/`replaceAll` retiram apenas `beforeDays`, sem limpar os dias anteriores da própria fala. **Regressão confirmada.** Sugestão: na correção explícita, reiniciar também o acumulador da fala para o medicamento afetado e depois somar os trechos da nova resposta; ou pedir esclarecimento, preservando o estado até confirmar. |
| RV-M3-04 | S2 | **Fechado na cobertura executada.** Com 08:00/20:00 prévios, “às 20 não às 21” mantém 08:00/21:00. Sem negação, continua somando. “às 8 não às 9 não às 10” termina só em 10:00; “não às 21” sem horário dito antes na fala corrige o anterior; “os dois às 8 não às 9” altera ambos para 09:00. “Não sei” como duração continua indefinida; “não tomo de manhã, só à noite” não inventa horário nem apaga o rascunho. |
| RV-M3C-01 | S2 | **Fechado na cobertura executada.** `vitamina d 8 horas` mantém o nome e 08:00; ao perguntar horário, `vitamina d 8` preenche o mesmo rascunho, sem criar D8. B12/D3 e dose após letra continuam separados dos horários. |
| RV-M3C-02 | S2 | **Fechado para o caso relatado.** `losartana 1/2 comprimido às 8` mantém dose `1/2 comprimido`, sem transformar o denominador em dose; horário preservado. A dose continua texto, sem conversão numérica/clínica. Limites adicionais abaixo. |
| RV-M3C-03 | S2 | **Aberto, correção parcial.** Datas válidas `até 15/12`, `15/12/2026` e `até 1/1` usam mês correto; `31/2` e `15/13` sem contexto de pergunta não preenchem término. Entretanto, quando `expecting=TREATMENT` ou END_DATE, `até 31/2` termina em **02/11/2026**, e `até 15/13` em **13/11/2026**, para hoje 27/10/2026. O mesmo ocorre na resposta END_DATE `31/2`. Em `voice/VoiceParser.kt:405` e `:413`, o formato inválido retorna sem consumir/rejeitar o trecho; o laço (`:156`) continua e `askedAboutTime` (`:396`) interpreta o último número como outro dia. **Preexistente ao reparo, confirmado por comparação.** Com nome/dose/dias/horário completos, o validador retorna `Valid` para esse término incorreto. Sugestão: impedir a reinterpretação dos fragmentos de uma data rejeitada e manter a pergunta pendente, sem substituir por um prefixo/sufixo válido. |

## Novo achado

| ID | Gravidade | Arquivo:linha | Problema e evidência | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-M3D-01 | S2 | `voice/VoiceParser.kt:407` | **Ano explícito descartado.** Em 27/10/2026, `até 15/12/2025` e `até 15/12/2027` produzem ambos 15/12/**2026**. O código consome o ano e não o usa; `civilDate` (`:419`) calcula a próxima ocorrência só por dia/mês. Com o restante do cadastro preenchido, o validador considera o rascunho `Valid`. RF08/RF15/RF17: a confirmação não substitui a obrigação de preservar ou esclarecer o que foi dito. O roteiro pede avaliar esse comportamento; não há DEC aceitando ignorar o ano. A versão anterior já errava a data com barra, mas por ignorar também o mês; não é correção de um comportamento antes correto. | Respeitar o ano explícito; se esse formato ficar fora do MVP, rejeitá-lo/pedir a data por extenso ou pelo formulário. Para ano passado, apresentar erro ou pedir esclarecimento, em vez de avançar silenciosamente para outro ano. Não é necessário ampliar o cadastro para tratamentos históricos. |

## Demais casos do roteiro

- “segunda, quarta e também sexta e sábado” soma os quatro; “na verdade só sábado e também domingo” como resposta
  a rascunho anterior troca para sábado/domingo. A falha surge ao corrigir dados já ditos na **mesma** fala, acima.
- “todos os dias” no meio de dias específicos resulta nos sete dias. `losartana segunda e metformina sexta` preserva
  segunda apenas no primeiro e sexta apenas no segundo.
- `complexo b 6 comprimidos` mantém nome `Complexo b` e dose `6 comprimidos`; `vitamina b 12 às 8` mantém B12/08:00.
- `vitamina d 8` fora da pergunta de horário vira `Vitamina D8`, sem horário. É uma ambiguidade da regra de nome
  com número, apresentada para revisão e ainda com horário pendente. Registrada como **limitação observada**, sem
  nova decisão de produto nem exigência de adivinhar se 8 seria dose, hora ou parte do nome.
- `1/2 comprimido e meio` é preservado literalmente. `3/4 de comprimido` não é suportado e deixa dose vazia;
  `1/2` sem unidade, como resposta de dose, também deixa dose vazia. Ambos exigem completar a dose; não viram
  silenciosamente `4 comprimidos` ou `2 comprimidos`. Não se avaliou interpretação clínica dessas expressões.
- `até 1/1` retorna 01/01/2027, conforme a regra existente para dia/mês **sem** ano.

## Limpeza, limites e próximo passo

Removidos de `app/src/test` os três testes temporários e as duas cópias de parser. Nenhum ajuste em
`SpeechInput`, câmera, manifest, Gradle ou dependências; nenhuma instalação/interação no emulador nesta sessão.
Dados de teste, relógio e fonte do aparelho não foram alterados. Mudanças locais preexistentes de `.idea/` e
`.claude/` preservadas; sem commit/push.

Não verificados nesta rodada: telas (opcionais no roteiro), voz real, TTS audível, câmera/embalagens reais,
aparelho físico, TalkBack e alarmes. A verificação anterior de integração/virada do dia permanece atribuída à sessão
anterior; não foi repetida nem apresentada como evidência nova.

Próximo passo: corrigir RV-M3-02, RV-M3C-03 e RV-M3D-01 com tratamento conservador de entrada não suportada, incluindo
pergunta de duração/data nos testes, e revisar o reparo. Nenhuma recomendação desta revisão foi implementada pelo
revisor ou aceita como nova decisão do autor. Código compilado e testes unitários passando não equivalem a aceite
no aparelho nem a medição de reconhecimento real.
