# Roteiro de revisão para o Codex — melhorias de M1/M2, M3 (voz) e M4 (câmera)

Preparado em 27/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Segue o roteiro comum do `HANDOFF_APP.md` §6;
este arquivo só acrescenta o alvo, os pontos de risco e os procedimentos de teste no emulador.

## 1. Alvo

`main`, intervalo **`a590fa5..9a235fc`** (tudo depois da última revisão do Codex, `REVISAO_CORRECAO_M2_CODEX.md`).

| Commit | O quê | RF |
| --- | --- | --- |
| `1ddd544` | Troca de tela sem esmaecer | RF02 |
| `290b638` | Relógio recusa horário repetido; calendário recusa término antes do início | RF07, RF08 |
| `d592b8a` | Hoje com cartão "Agora" (Entendi/Adiar) e "Lembrete adiado"; `AlertStore` (DEC-029) | RF09, RF25 |
| `4bebc3b` | `VoiceParser` + corpus (`VoiceParserTest`) | RF06, RF11–RF16 |
| `3200047` | Escolha Voz/Digitar, tela de voz, perguntas, revisão de vários, correção por toque (`FormMode.FixDraft`) | RF11–RF19 |
| `f2a7a05` | Mensagem para aparelho sem reconhecedor (erro 5) | RF11 |
| `f214691` | "(adiado)" nas repetições; erro de voz único; rótulo da barra até 2,0× | RF02, RF11, RF25 |
| `0c06d49` | Câmera: CameraX + ML Kit embutido + `OcrParser` | RF20–RF23 |
| `9a235fc` | Remove INTERNET, ACCESS_NETWORK_STATE e telemetria (`datatransport`) trazidas pelo ML Kit | RF20 |

Os commits `docs:` do intervalo só registram; confira se batem com o código.

## 2. Antes de começar

1. `git pull`; registre branch e commit de partida.
2. Leia `AGENTS.md`, `docs/ESTADO.md`, `docs/HANDOFF_APP.md` (§2, §3, §5 passo 8, §7 e Diário de 27/10),
   `docs/DECISOES.md` (DEC-004, DEC-012, DEC-014, DEC-016, DEC-028, **DEC-029**), `docs/ESPEC_ARQUITETURA.md` §4–§8.
3. `.\gradlew.bat assembleDebug test` — esperado: 87 testes, 0 falhas.
4. Emulador `Pixel_6_2` (API 35). O aparelho físico do autor (Pixel 8a, GrapheneOS) **não tem reconhecedor de voz**
   (ver Diário); não use para voz. O celular de teste definitivo (Samsung) ainda não chegou.

## 3. O que verificar, por área

Além da lista do §6 (escopo, `DraftValidator`, concentração ≠ dose, OCR sem dose/horários, nada de "Tomado",
datas no fuso do aparelho, testes da lógica pura), olhe com atenção:

### 3.1 Cartão do aviso na Hoje e `AlertStore` (DEC-029)
- `reminders/AlertStore.kt`, `domain/ActiveAlert.kt`, `ReminderReceiver`, `ReminderScheduler.cancel/respond`, `BootReceiver`, `ui/home/HomeScreen.kt`.
- O registro guarda **só avisos pendentes** e some com Entendi, desativar, excluir e reinício. Confirme que não vira
  histórico nem "Visto às" (DEC-004).
- Riscos que eu, implementador, não fechei: entrada velha que nunca some (ex.: atualização do app com aviso na tela,
  `MY_PACKAGE_REPLACED` não limpa); `snoozed()` sem entrada anterior não faz nada; toque duplo em Entendi/Adiar no cartão
  (`PendingIntent.send` duas vezes); escrita do receptor (thread de IO) e leitura da tela.

### 3.2 Formulário
- `ui/form/FormScreen.kt` (`TimeDialog` com `taken`, `DateDialog` com `SelectableDates`), `FormViewModel` (`FormMode`).
- **Possível padrão silencioso para o autor decidir:** rascunho da voz/câmera **sem dias** abre no formulário em
  "Todos os dias" (`FormFields.from(draft)`), igual ao cadastro manual. Se o usuário não reparar na revisão, grava dias que
  não disse. Avalie a gravidade.
- Data de início mudada **depois** do término: o calendário não impede (só o validador avisa). Confirme que é aceitável.

### 3.3 Voz
- `voice/VoiceParser.kt` (regras e lista de palavras comuns), `VoiceQuestions.kt`, `SpeechInput.kt`, `Speaker.kt`,
  `voice/ui/VoiceViewModel.kt`, `VoiceScreen.kt`; testes `VoiceParserTest` (28), `VoiceQuestionsTest` (5).
- Escolhas de interpretação a validar (KDoc e handoff §3 "Limitações do M3"): "às 8 e 20" = 08:20 (ESPEC §7);
  "às 8, 14 e 22" = três horários; "na verdade" troca o horário mais próximo; "só" troca todos; "os dois" vale para
  todos; nomes ligados por "e" sem nada no meio recebem as mesmas informações; "dia sim, dia não" e "por um mês" não são
  interpretados.
- **Tente quebrar o interpretador** (pelo teste unitário ou pela injeção de texto do §4.2). Sugestões:
  "tomo 2 comprimidos de losartana de 12 em 12 horas a partir das 8"; "metformina 850 duas vezes ao dia às 7";
  "AAS 100 mg depois do almoço"; "sinvastatina à noite" (sem número); "vitamina D uma vez por semana";
  "omeprazol em jejum às 6 e meia"; "remédio da pressão às 8"; "puran 50 microgramas"; "insulina 10 unidades às 7";
  "losartana às 8 e às 20, não, às 21"; nomes com número ("B12"); frase só com "sim"/"não".
  Para cada caso: o que virou nome? alguma concentração foi parar na dose ou o contrário? algum horário foi inventado?
- Fluxo: `VoiceViewModel` limpa a sessão no início (exceto vindo da câmera, `fromSession`); "Prefiro digitar" com 1
  rascunho abre o formulário, com vários vai para a revisão; salvar passa cada rascunho pelo validador.
- `SpeechInput`: fallback do reconhecedor do aparelho (erros 12/13) para o do sistema; flag `active` contra resposta dupla.
- `Speaker`: só fala com voz `pt`; `shutdown` ao sair da tela.
- Logcat (DEC-015): `voz_ouvindo`, `voz_resultado` (com latência), `voz_interpretado`, `voz_erro`; sem áudio.

### 3.4 Câmera
- `camera/OcrParser.kt` (+ `OcrParserTest`, 6), `camera/ui/CameraViewModel.kt`, `CameraScreen.kt`, rotas e manifest.
- **RF23:** nada da foto vai para dose, horários, dias ou tratamento.
- Câmera desliga ao sair da pré-visualização (`unbindAll` no `onDispose`); a fase só muda depois de a foto chegar (bug
  corrigido antes do commit: mudar antes cancelava a captura).
- `image.toBitmap()` em resolução cheia: risco de memória em aparelho fraco?
- **Rede:** confira no manifest final (`app/build/intermediates/merged_manifest/debug/.../AndroidManifest.xml`) que não
  há `INTERNET`, `ACCESS_NETWORK_STATE` nem componentes `datatransport`. Erro conhecido e inofensivo no Logcat ao usar a
  câmera: `TransportRuntime.Executor ... non-existent component ... JobInfoSchedulerService` (a telemetria tenta
  agendar e não consegue; o app não cai). Avalie se é aceitável ou se há forma mais limpa.
- Dependências novas: CameraX 1.4.2, `com.google.mlkit:text-recognition` 16.0.1 (modelo embutido; APK debug 55,7 MB).

### 3.5 Regressão
- §7 do handoff para M0, M1, M2, **M2 — DEC-029**, M3 e M4.
- Rótulo "Medicamentos" com fonte 1,5× e 2,0× (`adb shell settings put system font_scale 2.0`; volte para 1.0).

## 4. Procedimentos no emulador

### 4.1 Relógio (lembretes)
Handoff §5 passo 8: `settings put global auto_time 0` e `cmd alarm set-time <epoch ms>`. Roteiro mínimo: 30 s antes de
um horário → aviso e cartão "Agora"; Adiar → "Lembrete adiado / Novo alerta às…"; avançar o intervalo → "(adiado)";
avançar mais um intervalo → 1ª repetição ainda com "(adiado)"; Entendi no cartão → some. Teste também o ciclo de
**três repetições sem resposta** e **deslizar a notificação** (pendentes desde a revisão anterior). Ao terminar, volte a
hora real e `auto_time 1`.

### 4.2 Voz sem microfone (texto injetado)
O microfone do PC não chega ao emulador. Para conferir telas e fluxo, **ajuste local temporário, nunca commitado**, no
início de `SpeechInput.start()`:

```kotlin
// TEMP-REVISAO (não commitar)
val fake = Class.forName("android.os.SystemProperties").getMethod("get", String::class.java)
    .invoke(null, "debug.genki.voz") as String
if (fake.isNotEmpty()) { events.listening(true); events.listening(false); events.result(fake.replace('_', ' '), 0); return }
```

Depois: `adb shell setprop debug.genki.voz "tomo_losartana_50_mg_às_8"` e tocar no microfone (espaços viram `_`).
Para voltar ao microfone real, remova o ajuste (a propriedade `debug.*` some ao reiniciar o emulador). **Antes de encerrar: remova o ajuste, `git diff` sem nada em `app/src`, reinstale.**
No emulador o reconhecedor do aparelho não tem pt-BR (erro 13) e o do sistema usa a internet; registre, mas não é
problema do app.

### 4.3 Câmera com imagem de embalagem
A câmera do emulador mostra uma sala virtual sem texto (bom para testar a falha). Para a leitura, **ajuste local
temporário** em `CameraScreen.kt`, `onCaptureSuccess`, trocando as duas linhas de `rotation`/`bitmap` por:

```kotlin
// TEMP-REVISAO (não commitar)
val fake = java.io.File(context.getExternalFilesDir(null), "ocr.png")
val rotation = if (fake.exists()) 0 else image.imageInfo.rotationDegrees
val bitmap = if (fake.exists()) android.graphics.BitmapFactory.decodeFile(fake.path) else image.toBitmap()
```

e `adb push <imagem>.png /sdcard/Android/data/br.edu.ifrs.genki/files/ocr.png`. Há uma imagem pronta em
`build/ocr.png` (local, não versionada; "LOSARTANA POTÁSSICA / 50 mg / 30 comprimidos revestidos / …"). Faça também
uma imagem sua, com marca de laboratório em destaque ou volume em ml, para testar as limitações do `OcrParser`.
**Antes de encerrar: remova o ajuste e o arquivo do emulador.**

### 4.4 Limpeza
Apague os medicamentos de teste (o emulador já tem Metformina 08:00 e Losartana 19:30, que devem ficar), volte fonte e
relógio, nenhum ajuste local em `app/src`.

## 5. Relatório

- Salvar em `docs/revisoes/REVISAO_M3_M4_CODEX.md`, no formato das revisões anteriores: escopo, resultado, tabela de
  achados (ID `RV-M3-xx`/`RV-M4-xx`/`RV-M2B-xx`, S1–S4, arquivo:linha, problema, correção sugerida), verificação com
  evidências, limites (o que não foi verificado: voz real, TTS audível, aparelho físico, embalagens reais, TalkBack).
- Diga explicitamente se usou os ajustes locais de §4.2/§4.3 e que foram removidos.
- Atualizar `HANDOFF_APP.md` (§2/§3 e uma linha no Diário), `ESTADO.md`, `ENTREGAS.md`, `REGISTRO_IA.md`.
- **Não alterar código** (DEC-026). Não fazer push sem autorização do autor.

Execução sugerida (handoff §4): `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_M3_M4_CODEX.md`.
