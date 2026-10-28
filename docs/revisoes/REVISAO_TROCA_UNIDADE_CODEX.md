# Revisão — troca de unidade da dose (DEC-037)

**Conferência posterior, 28/10/2026, Codex:** RV-UN-01 fechado em `be24b9d`; T08/T09 e ida/volta sem editar conferidos no emulador. Sem novo achado no diff. Ver `REVISAO_CORRECAO_TROCA_UNIDADE_CODEX.md`. O resultado aberto abaixo é histórico.

28/10/2026 — Codex (GPT-6). Partida: `main`/`277361e7341471d5e53fcae2128237c1d7f13032`; `git pull`: atualizado.
Alvos: implementação `a2680da` e roteiro/documentação `277361e`, desde `a1701d0`.

**Resultado: casos diretos da DEC-037 verificados; um S2 preexistente confirmado no fluxo alterado.**
Não foi identificada regressão nova atribuível a `a2680da` na cobertura executada. O S2 abaixo também está
presente na lógica de `a1701d0`; não deve ser atribuído como introduzido por este commit.

## Referências e escopo

- Proposta oficial atual reaberta pelo conector do Drive: [Genki_Proposta_Final.pdf](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view), especialmente §2.1 (prevenção de erros, confirmação e edição).
- [Planilha oficial, aba Requisitos](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit), intervalo `A1:H45`: RF01–RF03, RF17–RF18 e RNF01–RNF03 pertinentes ao formulário.
- `docs/ESPEC_ARQUITETURA.md`, `docs/REQUISITOS_MAPA.md`, DEC-036/037 e roteiro `docs/ROTEIRO_TESTE_DOSE_GRADES.md`.
- Inspeção de `DoseUnit`, `DoseInput`, testes, estado do formulário e diff documental. A mudança permanece no escopo; não altera banco, validador, lembretes, OCR ou serviços.
- Configurações locais de `.idea/` e `.claude/settings.local.json` já estavam presentes na partida e foram preservadas. O JDK 25 da IDE acompanha o daemon já configurado; o alvo Java do app continua definido no Gradle. Nenhum defeito confirmado nesses diffs locais.

## Achado aberto

| ID | Gravidade | Arquivo:linha | Problema | Correção sugerida |
| --- | --- | --- | --- | --- |
| RV-UN-01 | S2 — erro relevante, preexistente | `app/src/main/java/br/edu/ifrs/genki/ui/form/FormScreen.kt:289` (também linhas 277 e 300) | Ao editar a dose livre em **Outro**, `dose` muda, mas `amount` conserva a quantidade da unidade anterior. Ao sair de Outro, `choice.keep(amount)` pode recuperar esse valor antigo e gerar uma dose que não corresponde mais ao texto atual. | Invalidar a quantidade anterior quando o texto livre for editado; ao escolher uma unidade estruturada depois disso, exigir nova quantidade. Cobrir a transição completa, além do teste isolado de `keep`. |

Reprodução no APK sem alterações, Pixel_6_2 / API 35:

1. Formulário novo, nome `Teste`; Comprimido → 2 → Cápsula: 2 continua, como previsto.
2. Outro: campo recebe `2 cápsulas`.
3. Substituir o campo por `1 colher de sopa` e fechar o teclado.
4. Tocar Cápsula: **2 fica marcado**, embora o texto atual não contenha essa quantidade/unidade.
5. Tocar Outro novamente: campo mostra **`2 cápsulas`**, confirmando que o texto da dose também mudou.
6. Reprodução adicional com nome preenchido: depois da recuperação indevida de 2, Continuar chega à etapa 2; não há bloqueio que exija escolher novamente a quantidade. Nada foi salvo.

O resultado depende do caminho: iniciar em Outro e digitar o mesmo texto deixa `amount` vazio; passar primeiro
por uma quantidade estruturada pode recuperar o valor antigo. A comparação com `git show a1701d0:.../FormScreen.kt`
confirma o mesmo estado separado e a mesma leitura da quantidade antiga antes de DEC-037. O roteiro novo não cobre
a edição de Outro seguida da volta a uma unidade estruturada; acrescentar esse caso ao reparar o achado é sugestão,
sem nova decisão de produto aceita nesta sessão.

## Verificação

- `.\gradlew.bat assembleDebug test`: passou; testes inicialmente `UP-TO-DATE`.
- `.\gradlew.bat testDebugUnitTest --rerun-tasks`: passou, **128 testes executados, zero falhas/erros** nos XMLs. Aviso de compilação preexistente em `VoiceParser.kt:357` (condição sempre falsa), fora do diff revisado.
- APK instalado com `adb -s emulator-5554 install -r`: Success. Nenhuma instalação no A31.
- No emulador: T01 (2 comprimidos → 2 cápsulas), T02 (Meio → Cápsula, sem quantidade; Continuar bloqueado com mensagem), T03 (20 gotas → ml, sem quantidade), T04 (5 ml → 5 gotas), T05 (Outra/4 → Cápsula, sem quantidade/campo), T06 (tocar Comprimido novamente mantém Outra/4) e transferência de dose para Outro conferidos.
- Regressão curta: abertura da Hoje, escolha Digitar, Cancelar com confirmação e descarte, lista Medicamentos com Metformina/Losartana anteriores, Ajustes e volta à Hoje. Nenhum crash AndroidRuntime observado no log consultado.
- Evidências locais ignoradas pelo Git: `build/dec037_*.xml` e `build/dec037_stale_amount.png`; especialmente `other_edited`, `stale_amount`, `stale_dose`, `stale_confirmed`, `stale_step2`, `half_error2`, `four_same` e `four_capsule`.

## Limites e estado final

Sem alteração de código ou configuração pelo revisor; nenhum teste/instrumentação temporários adicionados.
Rascunho descartado, nenhum medicamento salvo/editado/excluído; fonte, mídia, relógio e permissões não alterados.
Emulador deixado na Hoje. A31 não operado. Sem commit ou push.

Não verificados nesta rodada: aceite do autor no A31, edição de medicamento persistido, fonte 2,0×, TalkBack,
voz/TTS audíveis, câmera/OCR em tela e ciclo temporal de lembretes. A suíte unitária cobre a lógica existente;
a regressão visual desta rodada é restrita ao formulário e às abas. Próximo passo: reparo pontual de RV-UN-01
pelo implementador e conferência, mantendo separado o aceite manual do autor.
