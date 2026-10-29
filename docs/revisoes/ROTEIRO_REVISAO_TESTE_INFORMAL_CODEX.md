# Roteiro de revisão para o Codex — APK de teste informal e retornos (DEC-042 a DEC-046)

Preparado em 29/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Siga o `HANDOFF_APP.md` §5 e §6; quem revisa
não altera código.

## Alvo

`main`, intervalo **`0930dfd..c52338c`** (tudo depois da conferência RV-4041). O app foi distribuído como APK release a
uma pessoa de confiança para um teste informal **fora da pesquisa**; os retornos do autor e desse teste viraram as
mudanças abaixo.

| Commit | Pacote | O que mudou | Onde |
| --- | --- | --- | --- |
| `63e2cc7` | Release | Assinatura do release lida de `local.properties` (chave fora do Git); ícone adaptativo novo com monocromático | `app/build.gradle.kts`, `res/drawable/ic_launcher_*`, `res/mipmap-anydpi` |
| `332b453` | Release 1.0.1 | R8 desligado no release: a otimização parcial do template (`packageScope`) quebrava a abertura com `IllegalAccessError` no `ProcessLifecycleInitializer` | `app/build.gradle.kts` |
| `54be0ae` | DEC-042 | Banco preservado nas atualizações: sem `fallbackToDestructiveMigration`, esquema exportado, migrações explícitas | `data/GenkiDatabase.kt`, `data/Migrations.kt`, `app/schemas/.../1.json`, `MigrationsTest` |
| `5b812b9` | DEC-043, 1.0.2 | Hoje: próximo lembrete com todos os medicamentos do horário e "Ver mais"; "Cancelar" na escolha, câmera e voz; escuta que junta trechos depois de pausa; introdução enxuta | `HomeScreen`, `ActiveAlert.nextGroup`, `OccurrenceCalculator.firstGroupAfter`, `SpeechInput`, `VoiceScreen`, `CameraScreen`, `AddChoiceScreen`, `IntroScreen`, `SettingsScreen` |
| `8bf09a2` | 1.0.3 | Pausa da escuta de 3 s para 1,5 s | `SpeechInput.PAUSE_MS` |
| `c6ba1e4` | DEC-044, 1.0.4 | Tutorial guiado sobre as telas reais (camada escura com recorte, balão, 7 passos, vai à tela Adicionar e volta); introdução com 2 páginas; "Ver o tutorial" em Ajustes | `ui/tutorial/Tutorial.kt`, `GenkiApp`, `HomeScreen`, `AddChoiceScreen`, `IntroScreen`, `SettingsScreen`, `TutorialStepsTest` |
| `387c308` | DEC-045, 1.0.5 | "Próximo" do tutorial não passava com dedo real (a camada consumia os movimentos); pergunta "Quer ver um tutorial rápido?" na tela e falada antes do tutorial | `Tutorial.kt` (`blockTouches`, `Offer`), `GenkiApp` |
| `c52338c` | DEC-046, 1.0.6 | Voz em etapas: "Confira os remédios" (lista de nomes) antes de doses e horários, com correção por toque (trocar, X, Separar) e por voz ("remover X", "trocar X por Y", "não é X, é Y", "separar X", nome novo, "pronto") | `voice/NameCommands.kt`, `VoiceViewModel` (fase `NAMES`, gravação por função), `VoiceScreen` (`Names`, `NameCard`), `NameCommandsTest`, `VoiceNamesFlowTest`, ESPEC §7 |

Decisões: DEC-042 a DEC-046 em `docs/DECISOES.md`. Introdução, tutorial e pergunta estão **fora da proposta** e
registrados como acréscimo.

## Execução

1. `git pull`; registrar a partida. `.\gradlew.bat assembleDebug test`: esperado **158 testes, 0 falhas**.
   `.\gradlew.bat assembleRelease` deve compilar (sem a chave no `local.properties`, o APK sai sem assinatura: esperado).
2. Escopo e regras: gravação só pelo `DraftValidator` (a conferência dos nomes só altera rascunhos da sessão);
   concentração ≠ dose; nada de "Tomado"; sem dependência nova; manifest sem permissão nova; RNF07 (nenhum áudio
   gravado) mantido na escuta por trechos.
3. Pontos para olhar com atenção:
   - **Release e banco:** R8 desligado não deixa regra órfã; `versionCode` cresce a cada versão (1 → 7). DEC-042: sem
     mudança de entidade, a versão continua 1 e o hash de identidade do Room é o mesmo, para instalar por cima sem perder
     dados; `MigrationsTest` falha se a versão subir sem migração ou sem esquema exportado.
   - **Hoje (DEC-043):** `nextGroup` e `firstGroupAfter` com lembretes desligados, tratamento terminado, avisos tocando
     ou adiados no mesmo horário; "Ver mais" guarda o estado ao girar ou voltar; linha "Depois: … e mais N".
   - **Escuta por trechos (`SpeechInput`):** `stop` durante a espera entre trechos entrega na hora; `cancel` e `destroy`
     limpam o `Handler`; erro na continuação entrega o que já tinha; troca para o reconhecedor do sistema (sem português
     no aparelho) não perde os trechos; latência da DEC-015 medida só no último trecho; `MAX_PARTS` impede ciclo com ruído.
   - **Tutorial (DEC-044/045):** `blockTouches` bloqueia a tela de baixo sem consumir (toque com deslize no "Próximo"
     avança; toque na área escura não aciona nada); ida e volta da tela Adicionar (`tutorialOpenedAdd`), também com
     Voltar do sistema no meio e "Pular" na tela Adicionar; alvo ausente (Hoje sem medicamentos ou com aviso tocando)
     deixa o balão no centro sem recorte; toque na notificação do lembrete durante o tutorial; leitor de tela só no
     balão (`clearAndSetSemantics`); `Speaker` da pergunta encerrado ao fechar; balão com fonte 2,0×.
   - **Conferência dos nomes (DEC-046):** falsos positivos de `NameCommands.parse` (palavras de confirmação no começo de
     uma frase com outro conteúdo; "e" de "não é X e Y"); `similar` com nomes próximos mas diferentes (ex.: Prednisona ×
     Prednisolona, Losartana × Lorsartana) pode tirar ou trocar o remédio errado; "Separar" não oferecido em nome com
     "de"; separar ou trocar copia dose, dias e horários para os novos; escuta automática só nas fases `NAMES` e `ASKING`;
     Voltar com `renaming` aberto; "Prefiro digitar" não aparece na fase `NAMES` (avaliar se falta); câmera → voz pula a
     conferência.
4. Emulador (§5), sem mexer em código de forma permanente. Para a voz, injetar a fala em `SpeechInput.start()` só
   durante a execução e remover depois (como nas revisões anteriores). Para o tutorial, imitar o dedo com
   `adb shell input swipe x y x+6 y+4 120`, porque `input tap` não reproduz a falha do DEC-045. Para a primeira abertura
   no debug: `run-as br.edu.ifrs.genki` e `intro_done` = `false` em `shared_prefs/genki_settings.xml`; restaurar depois.
   Casos mínimos:
   - V1: "rivaroxabana metoprolol e simvastatina" → lista com 2 → Separar → trocar por voz → remover por voz → incluir
     outro → "pronto" → perguntas de dose.
   - V2: fala completa de um remédio → lista com 1 → Continuar → revisão sem perguntas.
   - V3: tocar no nome, digitar "A, B" → dois remédios com as mesmas informações.
   - T1: tutorial por Ajustes, 7 passos com toque com deslize, Voltar no passo 3, Pular no passo 4.
   - T2: primeira abertura → pergunta → "Agora não"; repetir com "Ver tutorial".
   - H1: três medicamentos no mesmo horário → "Ver mais"/"Ver menos" (inserir no banco do emulador e restaurar depois).
   - Regressão da §7 para M2 (lembrete real no canal em uso) e para a voz (X, correção por voz, dose, relógio).
   Fonte 1,0× e 2,0× na conferência dos nomes e no balão do tutorial.

## Relatório

`docs/revisoes/REVISAO_TESTE_INFORMAL_CODEX.md`, com achados S1–S4 (ID, gravidade, arquivo:linha, problema, correção
sugerida) e o que não foi verificado. Atualizar `HANDOFF_APP.md` (§2, §3, §7 e Diário), `ESTADO.md`, `ENTREGAS.md` e
`REGISTRO_IA.md`. Não alterar código; não fazer commit nem push; não operar o A31; não usar a chave de assinatura.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_TESTE_INFORMAL_CODEX.md`.
