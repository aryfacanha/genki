# Conferência pontual — reparos RV-INF-01 a RV-INF-05

29/10/2026 — Codex (GPT-6.1). **RV-INF-01/02/03/05 fechados na cobertura executada; RV-INF-04 parcial, S2 residual aberto.** Nenhum achado novo S1–S4 confirmado. Nenhum reparo permanente de código, commit ou push.

## Partida e escopo

- Branch `main`, HEAD de partida e final `a893fe864c95c37a02899ebfc70ac75dc4d163e0`. O alvo pedido é `7f91441`, versão 1.0.8, versionCode 9, sobre `a1f2ebe`. O único diff entre alvo e HEAD é o roteiro documental `ROTEIRO_CONFERENCIA_RV_INF_CODEX.md`; o código conferido coincide com o alvo.
- `git pull` tentado e bloqueado: `cannot open '.git/FETCH_HEAD': Permission denied`. Atualização remota não verificada; alvo local disponível.
- Alterações preexistentes preservadas: `.idea/gradle.xml`, `.idea/misc.xml`, itens não rastreados em `.idea/` e `.claude/settings.local.json`.
- Consultados estado, decisões pertinentes (especialmente DEC-026/046), handoff §2/3/5/6/7, relatório anterior, ESPEC e mapa RF/RNF. Reabertos pelo conector Google Drive a [proposta oficial](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view) e os [requisitos oficiais](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit), estes fornecidos como texto de XLSX armazenado no Drive. Escopo restrito aos reparos, regressões indicadas e atalho de digitação; sem revisão ampla.
- Todas as operações de aparelho dirigidas explicitamente a `emulator-5554` (API 35). A31 apenas listado por `adb devices`, sem operação nele. Chave de assinatura não usada; somente build/instalação debug.

## Resultado por achado

| Achado | Situação | Evidência |
| --- | --- | --- |
| RV-INF-01, S2 | **Fechado** | `Confirm` exige todas as palavras no conjunto permitido e ao menos uma central. Testes de “sim, mas falta atenolol” e “pronto, remover metoprolol” passam. No emulador: Losartana + Metformina → ressalva → três cartões, incluindo Atenolol, ainda em “Confira os remédios”. `rv01.xml/png`; log de `Other` às 15:22:43.631 em `rv03.log`. Confirmações curtas continuam passando. |
| RV-INF-02, S2 | **Fechado** | O segundo “é” é literal acentuado em `NOT_IS`. Reprodutor retorna `Other("não é losartana e metformina")`, encaminhado ao `VoiceParser`, sem `Rename` desse comando. “não é X, é Y” e “não é X é Y” continuam cobertos e passam. Não se atribui entendimento semântico geral de negativas ao interpretador. |
| RV-INF-03, S2 | **Fechado** | Dois compostos com Metoprolol → `Ambiguous`; rascunhos inteiros iguais antes/depois no probe de ViewModel. No emulador, “remover metoprolol” preserva “Rivaroxabana metoprolol” e “Atenolol metoprolol” e mostra “Há mais de um remédio com Metoprolol. Toque no nome ou no X do que quer corrigir.” Log `Ambiguous` às 15:23:30.186; `rv03_after.xml/png`. Probe confirma que o prompt tem o mesmo texto da mensagem; `VoiceScreen` o entrega ao `Speaker`, com TTS pt-BR `pronto=true`. **Som audível não conferido.** |
| RV-INF-04, S2 | **Parcial; residual aberto** | Voltar e Cancelar interno durante TTS deixam a lista intacta e não abrem escuta posterior. Cancelar interno também descarta trecho em espera e callback pendente. **Voltar com `state.listening=true` ainda chama `input.stop()` antes de verificar `renaming`, mantendo a edição e entregando texto em vez de descartá-lo.** Dois reprodutores na tela alteram o nome após Voltar; detalhes abaixo. |
| RV-INF-05, S3 | **Fechado** | Composto + inteiro: “remover sinvastatina e metoprolol” deixa só Rivaroxabana, no probe e na tela (`rv05.xml/png`). Probes adicionais confirmam igualdade integral dos rascunhos quando um alvo não existe ou quando a ambiguidade aparece depois de um alvo inteiro válido; nenhuma remoção parcial aplicada. |

## RV-INF-04 — reprodução residual e encaminhamento

Local: `app/src/main/java/br/edu/ifrs/genki/voice/ui/VoiceScreen.kt:175–176`. A ordem do `when` é:

```kotlin
state.listening -> input.stop()
state.renaming != null -> { speaker.stop(); input.cancel(); viewModel.cancelRename() }
```

`cancelRename()` agora anula o prompt, mas não é chamado pelo primeiro ramo. `SpeechInput.stop()` preserva o contrato de finalizar a resposta; durante a pausa entrega imediatamente os trechos já reconhecidos. Esse contrato serve ao botão de parar o microfone, mas não cancela a troca.

1. Lista com Losartana; tocar seu nome; deixar terminar a pergunta “Diga o nome certo…”. A injeção em `SpeechInput.start()` coloca “atenolol” em `parts`, com `active=true`, `waiting=true` e entrega agendada. É o estado de espera depois de um trecho reconhecido, usando `stop()`/`deliver()` originais.
2. `voz_ouvindo` às **15:26:01.335**; Voltar durante essa espera; `voz_resultado texto="atenolol"` às **15:26:03.411**, antes da entrega agendada. Tela muda para **Atenolol**. Esperado: continuar com Losartana, edição encerrada, sem aplicar resposta. Em seguida o novo resumo abre outra escuta às **15:26:07.836**.
3. Segunda variante: lista agora com Atenolol; tocar nome; escuta com resultado “dipirona” agendado pelo substituto. `voz_ouvindo` às **15:27:08.558**; Voltar enquanto ouve mantém o campo de troca e “Ouvindo…” (`back_pending_immediate.xml/png`). Callback às **15:27:20.561**, `active=true`, passa por `listener.onResults` original e renomeia para **Dipirona** (`back_pending_final.xml/png`).

Evidência local ignorada pelo Git: `build/conferencia_rv_inf/back_waiting.log`, `back_waiting_immediate.xml/png`, `back_waiting_final.xml/png`, `back_pending.log` e capturas acima. Os horários e resultados essenciais estão transcritos aqui para passagem de contexto.

**Sugestão pendente:** dar prioridade ao cancelamento da troca em Voltar, inclusive com escuta ativa, usando o mesmo encerramento do Cancelar interno (`speaker.stop`, `input.cancel`, `cancelRename`). Preservar o comportamento do botão do microfone e de Voltar nos demais modos. Repetir só essas variantes após reparo. O revisor não alterou essa lógica.

| Variante na tela | Resultado |
| --- | --- |
| Voltar durante TTS | Passa: Losartana preservada; oito segundos de observação após o toque, sem `voz_ouvindo` no intervalo. |
| Cancelar interno durante TTS | Passa: mesmo resultado; não foi usado o Cancelar do topo, que sai do cadastro. |
| Voltar durante pausa com trecho já reconhecido | **Falha:** entrega e troca Losartana por Atenolol; nova escuta após o resumo. |
| Cancelar interno durante pausa com trecho | Passa: Atenolol preservado, “dipirona” descartada; sem resultado ou nova escuta no intervalo observado. |
| Voltar durante escuta com callback pendente | **Falha:** edição permanece aberta; callback troca Atenolol por Dipirona. |
| Cancelar interno durante escuta com callback pendente | Passa: Dipirona preservada; callback “metformina” às 15:27:50.624 registra `active=false` e não entrega resultado. |

## Build, testes e regressões pedidas

- `.\gradlew.bat assembleDebug test` passou na partida e após retirada dos temporários. **Execução final: 166 testes, zero falhas, erros ou ignorados**, com `testDebugUnitTest` executado. Não houve build release nesta conferência.
- Repetidos os oito probes de `InformalReviewProbeTest` da revisão anterior: **8/8 passam**. Duas expectativas foram ajustadas à API reparada: antigo `None` → `Other(texto)` e antigo `NotFound` → `Ambiguous`. Os comportamentos esperados permanecem não executar troca pela conjunção e não escolher um composto arbitrariamente.
- Os outros seis probes verificam ressalva sem pular nomes, remoção composta inteira, divisão digitada “A, B” preservando todos os campos/IDs distintos, fala completa → nomes → revisão, câmera → dose com origem preservada e comparação conservadora. Passam.
- Cinco probes adicionais passam: ambiguidade sem mudança de rascunho e mesmo texto no prompt; remoção composta atômica com alvo inexistente; com alvo ambíguo depois de alvo válido; digitação com um; digitação com vários e correção sem retornar a `NAMES`. Total com temporários: **179 testes, zero falhas/erros/ignorados**. Não detectam o residual de `handleBack`, pois ele pertence à tela.
- Testes existentes de confirmações curtas, “trocar X por Y”, “não é X, é Y” e Separar passam. As regressões de câmera e fala completa foram repetidas em ViewModel, sem captura real de embalagem ou fala humana.
- Tentativas com wrapper combinado a outros comandos falharam por tentativa de download/rede restrita; chamadas isoladas do wrapper funcionaram. Uma tentativa de incluir probes por init/sourceSet Java não os incluiu na compilação Kotlin; a execução válida usou dois arquivos temporários em `src/test`, depois removidos. Nenhuma configuração permanente foi alterada.

## “Prefiro digitar” e fonte

- Um medicamento: após RV-INF-05 deixar Rivaroxabana, o atalho abre **Novo medicamento**, etapa 1 de 3, nome preservado (`typing_single.xml/png`).
- Vários: Losartana + Metformina, atalho em fonte **2,0×** → **Confira antes de salvar**, com os dois rascunhos e campos faltantes explícitos (`typing_multiple2x.xml/png`). A lista não volta à conferência no probe de correção posterior.
- Em 2,0×, captura `names2x.png` inspecionada: título em duas linhas, nomes e ações legíveis, “Prefiro digitar” inteiro na área inferior, acionado com sucesso. O texto explicativo excede a área visível dentro da coluna rolável; isso não oculta o atalho. Sem teste de TalkBack.

## Restauração e limites

- Injeção limitada a `SpeechInput.start()` durante a execução. Modos controlados: resultado direto; trecho aguardando pausa; callback de reconhecimento pendente. `Speaker`, `VoiceScreen`, `stop`, `cancel`, `deliver` e `listener.onResults` permaneceram originais. Isso verifica o fluxo/cancelamento com entrada simulada e callback real do TTS, não o serviço real de reconhecimento nem a audibilidade.
- Nos modos de pausa/callback pendente, a janela simulada foi de **12 segundos** para permitir o acionamento e a captura automatizados; não é uma medição da pausa real de 1,5 s. O residual depende do ramo escolhido com `listening=true`, não de uma alteração desse contrato de cancelamento.
- Temporários de `src/test` retirados. `SpeechInput` restaurado byte a byte, SHA-256 **`77717ab1856873761d094bac1292cbdfc7c6609a95425b3c84d1a23c70c9851b`**. `git diff -- app` vazio. Build final original reinstalado com `adb -s emulator-5554 install -r`.
- Antes dos testes houve backup de `databases`, `shared_prefs` e `files`. Depois da instalação original, todos os arquivos regulares desse backup foram restaurados e comparados **byte a byte** antes da reabertura, incluindo banco/WAL/SHM e preferências. Após reabrir, `medication` (**2 registros**), `schedule_time` (**2**) e `room_master_table` (**1**) coincidem integralmente com a partida. Nenhum medicamento de teste salvo.
- Fonte **1,0×** e permissão de microfone inicialmente ausente restauradas. Permissão de notificações, canais, relógio e rotação não alterados. Hoje final com os mesmos Metformina/Losartana, próximo lembrete 19:30. Buffer de crash vazio. Artefatos auxiliares permanecem locais em `build/conferencia_rv_inf/`, ignorados pelo Git.
- Não verificados: A31/GrapheneOS, voz humana/pausas em reconhecedor real, TTS audível, TalkBack, assinatura/release, migração real ou bateria ampla de alarmes/OCR/tutorial. As coberturas anteriores permanecem históricas, sem atribuir nova execução a esta rodada.

## Categorias e próximo passo

**Compromisso da proposta:** voz assistiva com confirmação/correção e alternativa por toque/texto, sem precisão clínica presumida. **Decisão aceita:** DEC-046 e pedido de reparo/atalho já registrados pelo Claude; nenhuma decisão nova nesta sessão. **Implementado:** pacote `7f91441`. **Verificado:** somente os casos acima. **Sugestão pendente:** concluir o residual de RV-INF-04 e solicitar conferência pontual desse percurso. Não equivale a aceite no aparelho ou resultado de pesquisa; texto não integrado à monografia.
