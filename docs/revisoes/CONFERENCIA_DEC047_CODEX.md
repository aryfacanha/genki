# Conferência pontual — DEC-047 — Codex

29/10/2026 · Codex (GPT-6.1) · versão 1.0.10 (versionCode 11).

**Resultado: um S2 e três S3 confirmados.** Achado 1 parcialmente corrigido: recuperação do adiado funciona, mas o
disparo com processo encerrado pode emitir outro alerta cinco segundos depois. Achado 2 conferido sem defeito na
cobertura executada. Achados 3–4 parcialmente corrigidos: digitação disponível, com problemas de seleção, rotação e
fonte ampliada. Sem correção permanente pelo revisor; sem commit, push ou operação no A31.

## Alvo, contexto e limites

- Partida: `main`/`013c3c2ad7bcda6b58f849258946e5d4ccf90fe2`. Esse commit só acrescenta o roteiro; o app corresponde a
  `260a535`, sobre `ebb6883`. `git pull` tentado e bloqueado por falta de escrita em `.git/FETCH_HEAD`; remoto não confirmado.
- Escopo: apenas achados 1, 2 e 3–4 de `PRE_REVISAO_HEURISTICA_A31.md`, conforme roteiro e DEC-047. Não é revisão ampla
  nem execução do MET-3 pelo pesquisador. Não fecha nem reabre os achados RV-INF de outras conferências.
- Lidos: estado, decisões pertinentes, handoff, especificação técnica, mapa de requisitos, pré-revisão e diff do alvo.
  Reabertos pelo conector Drive a [proposta oficial](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view)
  e `Requisitos!A1:H45` da [planilha oficial](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit).
  A DEC-047 aceita prevalece sobre os trechos antigos da ESPEC §3/§6 que ainda dizem duplicidade sem tratamento e
  adiamentos perdidos no reinício. Essa desatualização foi identificada antes de depender desses trechos.
- Emulador Pixel_6_2, `emulator-5554`, API 35; todos os comandos de operação ADB com serial explícito. O A31 estava
  conectado, mas não foi operado. Alterações preexistentes em `.idea/` e `.claude/settings.local.json` preservadas.

## Achados confirmados

S1 = bloqueia · S2 = erro relevante · S3 = menor · S4 = sugestão.

| ID | Gravidade | Arquivo:linha | Problema confirmado | Correção sugerida, pendente do autor |
| --- | --- | --- | --- | --- |
| RV-047-01 | **S2** | `reminders/ReminderScheduler.kt:85`; chamada em `data/MedicationRepository.kt:50` e `GenkiApplication.kt:21` | Quando o alarme adiado inicia um processo novo, `syncAll()` pode restaurar o próprio adiado em entrega para `agora + 5 s`. O receptor emite o primeiro aviso e muda o cartão para tocando, mas o segundo alarme já ficou agendado. Foram observados dois `FIRE_SNOOZED` do mesmo horário com intervalo de cinco segundos, sem nova interação e com intervalo configurado de dez minutos. | Coordenar recuperação e entrega do adiado, para não recriar um alarme já consumido/em entrega. Conferir também a inicialização pelo receiver, além da abertura da Activity; preservar recuperação de vencidos e unicidade nas chamadas repetidas de `syncAll()`. |
| RV-047-02 | **S3** | `ui/components/TimeDialog.kt:118` e `:124` | Tocar no centro do campo Minuto com `30` coloca o cursor, sem conservar a seleção total pretendida pelo `onFocusChanged`. Digitar `7` deixa `30` inalterado: a edição tenta acrescentar um terceiro dígito e é recusada. A seleção automática inicial e a passagem por teclado funcionam, mas não garantem a substituição por toque pedida no roteiro. | Fazer a seleção total persistir após o gesto de foco/toque, sem reselecionar a cada dígito; testar toque no número, além do foco automático e Tab. |
| RV-047-03 | **S3** | `ui/components/TimeDialog.kt:59` | Com o diálogo aberto e `18:21` digitado, girar de retrato para paisagem restaurou silenciosamente `19:30`, valor inicial da edição, com OK habilitado. `typing` usa `rememberSaveable`, enquanto hora e minuto usam somente `remember` (`:60–61`). A entrada ainda não confirmada é perdida. | Preservar os dois `TextFieldValue` na recriação, por exemplo com `rememberSaveable` e o Saver apropriado, e manter os modos sincronizados. |
| RV-047-04 | **S3** | `ui/components/TimeDialog.kt:119` e `:124` | Em fonte 2,0×, no retrato de 411 dp do emulador, a largura fixa de 112 dp faz o rótulo Minuto quebrar em `Minut` / `o`. O campo fica mais alto e desalinhado em relação a Hora. Números e ações continuam visíveis. | Permitir largura/arranjo compatível com a fonte ampliada, mantendo os rótulos inteiros; conferir o diálogo com teclado aberto. |

### Reprodução do RV-047-01

Executado com APK original, sem simulação de voz:

1. Um lembrete real `FIRE` das 19:30 foi emitido; tocar em **Adiar 10 min** guardou o adiado.
2. Parada forçada e reabertura preservaram cartão e um único alarme no prazo prometido.
3. Com o adiado restaurado, levar o relógio do emulador a cerca de vinte segundos antes do prazo; aguardar a
   sincronização por troca de hora, ir ao launcher e executar `adb -s emulator-5554 shell am kill br.edu.ifrs.genki`.
   `pidof br.edu.ifrs.genki` retornou vazio; não se usou parada forçada nessa etapa, pois ela cancelaria o alarme.
4. Aguardar o prazo sem abrir a Activity nem interagir. Trecho do `GenkiEval`:

```text
10-29 19:40:23.516 4922 4922 app_iniciado
10-29 19:40:23.607 4922 4936 lembrete_emitido timeId=42 acao=FIRE_SNOOZED repeticao=0
10-29 19:40:28.596 4922 4936 lembrete_emitido timeId=42 acao=FIRE_SNOOZED repeticao=0
```

O ID 42 veio da edição temporária feita pela interface; o backup original tem horários 5 e 6. O segundo evento não é
`REPEAT`. Usar o mesmo `PendingIntent` evita duas entradas simultâneas ao restaurar, mas não impede recriar o alarme
depois que o Android já iniciou sua entrega. Uma segunda tentativa para tocar **Entendi** entre as duas emissões
não obteve árvore de interface válida a tempo; não se afirma que esse percurso foi verificado.

### Reprodução dos S3 do diálogo

- **Seleção:** editar Losartana, abrir o horário 19:30, escolher Digitar o horário; tocar no centro do campo com 30
  e digitar 7. Captura mostra cursor/indicador de inserção, sem seleção total; depois da tecla continua 30.
- **Rotação:** no mesmo diálogo, com foco automático, digitar hora `1` e depois `8`; focar minuto e digitar `2` e
  depois `1`. Captura/árvore antes: 18:21. Girar: captura/árvore depois: 19:30, mantendo a entrada digitada.
- **Fonte:** no modo digitado, fonte do sistema 2,0×, retrato, teclado numérico aberto: Minuto quebra dentro da palavra.

## Verificado

| Ponto | Evidência e resultado |
| --- | --- |
| Build e testes originais | `assembleDebug test` passou. `testDebugUnitTest --rerun-tasks` executou **171 testes, zero falhas/erros**. Outra execução dos testes originais ocorreu ao retirar os probes da compilação. Build final após remover a simulação passou; nessa última chamada os 171 testes foram reutilizados (`UP-TO-DATE`). |
| Probes adicionais | Quatro testes externos em `build/dec047/probes`, anexados só por init script temporário: dois nomes existentes/zero gravações antes de confirmar/gravação única por rascunho; dispensar e pedir o aviso novamente; confirmar ainda valida rascunho que ficou incompleto; adiado de quatro dias atrás retorna `agora + 5 s`. **4/4 passam**, fora da contagem oficial de 171 e sem integração ao app. |
| Parada forçada | `FIRE` real → Adiar → `am force-stop` → reabrir. Cartão preservado, preferências iguais, exatamente um `FIRE_SNOOZED` ativo com o mesmo instante antes/depois. O dump de alarmes também contém histórico de cancelamentos; ele não foi contado como alarme ativo. |
| Reinício real | `adb -s emulator-5554 reboot` com adiado futuro. Após concluir o boot, cartão preservado e um `FIRE_SNOOZED` no mesmo prazo (20:01:16.483). A abertura posterior manteve ambos. Não foi reinício físico do A31. |
| Vencido ao reabrir | Parada forçada → avançar para depois do prazo → reabrir: `app_iniciado` 20:02:20.260, `FIRE_SNOOZED` 20:02:26.319, cartão Agora. |
| Adiado antigo e revalidação | Fixture temporária a partir do backup: horário válido 6 adiado há quatro dias, horário 5 com medicamento desativado e horário inexistente 999. Ao abrir, só 6 emitiu `FIRE_SNOOZED` (19:58:10.350, após início 19:58:05.155). O 5 foi removido durante `syncAll`; o 999 não emitiu aviso. Esse caso usa estado preparado, não exclusão manual pela tela. |
| Nome próprio na edição | Salvar Losartana sem mudar o nome concluiu sem aviso de duplicidade. |
| Nome de outro na edição | Trocar Losartana por `METFORMINA` abriu o aviso; Voltar e conferir não gravou; salvar novamente repetiu o aviso; Salvar mesmo assim concluiu. |
| Voz, dois nomes repetidos | Entrada simulada completa de Losartana e Metformina → nomes → revisão → aviso com os dois nomes. Voltar e conferir e tentar de novo funcionaram. Cadastrar mesmo assim criou exatamente dois registros: banco passou de 2 para 4 medicamentos. |
| `FixDraft` | Corrigir Losartana → Concluir correção retornou à revisão da voz sem aviso de duplicidade e sem criar registro. O aviso ocorreu apenas ao salvar a revisão. |
| Cadastro novo no formulário | Um rascunho completo da voz → Prefiro digitar → três etapas → revisão: aviso de Losartana; voltar/repetir/confirmar funcionaram. A confirmação criou um único medicamento, banco de 4 para 5. O ramo é `FormMode.New(fromSession = true)`, não formulário vazio preenchido do zero. |
| Validação do horário | Hora 24, minuto 60 e hora vazia: mensagem junto aos campos e **ancestral clicável do OK desabilitado**; o nó de texto isolado continua `enabled=true` na árvore. Adicionar 08:00 a uma lista que já tinha 08:00 mostrou Este horário já foi adicionado e bloqueou OK. |
| Digitação e alternância | `1 → 18` e `2 → 21` sem salto automático. Campos → relógio → campos preservou 18:21 sem recriar a Activity. A substituição por toque e a rotação têm os S3 acima. |
| Regras do app | Diff sem dependência nova nem mudança de permissões. Só versionCode/versionName no Gradle. Formulário e voz continuam produzindo `DraftValidator.Result.Valid` antes do repositório; o aviso adia a gravação. Sem novo caminho até Room, Tomado/histórico de ingestão ou alteração de datas civis no diff. |

## Restauração e limitações

- Backup obtido antes dos testes: dois medicamentos (Metformina/Losartana), horários 5/08:00 e 6/19:30, alertas vazios.
  Banco e preferências restaurados: **os oito arquivos do backup comparados byte a byte antes de reabrir**. Após abrir,
  medicamentos e horários conferidos novamente. Registros de teste e fixture não permanecem no banco.
- Entrada temporária apenas em `SpeechInput.start()`, lendo texto local consumido uma vez; removida ao terminar.
  SHA-256 original/final de `SpeechInput.kt`:
  `77717AB1856873761D094BAC1292CBDFC7C6609A95425B3C84D1A23C70C9851B`.
  `git diff -- app` vazio; APK final recompilado sem a cadeia `dec047_speech.txt` nos DEX e reinstalado só no emulador.
- Fonte 1,0×, rotação automática e posição inicial, hora real/automática, microfone revogado e notificação permitida
  restaurados. Artefatos locais de conferência estão em `build/dec047/`, ignorado pelo Git; não integram o app.
- Houve ANR/travamentos na sessão inicial do emulador e uma falha nativa de memória do daemon Gradle ao tentar
  compilar a simulação; recuperados. Não atribuídos à DEC-047 sem reprodução causal. Computer Use recusou acesso à
  janela do emulador; a conferência usou ADB. Algumas tentativas do wrapper pediram download bloqueado pela rede;
  as chamadas padrão de build voltaram a funcionar. Esses eventos não são falhas dos 171 testes.
- Não verificados: TalkBack real, fala reconhecida por serviço real, TTS/som audíveis, aparelho físico, Doze e outras
  versões do Android. A corrida em toda ordem possível de boot/entrega não foi provada ausente; o RV-047-01 confirma
  uma ordem defeituosa na inicialização pelo alarme. Não inferir aceite do autor nem integração à monografia.

**Próximo passo sugerido:** autor avaliar os quatro reparos e solicitar implementação pontual, seguida de conferência
dos mesmos percursos, especialmente um único disparo do adiado com processo encerrado. Nenhuma decisão nova aceita.
