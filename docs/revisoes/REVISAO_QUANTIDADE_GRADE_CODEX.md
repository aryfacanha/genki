# Revisão — quantidade da dose e grades — Codex

27/10/2026 — Codex (GPT-6). Partida: `main` / `a96184b`.
Alvos: `33a33ab` e `a96184b` (`ee23734..a96184b`; `095296f` apenas registra a revisão anterior).
`git pull`: Already up to date; seis commits locais à frente. Alterações de `.idea` preservadas.

## Resultado

**Sem achados S1–S4 confirmados no escopo.** Os dois commits estão revisados na cobertura abaixo; aceite do autor no A31 continua separado. Nenhuma correção permanente de código pelo revisor.

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| — | — | — | Nenhum defeito confirmado no escopo revisado | — |

S1 bloqueia; S2 erro relevante; S3 menor; S4 sugestão.

## Base e inspeção

Estado, handoff, DEC-026/033/036 e ESPEC vigentes consultados. [Proposta oficial](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view) e [requisitos oficiais](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit) reabertos pelo conector do Drive nesta rodada.

- **Compromisso da proposta:** cadastro com confirmação, correção, legibilidade e alternativas de interação; relacionados RF01/02/03/05/17/18/19 e RNF01–05.
- **Decisão aceita:** opções de quantidade e caixas alinhadas são pedidos do autor registrados nas entregas/Diário do Claude, complementando a DEC-036. Nenhuma decisão nova aceita nesta revisão.
- **Implementado:** `DoseUnit.amounts`; `FormScreen.kt:274` mantém unidade/quantidade e alternativa digitada; `Components.kt:238` distribui `ChoiceGrid` pela largura e maior rótulo medido; `VoiceScreen.kt:328`/`:570` usa a grade nas respostas e dias.

As opções continuam gerando o mesmo texto de dose. A grade reserva largura por coluna, mantém a última linha alinhada e recalcula colunas com a fonte. Seleção, callbacks, nomes completos dos dias do formulário e altura mínima de 48 dp permanecem. Respostas da voz continuam pelo interpretador; o caminho de gravação/`DraftValidator` não mudou. Sem nova dependência, rede, alteração de banco/manifest, inferência por OCR, indicação de ingestão ou alteração de datas/agendamento.

## Verificado

`.\gradlew.bat assembleDebug test` passou: **127 testes executados, zero falhas/erros**. Após retirar a entrada temporária, o mesmo comando passou de novo (testes `UP-TO-DATE`). Diff sem erros de whitespace; `app/src` final sem alterações. Hash SHA-256 de `VoiceScreen.kt` restaurado idêntico à cópia inicial.

Emulador Pixel_6_2/API 35, instalação com `-s emulator-5554 install -r`:

| Fluxo | Resultado observado |
| --- | --- |
| Quantidade pronta e Outra | Comprimido mostra Meio/1/2/3/Outra. Outra abre o campo; `4` permite avançar e permanece ao voltar, com Outra selecionada |
| Troca para Outra após quantidade pronta | `1` → Outra limpa a quantidade; Continuar mantém a etapa e mostra “Informe quanto você toma por vez.” ao rolar. Meio permite avançar |
| Dias do formulário | Dias específicos abre sete caixas; marcar segunda mantém os mesmos limites das caixas antes/depois e atualiza “Dias selecionados: segunda” |
| Fonte 2,0× no formulário | Unidades em duas colunas, quantidades em três, dias abreviados em quatro; rótulos e ✓ legíveis. Outra quantidade acessível pela rolagem |
| Respostas da voz | Entrada temporária `losartana` inicia pergunta da dose; toque em 1 comprimido avança para dias |
| Dias da voz em 1,0× e 2,0× | Segunda e quarta marcadas; grade passa de três para duas colunas, texto legível. Confirmar dias preenche ambos e pergunta os horários |
| Horários/duração da voz em 2,0× | Grades legíveis em duas colunas; 08:00 e 20:00 → Uso contínuo → revisão com nome, dose, dias, horários e duração corretos; nada salvo |
| Aviso de volume no APK original em 2,0× | Mídia 2/15 antes de entrar na voz: aviso central legível, ausente na captura após esperar 5 s. Complementa a observação parcial de `ee23734` |
| Regressão curta | Hoje, escolha de cadastro, Medicamentos e Ajustes; cancelamento/descarte manual e da voz. Lista final mantém Metformina/Losartana com dados anteriores |

Evidências locais ignoradas pelo Git: `build/grid_*` (XMLs e capturas), incluindo `grid_four_return.xml`, `grid_amount_error.xml`, `grid_days_before.xml`, `grid_days_after.xml`, `grid_dose_large.png`, `grid_voice_picker_large_bottom.png`, `grid_voice_review_large.png` e `grid_volume_large_shown.png`/`hidden.png`.

## Limites e restauração

A entrada temporária foi uma chamada local de `VoiceViewModel.heard`, conforme o roteiro do handoff; foi retirada antes do build final. Não houve reconhecimento real de áudio. Na troca de fonte, a entrada temporária reaplicou o nome durante uma pergunta e produziu “Não entendi”; isso pertence à instrumentação e não foi classificado como defeito do app. O fluxo continuou pelos botões.

APK original recompilado/reinstalado só no emulador; abertura da voz original sem rascunho injetado conferida. Rascunhos de teste descartados, nenhum medicamento salvo/editado/excluído. Fonte **1,0×** e mídia **8/15** restauradas e relidas; relógio/permissões preservados. Log do processo final com `app_iniciado`, TTS inicializado e descarte, sem erro E ou crash AndroidRuntime observado. A31 não operado.

**Não verificado nesta rodada:** TalkBack anunciando estados/aviso, voz/TTS audíveis, câmera, ciclo temporal dos lembretes, todas as combinações de unidades/dias e teclado nas perguntas da voz. O ensaio do aviso cobre entrada com volume baixo e expiração, não cronometra precisamente a animação nem repete todas as transições baixo/alto/baixo. A grade de dose da voz foi observada em 1,0×; dias, horários e duração também em 2,0×. Fonte ampliada confirma esses grupos, não uma auditoria completa das telas.

Próximo: aceite do autor no A31 e verificações técnicas previstas. Sem novo reparo indicado por esta revisão; sem commit/push ou mudança permanente de configuração pelo Codex.
