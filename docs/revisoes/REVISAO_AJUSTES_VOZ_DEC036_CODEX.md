# Revisão dos ajustes de voz e DEC-036 — Codex

**Atualização de 27/10/2026 — Codex:** RV-VZ-01/02 corrigidos em `d4d6350` e **fechados** na
conferência independente `REVISAO_CORRECAO_AJUSTES_VOZ_DEC036_CODEX.md`. Build/126 testes e os
nove reprodutores passam; Cancelar/voltar, manter/descartar, cadastro vazio e edição sem mudanças
conferidos no emulador. Sem achado novo no escopo; abaixo permanece o registro histórico da revisão.

27/10/2026 — Codex (GPT-6). Partida: `main`/`0ea0da7`; `git pull`: já atualizado.
Alvos: `fa75715` e `0ea0da7`, diff `a6d9b5a..0ea0da7`.
Roteiro: `HANDOFF_APP.md` §5/6/7 e casos pertinentes de `ROTEIRO_TESTE_VOZ.md`.
Lidos estado, DEC-031/033/036, ESPEC §4/5/7, proposta e planilha atuais no Drive.

**Resultado: dois S2 abertos — um comportamento novo no Cancelar e uma falha preexistente ainda
presente nos casos vizinhos da correção de horários. Nenhum S1 encontrado na cobertura executada.**
As correções simples de horários passam; o seletor de dias, teclado, unidade/quantidade e aviso
de volume passaram nas verificações descritas abaixo. Não houve correção de código pelo revisor.

| ID | Gravidade | Arquivo:linha | Problema e reprodução | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-VZ-01 | **S2 — novo** | `app/src/main/java/br/edu/ifrs/genki/ui/form/FormScreen.kt:111`; origem do estado em `FormViewModel.kt:163–165` | **Cancelar não confirma o descarte dos dados recebidos da voz.** Falar Losartana com dose, escolher dias e informar 08:00; Prefiro digitar abre Novo medicamento preenchido. Sem editar esse formulário, Cancelar volta diretamente à Hoje, sem “Descartar cadastro?”. `changed` compara com o baseline, que já contém os dados da voz, e por isso continua falso. O cadastro sai da navegação; não há botão para retomá-lo. Contraria a DEC-036, inclusive o caso que motivou o botão. No cadastro manual que foi alterado, o diálogo aparece normalmente. | Separar “há dados no cadastro novo” de “houve mudança em relação ao baseline”. Cancelar deve pedir confirmação também para um rascunho preenchido recebido da voz/câmera, preservando o comportamento da edição/correção. |
| RV-VZ-02 | **S2 — preexistente; correção parcial** | `app/src/main/java/br/edu/ifrs/genki/voice/VoiceParser.kt:287–295` e `:226–234` | **Outra correção na mesma fala mantém o horário descartado.** “losartana às 8 na verdade às 9 na verdade às 10” resulta em **08:00 e 10:00**, em vez de só 10:00. Trocar o segundo horário por “na verdade todos os dias” resulta em 08:00 e 09:00. Também acontece quando a segunda correção é de outro medicamento. `correct` agrega os horários brutos à base e limpa o trecho, sem consolidar a substituição anterior. Comparação com `a6d9b5a` confirma que esses casos já falhavam antes; a mudança corrige uma correção simples, mas não encerra esses vizinhos diretos de V32/RF16. | Ao começar outra correção, consolidar o resultado já corrigido, evitando recolocar o horário substituído e respeitando cada medicamento. Manter os casos simples e a negação explícita que já passam. |

## Testes e comparação

- `.\gradlew.bat assembleDebug test` passou inicialmente (`UP-TO-DATE`) e **passou novamente após
  retirar os temporários, com execução dos 125 testes existentes: zero falhas/erros**.
- Nove testes temporários adicionais: **seis passam e três falham**, todos os três correspondendo
  às variantes de RV-VZ-02. Passam correção simples, correção em duas falas separadas, negação de
  horário conhecido, dia primeiro com ano explícito, dose quantidade/unidade e preservação de Outro.
- Uma comparação observacional adicional com cópia do parser anterior confirma a preexistência:

| Fala | Esperado | Antes (`a6d9b5a`) | Atual (`0ea0da7`) |
| --- | --- | --- | --- |
| Losartana às 8, na verdade às 9, na verdade às 10 | 10:00 | 08:00, 09:00, 10:00 | 08:00, 10:00 |
| Losartana às 8, na verdade às 9, na verdade todos os dias | 09:00 | 08:00, 09:00 | 08:00, 09:00 |
| Losartana às 8, na verdade às 9 e Metformina às 20, na verdade às 21 | Losartana 09:00; Metformina 21:00 | Losartana 08:00/09:00; Metformina 20:00/21:00 | Losartana 08:00/09:00; Metformina 21:00 |

Reprodutores e XMLs locais em `build/TemporaryVoiceRoundReviewTest.kt`, `build/VoiceParserBeforeRound.kt`,
`build/voice_round_probe.xml` e `build/voice_round_comparison.xml` (não versionados).
Não é uma regressão nova atribuída a `fa75715`; é uma limitação reproduzida da correção no escopo revisado.
Os casos antigos de M3/Hoje já fechados não foram reabertos sem evidência.

## Emulador e regressão

Instalado somente em **Pixel_6_2, API 35 (`emulator-5554`)**. Para conferir as telas, `SpeechInput.start`
devolveu temporariamente “losartana 1 comprimido”; isso testa a entrada do parser e o fluxo visual,
sem representar reconhecimento de áudio real. Ajuste removido, APK original recompilado e reinstalado ao fim.

- Cadastro manual vazio: Cancelar sai; após nome + Comprimido + quantidade 2, Continuar abre etapa 2;
  Cancelar pergunta “Descartar cadastro?”, Continuar editando mantém e Descartar sai. Nada salvo.
- Dias da voz: Escolher os dias → Segunda e Quarta → Confirmar dias mostra “segunda e quarta” no
  rascunho e avança para horários; a resposta passa pelo interpretador.
- Digitar a resposta: campo e Enviar ficam acima do teclado, microfone inferior é retirado;
  enviar “as 8” preenche 08:00 e segue para a duração. Captura conferida visualmente.
- Prefiro digitar: nome/dose/dias/horário recebidos; Comprimido e quantidade 1 aparecem corretamente.
  Cancelar sem alteração local reproduz RV-VZ-01.
- Volume de mídia **2/15**: aparece “O volume está baixo. Aumente para ouvir as perguntas.”;
  restaurado a **7/15**: aviso some. Não houve avaliação de áudio/TTS audível.
- Abas e dois medicamentos originais continuam presentes; aviso pendente de Losartana preservado,
  sem tocar em Entendi/Adiar. Nenhum medicamento criado, editado ou excluído.
- Diff limitado às regras e interfaces descritas; `DraftValidator` e os caminhos de gravação permanecem
  inalterados. Sem concentração convertida em dose, inferência nova por OCR, histórico de ingestão,
  rede, dependências ou configuração nova do app. “Outro” preserva doses fora da lista nos testes puros.
- Nenhum crash AndroidRuntime observado no processo final. Volume restaurado a 7/15; fonte 1,0×
  e relógio automático mantidos. `git diff -- app/src` vazio; dois testes/cópias temporários removidos.

Capturas locais em `build/`: `voz_round_cancel_manual.png`, `voz_round_keyboard.png`,
`voz_round_before_cancel.png`, `voz_round_after_cancel.png`, `voz_round_low_volume_confirmed.png`,
`voz_round_high_volume.png` e `voz_round_original_medications.png`.

## Limites e próximo passo

A31 não operado nem atualizado nesta revisão. Não verificados: voz/TTS audíveis reais, TalkBack,
fonte ampliada nas novas unidades, edição completa de medicamento existente, câmera e ciclo temporal
completo dos lembretes nesta rodada. O roteiro manual inteiro V01–V51 não foi executado.
Nenhum participante, resultado clínico, aceite do autor ou validação no aparelho é inferido.

Próximo: reparo pontual de RV-VZ-01 e RV-VZ-02, depois conferência dos casos reproduzidos e instalação
no A31 quando solicitada. Sem alteração permanente de código/configuração, commit ou push;
arquivos locais de `.idea/` e `.claude/settings.local.json` preservados.
