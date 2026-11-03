# Conferência das correções da Hoje — Codex

27/10/2026 — Codex (GPT-6). Partida e alvo: `main`/`0b65a53c439c99cc6ee3a0bb57584d0e85f0f3b1`.
`git pull`: já atualizado. Diff: `fa1fbbd..0b65a53`.
Roteiro: `docs/HANDOFF_APP.md`, §6 e casos da Hoje em §7, com checagem do emulador conforme §5.
Referência: `REVISAO_HOJE_CODEX.md`; DEC-034/035, ESPEC §5/6, RF09/RF24 e RNF02.
Proposta oficial e planilha atuais reabertas no Drive; sem mudança de decisão ou de escopo do produto.

**Veredito: RV-HOJE-01/02/03 fechados; correções aprovadas na cobertura executada.**
Nenhum achado novo S1–S4 no escopo desta conferência. Aceite completo no aparelho permanece separado.

| Item | Resultado | Evidência e localização |
| --- | --- | --- |
| RV-HOJE-01, S2 | **Fechado** | `Notifications.kt:32–55` combina estado geral, permissão e bloqueio do canal; `settingsIntent` escolhe categoria ou app. No emulador API 35, desligar só “Lembretes de medicamentos” faz aparecer a faixa; Resolver em Ajustes → Permitir notificações abre a categoria bloqueada. Religá-la faz Ajustes indicar Permitidas e retira a faixa na Hoje. Bloqueio geral por revogação de POST_NOTIFICATIONS também mostra a faixa; o botão solicita a permissão, e Allow restaura o estado. Nenhum diálogo indevido de permissão ao resolver apenas o canal. |
| RV-HOJE-02, S3 | **Fechado** | `HomeScreen.kt:242` permite `firstAhead == 0`. Às 07:00, Agora fica antes de 08:00; à hora real (17:32/17:34), entre 08:00 e 19:30; às 21:00, depois de 19:30. Nas três listas há um marcador. Às 21:00 o próximo lembrete aparece como Amanhã / 08:00 / Metformina. |
| RV-HOJE-03, S3 | **Fechado** | `HomeScreen.kt:513–521` coloca Passou sob o horário. Com fonte 2,0× no Pixel_6_2, Metformina e 1 comprimido ficam inteiros, cada um em sua linha. Após rolar, Losartana e sua concentração/dose também são legíveis; a quebra da dose ocorre entre palavras. Marcador final e controles permanecem alcançáveis. Sem reduzir a fonte do conteúdo. |

## Execução e limites

- **Build e testes: passou.** `.\gradlew.bat assembleDebug test`: BUILD SUCCESSFUL, tarefas
  `UP-TO-DATE`; XMLs conferidos: **116 testes, zero falhas e zero erros**. Não foram acrescentados
  testes: as correções alteram consulta de APIs Android e composição visual, sem lógica pura nova.
- **Instalado em:** somente `emulator-5554`, Pixel_6_2, API 35, APK original de `0b65a53`, via
  `adb install -r`. A31 não operado.
- **Capturas versus esperado:** faixa para canal bloqueado, destino correto no sistema, marcador
  antes/entre/depois dos horários e texto ampliado correspondem ao reparo esperado. Evidências
  locais não versionadas: `build/hoje_fix_initial.png`, `hoje_fix_channel_warning.png`,
  `hoje_fix_channel_destination.png`, `hoje_fix_channel_restored.png`, `hoje_fix_before_first.png`,
  `hoje_fix_after_last.png`, `hoje_fix_font20.png`, `hoje_fix_font20_rows.png` e `hoje_fix_final.png`
  (todos no diretório `build/`).
- **Regressão rápida:** abas Hoje/Medicamentos/Ajustes trocam; os dois medicamentos existentes
  permanecem. Toque na linha passada abre Metformina; Voltar para Hoje retorna à origem. A lista
  continua distinguindo passado e futuro; o próximo dia não desapareceu. Inspeção do diff confirma
  que não há escrita de medicamento, alteração de validador, dose/concentração, OCR, ingestão,
  calendário civil, agendamento, configuração ou dependência.
- **Erros no Logcat:** nenhum erro do PID atual do app e nenhum crash AndroidRuntime desde a
  limpeza inicial do log. Emulador permaneceu conectado nesta rodada.
- **Restauração:** notificações concedidas, canal `lembretes` com importância 4, alarmes exatos
  permitidos, hora real com `auto_time=1`, fonte 1,0× e `genki_alerts` vazio. Nenhum medicamento
  criado, editado ou excluído. Reabertura do app reagenda os alarmes futuros.
- **Não verificado nesta rodada:** A31, TalkBack audível, voz/TTS reais, câmera, Doze e ciclo real
  de disparo/repetição/adiamento. CRUD completo e fluxos M3/M4 não repetidos; testes unitários e
  inspeção cobrem a base inalterada. Os resultados anteriores continuam históricos, sem afirmar
  execução nova deles. O aceite manual completo no aparelho continua pendente.

Sem alteração de código ou configuração pelo revisor, commit ou push. Alterações preexistentes em
`.idea/` preservadas. Próximo passo: continuar o aceite manual no A31 e as verificações técnicas previstas;
não é necessário outro reparo dos três achados na cobertura desta conferência.
