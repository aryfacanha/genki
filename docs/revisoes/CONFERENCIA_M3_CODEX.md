# Conferência de fechamento do M3 — Codex

27/10/2026 — Codex (GPT-6). Partida: `main`, `5e5abe2f503e55435a605b0bc2cf3cfe1ed97c12`;
`git pull`: atualizado. Alvo: `0cef3f16821a003f2f6ea174eb505032d31e7b6a`.
Escopo: `ROTEIRO_CONFERENCIA_M3_CODEX.md`, somente os dois achados restantes e o texto do cartão.
Consultados o relatório anterior, handoff, decisões pertinentes, ESPEC §7/9 e mapa de requisitos vigentes.

**Veredito: M3 pode ser dado como revisado na cobertura técnica executada.** Os dois achados restantes estão
fechados, sem achado novo no escopo desta conferência. O aceite completo no aparelho continua separado.

| Item | Resultado | Evidência |
| --- | --- | --- |
| RV-M3-02 (resto), S2 | **Fechado** | `losartana segunda e quarta na verdade sexta e metformina sábado` deixa Losartana só na sexta e Metformina no sábado. A lista para antes de “na verdade” (`VoiceParser.kt:508`); lista aditiva sem correção continua somando no teste permanente `correctionAfterDayListWithAnotherMedication`. |
| RV-M3E-01, S3 | **Fechado** | “o nome é atenolol” durante pergunta de dias troca o nome para Atenolol, sem criar outro medicamento. O retorno antecipado respeita `renameNext` (`VoiceParser.kt:600`); nome solto continua ignorado, conforme `nameIsDuringAnyQuestion`. Reprodutor anterior também passa. |
| Texto do grupo | **Corrigido por inspeção** | `HomeScreen.kt:280` mostra `${group.size} lembretes`, compatível com um aviso por horário, inclusive vários do mesmo medicamento. Tela não repetida nesta conferência. |

- `assembleDebug test` passou antes e depois da execução temporária; `testDebugUnitTest` executou nas duas vezes.
  XMLs finais: **112 testes, zero falhas/erros**.
- Reprodutores locais: quatro classes `Temporary*` e duas cópias históricas do parser, copiadas somente para a
  execução de `testDebugUnitTest --tests 'br.edu.ifrs.genki.voice.Temporary*'`: **43/43 passam** (7 + 14 + 15 + 7),
  incluindo probes observacionais. XMLs preservados em `build/conference_TEST-*.xml` (não versionados).
- Seis arquivos temporários removidos; `app/src` sem alterações ao terminar. Sem mudança permanente de código ou
  configuração, commit, push, instalação, operação no emulador ou no A31. Alterações preexistentes em `.idea/` preservadas.

Limites anteriores continuam documentados: “até 29/2” sem ano não busca o próximo bissexto; voz real/TTS audível,
fallback TTS, erro real do reconhecedor, TalkBack e aceite completo no A31 não foram conferidos nesta sessão.
A verificação opcional por tela foi dispensada; o caso de nome foi confirmado nos testes e no fluxo do parser.
Nenhuma decisão nova ou aceitação de limitação pelo autor é inferida desta conferência.
