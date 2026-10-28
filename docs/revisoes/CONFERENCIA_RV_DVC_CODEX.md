# Conferência pontual — reparos RV-DVC-01…05

28/10/2026 — Codex (GPT-6). Partida: `main`/`51a8d1973b88d79900aa868e5d79a4f2d45af2d0`.
Alvo: `7ffddeb..a57c591`. O código de `app/src` da partida é idêntico ao de `a57c591`;
o commit posterior `51a8d19` altera somente documentação. Roteiro: `ROTEIRO_CONFERENCIA_RV_DVC_CODEX.md` e handoff §6.

**Resultado: RV-DVC-01…05 fechados na cobertura executada. Nenhum achado novo S1–S4 confirmado no diff.**
Build final e **134 testes executados, zero falhas, erros ou ignorados**. Conferência no emulador API 35,
com fala simulada; aceite no A31 permanece separado. Sem alteração permanente de código/configuração, commit ou push.

## Partida e escopo

`git pull` foi tentado e recusado: `cannot open '.git/FETCH_HEAD': Permission denied`.
Falta permissão de escrita nesse arquivo de metadados Git; não foi possível confirmar atualização com o remoto.
Isso não impediu conferir o alvo local disponível. Alterações preexistentes em `.idea/` e
`.claude/settings.local.json` foram preservadas; HEAD continua em `51a8d19`.

Lidos ESTADO, decisões pertinentes (DEC-026, 037–039), ESPEC, mapa de requisitos, handoff e revisão original.
Reabertos a [proposta oficial](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view)
e `Requisitos!A1:F38` da [planilha oficial](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit).
O diff mantém o compromisso de confirmação/correção e alternativas por toque, sem ampliar o produto.
Não altera banco, validação, dependências, permissões, datas, OCR ou lembretes.

## Resultado por achado

| ID | Gravidade original | Situação | Evidência do reparo |
| --- | --- | --- | --- |
| RV-DVC-01 | S2 | **Fechado** | `DoseUnit.kt:58–72` preserva 120 e impede +; `FormScreen.kt:417` usa `enabled = canPlus`. No emulador: Gota → 120 → + mantém 120, botão visualmente apagado; − produz 119. |
| RV-DVC-02 | S2 | **Fechado** | `DoseUnit.kt:46–69`: conversão em milésimos e subtração sem chegar a zero nos casos pedidos. No emulador: ml 0,25 → + 0,75 → − 0,25, − apagado; 1,25 → + 1,75. Revisão do formulário mostra **0,75 ml**. |
| RV-DVC-03 | S2 | **Fechado** | `VoiceQuestions.kt:63–67` fornece `everyHours` na pergunta do primeiro horário; `VoiceScreen.kt:590–638` escolhe um só, mostra `Frequency.fill` e substitui. Voz simulada “metformina três vezes ao dia” → dose por toque → relógio 07:30 → **07:30, 15:30 e 23:30**; “Trocar horário” por 08:30 mostra só **00:30, 08:30 e 16:30**. Após escolher novamente 07:30, confirmar e informar duração, a revisão mantém os três horários esperados. Pergunta comum mantém seleção múltipla: 08:00 e 20:00 chegaram juntos à revisão. |
| RV-DVC-04 | S3 | **Fechado** | `DoseUnit.kt:33–37`/`HALVES`: `1/2` e `½` viram 0,5 na operação; testes passam. Registro temporário criado pela voz/revisão com **1/2 comprimido**, reaberto no editor com **1/2**, salvo sem editar e relido ainda como **1/2 comprimido**. Novo Editar → + chega a **1**, e salvar mostra **1 comprimido**. |
| RV-DVC-05 | S3 | **Fechado** | `VoiceScreen.kt:290,354`: efeito fecha `picking` e ramo exige `!state.correcting`. Com seletor aberto e horário escolhido, “Algo errado? Corrigir por voz” mostra “Diga a correção” sem seletor; Voltar retorna à pergunta sem seletor. Também conferido resultado de correção → **Desfazer**: pergunta comum reaparece sem seletor. |

Nenhum reparo adicional indicado nesta conferência. O + apagado comunica indisponibilidade; não explica o número
100. Não foi classificado como novo achado: o comportamento pedido está perceptível e não muda a dose digitada.
O arredondamento das operações até milésimos é a limitação expressamente admitida pelo roteiro; não representa
preservação de precisão arbitrária além de três casas decimais.

## Testes e diferença em relação aos reprodutores antigos

O arquivo `build/TemporaryDoseVoiceContrastReviewTest.kt` ainda existia. Foi copiado temporariamente para `test/`
e executado **sem adaptação**: 138 testes ao todo, **duas falhas** nos quatro reprodutores; a suíte permanente
de 134 e os reprodutores de decimal/fração passaram. As duas expectativas antigas conflitam com o reparo pedido:

- `plusMustNotReduceATypedAmount` exigia **121** ao tocar + em 120; a regra reparada exige **120** e + desabilitado.
- `selectedTimesWithPendingFrequencyArePreserved` entregava **07:30 e 15:00** ao parser com frequência de 8 h;
  a UI reparada permite um único horário inicial. O parser não foi alterado para aceitar duas escolhas nessa pergunta.

Depois, somente esses dois reprodutores temporários foram adequados: esperar 120/`canPlus=false` e fornecer
07:30 com `everyHours=8`, esperando 07:30/15:30/23:30. **138/138 passaram**, incluindo os quatro reprodutores.
Isso corrige a expectativa “os quatro antigos passam” do roteiro; não se declara que os quatro originais passaram.
XMLs locais: `build/conferencia_rv_dvc_originais.xml` e `build/conferencia_rv_dvc_adequados.xml` (ignorados pelo Git).

Retirados os reprodutores, `.\gradlew.bat assembleDebug test` passou novamente com execução de
`testDebugUnitTest`: XMLs finais somam **134 testes, 0 falhas, 0 erros, 0 ignorados**.
`DoseUnitTest` (10) inclui `everyStepperAmountComesBackTheSame`, `stepperNeverLosesWhatWasTyped` e
`buildsSingularAndPlural`; `VoiceParserTest` (55) inclui `timesFromClockAreUnderstood`.
Uma tentativa avulsa de `testDebugUnitTest` esbarrou no download do wrapper por rede restrita;
o comando prescrito `assembleDebug test` executou com a distribuição disponível.

## Emulador, restauração e limites

Operado exclusivamente `adb -s emulator-5554` (Pixel_6_2, `sdk_gphone64_x86_64`, API 35), fonte 1,0×.
Capturas/árvores conferidas; exemplos locais em `build/conferencia_gota120.png`, `conferencia_ml025.png`,
`conferencia_revisao075.png`, `conferencia_frequencia0730.png`, `conferencia_frequencia_troca.png`,
`conferencia_revisao_frequencia.png`, `conferencia_desfazer_sem_relogio.png` e `conferencia_edicao_fracao.png`.
Ida/volta Comprimido/1 → Outro (“1 comprimido”) → Comprimido/1 também conferida.

A fala foi injetada temporariamente em `SpeechInput.start()` lendo um arquivo privado de teste no emulador.
Para Desfazer, o resultado foi entregue após 700 ms na thread principal, permitindo renderizar o modo de correção,
como acontece com o callback assíncrono real. A versão inicial síncrona da simulação não exercitava essa transição
de forma fiel e foi substituída antes de concluir a conferência; não se atribuiu seu efeito ao APK original.
O caso Voltar foi conferido mantendo a correção sem resultado automático.

Entrada temporária retirada e `SpeechInput.kt` restaurado byte a byte (SHA-256
`15135EF60C0F7BF6BB3F82A5395C9388EB954A7B5C85B84A810B4AB89D0AF9DD`).
`git diff --exit-code -- app/src` sem diferenças; teste temporário e cópia antiga em `build/` removidos.
Arquivo privado de fala removido do emulador. APK original recompilado e reinstalado com `install -r` (**Success**)
apenas no emulador. Cadastro manual/rascunhos de voz descartados; registro **Conferenciarvdvc** (id 19) excluído
pela interface. Metformina e Losartana anteriores preservadas na lista após a reinstalação.
Fonte 1,0× mantida; mídia, permissões e relógio não alterados. Sem crash AndroidRuntime observado no log da sessão
e sem erro no processo final consultado. Emulador deixado na Hoje.

**Não verificados nesta rodada:** A31 (não operado), reconhecimento/TTS reais, TalkBack, fontes ampliadas,
câmera/OCR, ciclo temporal dos lembretes, morte do processo nos estados do seletor e combinações fora dos cinco
achados. Não foi novo ciclo amplo, avaliação com participantes ou integração à monografia.
Próximo: bateria do autor no A31 e verificações técnicas já previstas. Nenhuma decisão de produto nova.
