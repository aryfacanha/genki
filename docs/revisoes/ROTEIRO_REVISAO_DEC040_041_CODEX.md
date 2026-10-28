# Roteiro de revisão para o Codex — ajustes da bateria (DEC-040) e introdução (DEC-041)

Preparado em 28/10/2026 por Claude Code (Opus 5.5), a pedido do autor. Siga o `HANDOFF_APP.md` §6; quem revisa não
altera código.

## Alvo

`main`, intervalo **`9ee4def..a030860`** (código em três commits):

| Commit | Pacote | O que mudou | Onde |
| --- | --- | --- | --- |
| `352527e` | DEC-040 parte 1 | Avisos de lista vazia na mesma altura; cartão "Lembrete adiado" verde-claro com borda; X com confirmação nos cartões "Entendi até agora" da voz; "Escolher a quantidade" na pergunta da dose (menu + − número +); "use contínuo" | `Components.EmptyMessage`, `HomeScreen` (`AlertColors`, `AlertSurface`, `AcknowledgeButton`), `MedicationsScreen`, `FormScreen.DoseInput` (internal, `title`), `VoiceScreen` (`SoFarCard`, `DosePicker`), `VoiceViewModel.chooseDose`, `VoiceParser` |
| `eb67d7d` | DEC-040 parte 2 | Microfone abre sozinho ao fim da pergunta falada; para no silêncio; "Não entendi" ouve de novo uma vez só; toque em resposta/Escolher/Corrigir cancela a escuta; confirmação falada "Anotei …"; horários do dia ajustáveis na pergunta do primeiro horário (E12) | `Speaker.speak(text, onDone)`, `SpeechInput.cancel`, `VoiceQuestions.confirmation`, `VoiceViewModel` (`Prompt.listen`, `misses`), `VoiceScreen` (efeito do prompt, `listenNow`, `onQuiet`, `TimesPicker`) |
| `a030860` | DEC-041 | Introdução de 4 telas na primeira abertura; som como alarme (canal novo `lembretes_alarme`); escuta automática desligável; Ajustes com essas opções e "Ver a introdução de novo" | `ui/intro/IntroScreen.kt`, `ui/settings/SettingsControls.kt`, `SettingsScreen`, `GenkiApp` (`IntroRoute`), `Settings`, `Notifications` |

Resultado da bateria do autor que motivou os pedidos: `docs/RESULTADO_BATERIA_COMPLETA_A31.md`. Proposta aprovada:
`docs/PROPOSTA_INTRODUCAO.md`. Decisões DEC-040 e DEC-041.

## Execução

1. `git pull`; registrar a partida. `.\gradlew.bat assembleDebug test`: esperado **137 testes, 0 falhas**.
2. Escopo e regras: gravação só pelo `DraftValidator` (`chooseDose` só altera o rascunho); concentração ≠ dose; nada de
   "Tomado"; sem dependência nova; o canal novo não é permissão nova (manifest inalterado). A introdução está fora da
   proposta e registrada como tal.
3. Pontos para olhar com atenção:
   - **Escuta automática:** corrida entre o fim da fala (`UtteranceProgressListener`, só a última fala conta) e toques do
     usuário; a escuta não pode abrir fora da pergunta, na correção, sem permissão, com a tela em segundo plano ou com
     `autoListen` desligado; nada de ciclo de escutas por ruído ou silêncio. Leitor sem voz em português: fim imediato.
   - **Confirmação falada:** textos de `VoiceQuestions.confirmation` para fala inicial completa, respostas, correção e
     dois medicamentos; nada falado quando nada mudou.
   - **E12:** com os horários do dia completos, o parser mantém a lista escolhida (`times.size >= perDay`); troca que
     repete um horário é bloqueada pelo relógio.
   - **X na voz:** tirar o único medicamento volta ao início; tirar o da pergunta atual passa para o próximo.
   - **Canal de alarme:** `canNotify`, o aviso de notificações bloqueadas na Hoje e o atalho do sistema usam o canal em
     uso; trocar o som depois de lembretes já agendados.
   - **Introdução:** só na primeira abertura (também aparece uma vez para quem já tinha o app); Pular, Voltar entre
     páginas, permissão negada, reabertura, 2,0×.
   - **Hoje:** aviso vazio na frente da rolagem (leitor de tela), cartão adiado com contraste (tabela em
     `AVALIACAO_CONTRASTE_WCAG.md`).
4. Emulador (§5), sem mexer em código; fala injetada em `SpeechInput.start()` só durante a execução, removida depois.
   Casos mínimos da bateria: A0, A3, D4, D11 (sem precisar ouvir o som: conferir o canal da notificação), E1, E12,
   E14–E20, G1–G7; fonte 1,0× e 2,0× na introdução e na voz.

## Relatório

`docs/revisoes/REVISAO_DEC040_041_CODEX.md`, com achados S1–S4 (ID, gravidade, arquivo:linha, problema, correção
sugerida) e o que não foi verificado. Atualizar `HANDOFF_APP.md` (§2, §3, §7 e Diário), `ESTADO.md`, `ENTREGAS.md` e
`REGISTRO_IA.md`. Não alterar código; não fazer commit nem push; não operar o A31.

Execução: `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < docs/revisoes/ROTEIRO_REVISAO_DEC040_041_CODEX.md`.
