# Conferência da correção dos ajustes de voz/DEC-036 — Codex

27/10/2026 — Codex (GPT-6). Partida e alvo: `main`/`d4d63504afebf3b1443940fcab5addc126191b45`.
`git pull`: já atualizado. Diff: `1cc5f31..d4d6350`.
Roteiro: `HANDOFF_APP.md` §5/6/7, com escopo pontual nos dois achados da revisão anterior.
Consultados estado, decisões pertinentes, ESPEC §4/5/7, relatório anterior, proposta e planilha atuais no Drive.

**Veredito: RV-VZ-01/02 fechados; correções aprovadas na cobertura executada.**
Nenhum achado novo S1–S4 no escopo desta conferência. Aceite completo no aparelho continua separado.

| Item | Resultado | Evidência |
| --- | --- | --- |
| RV-VZ-01, S2 | **Fechado** | `FormViewModel.hasUnsavedData()` (`FormViewModel.kt:164`) considera os campos do cadastro novo, inclusive baseline recebido; Cancelar e voltar usam o método. No emulador: fala injetada “losartana 1 comprimido” → Prefiro digitar → formulário preenchido sem mudança local → Cancelar mostra “Descartar cadastro?”. Continuar editando mantém os campos. Voltar da barra e voltar do Android também confirmam; Descartar retorna à Hoje. Cadastro manual vazio sai sem diálogo; edição existente sem alterações volta aos detalhes sem diálogo indevido. |
| RV-VZ-02, S2 | **Fechado nos casos reproduzidos** | `merged()` (`VoiceParser.kt:239`) é usado no fechamento e na consolidação antes da próxima correção (`:295`). Os três casos antes falhos passam: “às 8 na verdade às 9 na verdade às 10” → só 10:00; segunda correção de dias → só 09:00; Losartana/Metformina com correções na mesma fala → 09:00/21:00, respectivamente. O teste permanente `severalCorrectionsInTheSameUtterance` cobre os três. |

- **Build e testes:** `.\gradlew.bat assembleDebug test` passou inicialmente (`UP-TO-DATE`) e
  passou novamente após retirar os temporários, com **126 testes existentes executados, zero
  falhas/erros**. Nove reprodutores adicionais da revisão anterior: **9/9 passam**; XML local
  `build/vz_closure_probe.xml`. Incluem correção simples, duas falas separadas, negação de horário
  conhecido, dia primeiro com ano explícito e dose/Outro, além dos três casos antes falhos.
- **Instalado em:** somente emulador Pixel_6_2, API 35 (`emulator-5554`). Para alcançar o fluxo,
  `SpeechInput.start` devolveu temporariamente a frase acima; não foi reconhecimento de áudio real.
  A instrumentação e o arquivo temporário de testes foram removidos; original recompilado e
  reinstalado, app aberto e `git diff -- app/src` vazio ao terminar.
- **Capturas versus esperado:** formulário recebido preservado, diálogos de Cancelar e voltar
  confirmados por árvore/captura; evidências locais `build/vz_closure_filled.png`,
  `build/vz_closure_cancel_dialog.png`, `build/vz_closure_back_dialog.png` e `build/vz_closure_final.png`.
- **Regressão rápida:** cadastro vazio, manutenção dos dados ao recusar descarte, descarte confirmado,
  edição sem alteração, detalhes/lista e abas conferidos. Os dois medicamentos originais permanecem;
  nenhum medicamento criado, editado ou excluído. O diff não muda validador, banco, OCR, ingestão,
  calendário, rede ou configuração/dependências do app.
- **Erros no Logcat:** nenhum crash AndroidRuntime observado; processo original final aberto e
  Hoje conferida. Fonte 1,0×, relógio automático e permissões mantidos; volume não alterado nesta rodada.
- **Não verificado:** A31, voz/TTS reais ou audíveis, TalkBack, câmera, fonte ampliada e ciclo
  temporal dos lembretes nesta conferência. A instalação no A31 consta do registro do Claude,
  sem verificação independente do Codex nesta rodada. Demais resultados anteriores são históricos.

Sem correção permanente de código/configuração, commit ou push. Alterações locais de `.idea/` e
`.claude/settings.local.json` preservadas. Próximo: continuar o aceite manual no A31 e as verificações
técnicas previstas, sem reabrir um ciclo amplo do interpretador.
