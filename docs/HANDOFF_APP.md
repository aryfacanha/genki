# HANDOFF — App Android Genki (Claude Code ↔ Codex)

**Contexto geral e estável do app.** Para começar, leia nesta ordem: `AGENTS.md`, `docs/ESTADO.md`, **este arquivo** e `docs/HANDOFF_VERSAO.md` (o que está sendo feito agora). Confira o estado real (`git log`, build) e não confie só no texto.

| Arquivo | Para que serve | Ler |
| --- | --- | --- |
| `docs/HANDOFF_APP.md` (este) | O que é o app, estado por marco, como rodar, roteiros de emulador e de revisão, regressão rápida, divisão de trabalho | sempre |
| `docs/HANDOFF_VERSAO.md` | A versão em curso: estado, pendências, verificado e não verificado, regressão pendente, Diário do ciclo. **Reescrito a cada versão** | sempre |
| `docs/historico/HANDOFF_HISTORICO_ate_1.0.25.md` | Registro antigo, movido sem reescrever (preâmbulo, retomadas, revisões do Codex de 26 a 30/10, Diário até 03/11) | só para investigar o passado |

**Ao terminar uma alteração:** atualize o `HANDOFF_VERSAO.md` (estado, verificado/não verificado, regressão pendente) e acrescente uma linha no Diário dele. Mexa neste arquivo só se o contexto global mudar (marco, como rodar, roteiros, divisão de trabalho). **Ao publicar uma versão** (tag e Release), arquive o `HANDOFF_VERSAO.md` conforme a §4 e recomece o arquivo. Este arquivo é o retrato técnico do código; o estado do projeto inteiro (prazos, CEP, monografia) fica no `ESTADO.md`.

## Retomar aqui

Ver `docs/HANDOFF_VERSAO.md`. As retomadas antigas (a partir de 28/10/2026) estão no histórico.

## 1. O que é

App Android do Genki (Kotlin + Compose + Room), especificado em `docs/ESPEC_ARQUITETURA.md`.
Referência visual: protótipo web `D:\dev\genki-prototype` (github.com/aryfacanha/genki-prototype, `docs/HANDOFF.md`).
Divergências de dados/regras entre os dois: `docs/DIVERGENCIAS_PROTOTIPO.md` (até o autor decidir, vale a ESPEC — DEC-022).

## 2. Estado por marco

Marcos de base abaixo. Estado da versão em curso e das versões recentes: `docs/HANDOFF_VERSAO.md`. Revisões e conferências do Codex por marco (DEC-037 a DEC-047, 27 a 29/10) e as versões 1.0.20 a 1.0.22: histórico.

| Marco | Situação | Commits |
| --- | --- | --- |
| M0 Base | Implementado, compilado e aberto no emulador (Pixel_6_2, API 35) em 26/10: regressão da seção 7 passa. Tema e barra inferior alinhados ao protótipo `295212e`. Aceite manual do autor no aparelho (ESPEC §9) pendente | `c1f2b7e`, `cec21ab`, `53fe97a`, correção do rótulo (ver Diário) |
| M1 CRUD | Implementado, compilado e conferido no emulador em 26/10 (cadastro em 3 etapas + revisão, lista, detalhes, edição, exclusão; dados persistem ao reabrir). Decisões #2, #7, #12 aplicadas (DEC-027). Codex revisou em `42a0d16`, sem achados novos na cobertura do relatório. Aceite manual do autor no aparelho pendente | `f2ff130` |
| M2 Lembretes | Implementado e compilado; fluxo principal conferido pelo Claude Code em 26/10 (DEC-028). **RV-M2-01/02 corrigidos em `a590fa5` e fechados pelo Codex no emulador em 26/10**, sem novos achados na correção. Avisos do último dia/minutos consecutivos e reabertura com novo processo passam; Adiar/Entendi e cancelamento ao desativar conferidos. Retorno de Adiar, ciclo completo de três repetições/deslizar conferidos pelo Codex no emulador em 27/10. RV-M2B-01 fechado pelo Codex em 27/10, com reprodução independente da virada do dia; aceite no aparelho pendente | `f111670`, `a590fa5`; `revisoes/REVISAO_CORRECAO_M2_CODEX.md` |
| M2 — melhorias do autor (DEC-029) | Implementado e compilado em 27/10; instalado no Pixel 8a (Android 17) do autor, tela não conferida (aparelho bloqueado). Cartão "Agora"/"Lembrete adiado" na Hoje, relógio sem horário repetido, término não anterior ao início, sem esmaecer. Revisão do Codex concluída em 27/10: fluxo temporal passa; RV-M2B-01 corrigido em `7726739` e fechado pelo Codex na reprodução independente da virada do dia (27/10). DEC-032/033 e `caafe29` revisados pelo Codex no emulador: toque na notificação abre Hoje e preserva cadastro/edição; grupos Adiar/Entendi e tipos mistos passam; Entendi no minuto atual não reaparece como próximo. Aceite completo no A31 pendente | `1ddd544`, `290b638`, `d592b8a`, `d7b8c63`, `24888ec`, `caafe29` |
| M3 Voz | **Implementado, compilado e revisado pelo Codex na cobertura técnica executada (27/10).** Interpretador, perguntas/respostas por botão e texto, revisão múltipla, correção por toque/voz com Manter/Desfazer e digitação (DEC-031/033). RV-M3C-03/D-01 já fechados; **RV-M3-02 (resto)/E-01 fechados** na conferência de `0cef3f1`. Build/112 testes e 43 reprodutores locais passam; sem achado novo no escopo restrito. Voz real/TTS no A31 verificados pelo autor/Claude (§3); Codex não operou aparelhos nesta conferência. Aceite completo no A31 pendente | `4bebc3b`, `3200047`, `69711ce`, `fac6a0a`, `8191c25`, `24888ec`, `0cef3f1`; `revisoes/CONFERENCIA_M3_CODEX.md` |
| M4 Câmera | **Implementado e compilado; conferido no emulador (Pixel_6_2, API 35) em 27/10.** "Usar câmera" na escolha; CameraX (pré-visualização + uma foto, sem gravar) → ML Kit com modelo latino embutido (sem internet e sem Google Play Services) → `OcrParser` (nome = linha mais longa sem algarismos entre as 3 maiores; concentração = 1º número + mg/mcg/g/ml/UI) → "Confira o que eu li" com nome e concentração editáveis (RF22) → "Continuar por voz" (perguntas a partir da dose) ou "Continuar digitando" (formulário preenchido); falha → "Tentar de novo" ou "Digitar". Dose, horários, dias e tratamento nunca vêm da foto (RF23, teste). Revisão do Codex concluída em 27/10: fluxo de OCR passa na cobertura com imagens injetadas; RV-M4-01 (S3, origem no log ao salvar pela voz) corrigido em `7726739` e fechado pelo Codex com `fonte=CAMERA` no emulador em 27/10. Leitura real no aparelho (20 ensaios) pendente | `0c06d49` |

## 3. Verificado e não verificado

O verificado e o não verificado da versão em curso ficam em `docs/HANDOFF_VERSAO.md`; os das versões anteriores, no histórico. **Atalho de teste de alarme (1.0.22, 30/10)** — o método abaixo continua válido:

**1.0.22 (30/10, Claude Code, emulador API 35):** com `adb root`, `am start-foreground-service -n br.edu.ifrs.genki/.reminders.AlarmSoundService --el timeId N --es name 'Nome' --ei minute M` (aspas simples dentro de `adb shell "..."` para nome com espaço) e `am broadcast -n br.edu.ifrs.genki/.reminders.ReminderReceiver -a ACK --el timeId N`: frases, 3 falas em 20 s, parada no minuto, Entendi cala fala e som, TTS com `USAGE_ALARM`. Não verificados: ouvido, volume baixando, disparo real, tela bloqueada, A31/Samsung TTS, S20 FE.

## 4. Como rodar

- Repositório único (DEC-054, revoga a DEC-053): código e registros no `genki`. Versão nova: tag anotada `git tag -a vX.Y.Z <commit> -m "Genki X.Y.Z"` no commit do `versionName` e `git push origin vX.Y.Z`.
- Release de versão nova: `gh release create vX.Y.Z Genki-X.Y.Z.apk --title "Genki X.Y.Z" --notes-file <notas> --latest --verify-tag` (APK release assinado, renomeado; conferir o download).
- Build e testes (PowerShell, na raiz): `.\gradlew.bat assembleDebug test`.
- Instalar: `.\gradlew.bat installDebug` com um aparelho em `adb devices`.
- APK de distribuição: `.\gradlew.bat assembleRelease` gera `app/build/outputs/apk/release/app-release.apk`, assinado se `local.properties` tiver `genki.release.storeFile/storePassword/keyAlias/keyPassword` (chave fora do Git). Conferir com `apksigner verify --print-certs`. Versão nova: mesma chave e `versionCode` maior.
- Versões: AGP 9.4.1 (Kotlin 2.2.10 embutido), Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26 (DEC-011).
- Codex CLI (forma que funcionou nas revisões do protótipo): `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < roteiro.md`
  (roteiro via stdin). Ajuste o modelo se o CLI recusar.
- **Arquivar a versão ao publicar a Release:** depois da tag e da Release, mova o conteúdo do `HANDOFF_VERSAO.md` (estado, verificado, Diário do ciclo) para `docs/historico/HANDOFF_HISTORICO_ate_X.Y.Z.md` (ou acrescente ao arquivo histórico existente), sem reescrever, e recomece o `HANDOFF_VERSAO.md` para a versão seguinte.

## 5. Checagem no emulador (roteiro comum; skill `/testar-emulador` do Claude Code aponta para cá)

Checagem rápida automatizada. **Não substitui a verificação manual do autor** (ESPEC S5). Não altera código.

1. `adb devices` — se nenhum aparelho aparecer como "device", pare e peça ao autor para abrir o emulador
   no Device Manager ou conectar o celular.
2. `.\gradlew.bat assembleDebug test` — se falhar, mostre o erro e pare.
3. `.\gradlew.bat installDebug`
4. `adb logcat -c` e depois `adb shell am start -n br.edu.ifrs.genki/.MainActivity`; aguarde ~3 s.
5. Captura de tela (não use redirecionamento `>` no PowerShell, corrompe a imagem):
   `adb shell screencap -p /sdcard/genki_tela.png` e `adb pull /sdcard/genki_tela.png build/genki_tela.png`.
   Compare com o esperado para a tarefa e com o protótipo, se disponível.
6. Se o foco pedir navegação, use `adb shell input tap X Y` / `input text` e capture de novo.
7. `adb logcat -d *:E` (erros e crashes) e `adb logcat -d -s GenkiEval`.
8. **Relógio para testar lembretes** (sem root; conferido no emulador API 35 em 27/10): `adb shell settings put global auto_time 0`
   e `adb shell cmd alarm set-time <epoch em ms>` (ex.: 30 s antes de um horário cadastrado; o alarme toca na hora e,
   se o salto passar do horário, toca logo). Ao terminar: `cmd alarm set-time` com a hora real e `settings put global auto_time 1`.
   Em aparelho com Android antigo o comando pode não existir: mudar a hora pelos Ajustes do sistema.

Relatório, sempre neste formato:
- Build e testes: passou/falhou
- Instalado em: <modelo do aparelho/emulador e API>
- O que a captura mostra x o esperado
- Erros no logcat (ou "nenhum")
- NÃO verificado: voz, câmera, alarmes reais, leitor de tela, texto ampliado — verificação manual do autor

Se achar problema, descreva e pergunte se deve corrigir.

## 6. Revisão (roteiro comum; skill `/revisar` do Claude Code aponta para cá)

Alvo: o commit ou intervalo indicado (padrão: `git show HEAD`). Quem revisa **não altera código**, salvo pedido do autor.

1. Leia o diff do alvo, `docs/ESPEC_ARQUITETURA.md` (seções pertinentes) e as regras do `AGENTS.md`.
2. Rode `.\gradlew.bat assembleDebug test`.
3. Verifique, no mínimo:
   - escopo: só o RF/marco pedido; nada de Hilt, WorkManager, DataStore, rede, LLM;
   - toda gravação passa pelo `DraftValidator`;
   - concentração ≠ dose; OCR não preenche dose/horários/dias/tratamento;
   - nada de "Tomado" nem histórico de ingestão;
   - datas civis no fuso do aparelho;
   - textos e fluxo coerentes com o protótipo (`D:\dev\genki-prototype`, se disponível);
   - testes unitários da lógica pura existem e passam;
   - regressão rápida da seção 7 para os marcos já entregues.
4. Liste achados em tabela: ID, gravidade (S1 bloqueia · S2 erro relevante · S3 menor · S4 sugestão),
   arquivo:linha, problema, correção sugerida.
5. Diga o que não pôde verificar (aparelho, voz etc.).
6. Grave o relatório em `docs/revisoes/REVISAO_<marco ou pacote>_<ASSISTENTE>.md` e uma linha no Diário.

## 7. Regressão rápida (por marco entregue)

A regressão pendente da versão em curso fica em `docs/HANDOFF_VERSAO.md`; as regressões de revisão por marco (27 a 30/10), no histórico. Abaixo, o que vale sempre:

**Alarme falado (1.0.22):** com "Como alarme" e a opção ligada, o alarme fala em ~1,5 s e a cada 20 s, com o som mais baixo durante a fala; um remédio → horário e nome; vários no mesmo minuto → quantos; Entendi/Adiar calam; opção desligada ou "Como notificação" → sem fala; opção some de Ajustes no modo notificação.

**Voz (1.0.21):** falar o nome e calar: a escuta encerra sozinha em até ~4 s, mesmo em celular cujo reconhecedor não encerra; pausa curta no meio da frase ainda junta os trechos; tocar no microfone encerra em até 1,5 s; abrir a escuta e não falar → "Não ouvi nada" em até 8 s; ruído contínuo para em 30 s.

**Backup (1.0.20):** `adb shell dumpsys package br.edu.ifrs.genki | grep pkgFlags` sem `ALLOW_BACKUP` e `adb shell bmgr backupnow br.edu.ifrs.genki` → "Backup is not allowed". Atualizar por cima de versão anterior mantém os cadastros.

**Guia externo, 30/10:** ao mudar instalação, permissões, voz, câmera ou lembretes, conferir também `GUIA_INSTALACAO_TESTE.md` e README. Para exploração informal, o guia cobre cadastro manual → notificação/Entendi, voz/correção, câmera opcional e limpeza dos cadastros fictícios. Não substitui a bateria completa nem o protocolo acadêmico; roteiro não executado nesta entrega documental.

- **M0:** o app abre na aba Hoje; as três abas (Hoje, Medicamentos, Ajustes) trocam de tela; a aba ativa tem o fundo
  verde-claro atrás do ícone e o rótulo em negrito; nenhum texto roxo/lilás do Material padrão; build debug registra
  `GenkiEval` no Logcat (`adb logcat -d -s GenkiEval`).
- **M1:** Medicamentos vazia mostra "Nenhum medicamento cadastrado."; "Adicionar medicamento" → etapas 1–3 bloqueiam com erro em campo vazio; dois horários iguais → "Este horário já foi adicionado."; "Tem data para terminar" exige término não anterior ao início; revisão mostra tudo e "Alterar" volta só àquela etapa; "Confirmar e salvar" → cartão na lista e aviso acima do botão; fechar e reabrir o app mantém o cartão; detalhes → Editar (sair com mudança pergunta "Descartar alterações?"), Desativar lembretes (cartão mostra "Lembretes desligados"), Excluir pede confirmação. Logcat: `cadastro_iniciado`, `medicamento_salvo`, `medicamento_editado`, `medicamento_excluido`.
- **M2:** Hoje mostra a data por extenso, o próximo lembrete e a lista do dia (sem "Visto"/"Adiado"); Ajustes mostra repetição, intervalo e permissões; ao salvar o 1º medicamento sem permissão, aparece "Permitir notificações?"; com um horário 2–3 min à frente, a notificação chega no horário com "Entendi" e "Adiar N min"; sem resposta repete a cada N min até 3 vezes; "Adiar" volta em N min com "(adiado)"; "Entendi" (ou deslizar) some e cancela as repetições; "Desativar lembretes" cancela os alarmes (`adb shell dumpsys alarm | grep genki`). Logcat: `lembrete_emitido`, `lembrete_adiado`, `lembrete_reconhecido`.
- **M2 — casos adicionais da revisão Codex:** no último dia do tratamento, o aviso da última ocorrência deve permanecer até interação (RV-M2-01); dois horários do mesmo medicamento em minutos consecutivos devem manter os dois alarmes (RV-M2-02). Para configurar minutos consecutivos pelo cadastro, selecione os minutos e arraste o ponteiro entre as marcações do relógio; confira os valores na revisão antes de salvar (22:44/22:45 configurados assim pelo Codex em 26/10). Após reiniciar/desbloquear, conferir alarmes futuros antes de abrir a Activity (reagendamento conferido no emulador em 26/10).
- **M2 — DEC-029:** com um aviso tocando, a Hoje mostra o cartão "Agora" com "Entendi" e "Adiar N min" e, abaixo, "Depois: HH:MM · nome"; "Adiar" troca o cartão por "Lembrete adiado / Novo alerta às HH:MM / Horário original"; "Entendi" (no cartão ou na notificação) tira os dois; desativar/excluir tira o cartão. No relógio, escolher um horário já da lista mostra "Este horário já foi adicionado." e desabilita OK; no término, dias antes do início ficam desabilitados.
- **M3:** "Adicionar medicamento" abre "Como você quer cadastrar?"; "Usar voz" → microfone (pede permissão na 1ª vez) → falar "losartana 50 mg um comprimido às 8 todos os dias uso contínuo" → revisão direto; frase incompleta → "Entendi até agora" + pergunta escrita e falada; botões de resposta rápida; "Prefiro digitar" leva ao formulário preenchido; na revisão, "Corrigir" abre o formulário e volta; "Corrigir por voz"; "Confirmar e salvar" → aviso e Hoje. Logcat: `voz_ouvindo`, `voz_resultado`, `voz_interpretado`, `medicamento_salvo fonte=VOICE`. **Sem microfone no emulador:** o microfone do PC não chegou ao emulador em 27/10; para conferir o fluxo, injetar texto com um ajuste local em `SpeechInput.start()` e removê-lo antes do commit.
- **M4:** "Usar câmera" → permitir câmera → pré-visualização e "Tirar foto" → "Lendo a embalagem…" → "Confira o que eu li" (nome e concentração editáveis; nada de dose/horários) → "Continuar por voz" pergunta a dose; "Continuar digitando" abre o formulário preenchido; foto sem texto → "Não consegui ler a embalagem." com "Tentar de novo"/"Digitar". Logcat: `cadastro_iniciado fonte=CAMERA`, `ocr_resultado` (texto, nome, concentração, ms), `ocr_confirmado`, `medicamento_salvo fonte=CAMERA`. No emulador a câmera mostra uma sala virtual sem texto; para testar a leitura, trocar a foto por uma imagem com ajuste local e removê-lo antes do commit.

## 8. Divisão de trabalho (DEC-026)

- **Claude Code implementa; Codex revisa** (mesma divisão do protótipo, decisão do autor de 21/10).
- **Modo só Codex:** quando o limite do Claude Code acabar, o Codex implementa seguindo este arquivo e escreve no Diário
  "modo só Codex — aguardando revisão". Ao voltar, o Claude Code revisa primeiro esse trabalho (seção 6) e só depois
  começa algo novo.
- Nunca os dois editando os mesmos arquivos ao mesmo tempo. Um marco (ou parte dele) por commit.
- Mudança de configuração (`.claude/`, skills, `AGENTS.md`, Gradle, versões, permissões do manifest) entra no Diário
  com o que mudou e por quê, para o outro assistente saber.
- Push só com autorização do autor.

## Diário

O Diário do ciclo em curso está em `docs/HANDOFF_VERSAO.md` (uma linha por alteração, no topo); o de 26/10 a 03/11, no histórico.
