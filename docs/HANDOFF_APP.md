# HANDOFF — App Android Genki (Claude Code ↔ Codex)

Atualizado em: 26/10/2026, por Claude Code (Opus 5.5) — criação deste arquivo (DEC-025).
Quem assumir: leia `AGENTS.md`, `docs/ESTADO.md` e este arquivo, confira o estado real (`git log`, build) e não confie só
neste texto. Ao terminar cada alteração, **atualize as seções 2, 3 e 7 (se mudaram) e acrescente uma linha no Diário**.
Este arquivo é o retrato técnico do código; o estado do projeto inteiro (prazos, CEP, monografia) fica no `ESTADO.md`.

## 1. O que é

App Android do Genki (Kotlin + Compose + Room), especificado em `docs/ESPEC_ARQUITETURA.md`.
Referência visual: protótipo web `D:\dev\genki-prototype` (github.com/aryfacanha/genki-prototype, `docs/HANDOFF.md`).
Divergências de dados/regras entre os dois: `docs/DIVERGENCIAS_PROTOTIPO.md` (até o autor decidir, vale a ESPEC — DEC-022).

## 2. Estado por marco

| Marco | Situação | Commits |
| --- | --- | --- |
| M0 Base | Implementado, compilado e aberto no emulador (Pixel_6_2, API 35) em 26/10: regressão da seção 7 passa. Tema e barra inferior alinhados ao protótipo `295212e`. Aceite manual do autor no aparelho (ESPEC §9) pendente | `c1f2b7e`, `cec21ab`, `53fe97a`, correção do rótulo (ver Diário) |
| M1 CRUD | Não iniciado. Antes: decisões #2, #7, #12 de `DIVERGENCIAS_PROTOTIPO.md` | — |
| M2 Lembretes | Não iniciado. Antes: decisões #13, #14, #16 | — |
| M3 Voz | Não iniciado | — |
| M4 Câmera | Não iniciado | — |

## 3. Verificado e não verificado

| Item | Situação |
| --- | --- |
| `.\gradlew.bat assembleDebug test` | Passa em `53fe97a` (Claude Code, 26/10) |
| Testes unitários | `GenkiEvalTest` (2) |
| App aberto em emulador/aparelho | Emulador Pixel_6_2 (API 35), 26/10, Claude Code. Aparelho físico: não |
| Abas, cores, barra inferior | Verificados no emulador (capturas): abre em Hoje, três abas trocam, indicador verde-claro e negrito na ativa, sem roxo |
| Rótulo da barra com fonte ampliada | Inteiro até 1,5×; **a partir de 1,8× "Medicamentos" é cortado com reticências** (limitação: a largura da aba não comporta a palavra; o rótulo encolhe até 12 sp e depois corta) |
| `GenkiEval` no Logcat | Verificado no emulador (`app_iniciado`); nenhum erro do processo do app |
| Revisão independente (Codex) | Nenhuma ainda |

## 4. Como rodar

- Build e testes (PowerShell, na raiz): `.\gradlew.bat assembleDebug test`.
- Instalar: `.\gradlew.bat installDebug` com um aparelho em `adb devices`.
- Versões: AGP 9.4.1 (Kotlin 2.2.10 embutido), Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26 (DEC-011).
- Codex CLI (forma que funcionou nas revisões do protótipo): `codex.cmd exec -m gpt-6.1-sol -s workspace-write - < roteiro.md`
  (roteiro via stdin). Ajuste o modelo se o CLI recusar.

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

- **M0:** o app abre na aba Hoje; as três abas (Hoje, Medicamentos, Ajustes) trocam de tela; a aba ativa tem o fundo
  verde-claro atrás do ícone e o rótulo em negrito; nenhum texto roxo/lilás do Material padrão; build debug registra
  `GenkiEval` no Logcat (`adb logcat -d -s GenkiEval`).
- M1 em diante: acrescentar ao entregar cada marco.

## 8. Divisão de trabalho (DEC-025)

- **Claude Code implementa; Codex revisa** (mesma divisão do protótipo, decisão do autor de 21/10).
- **Modo só Codex:** quando o limite do Claude Code acabar, o Codex implementa seguindo este arquivo e escreve no Diário
  "modo só Codex — aguardando revisão". Ao voltar, o Claude Code revisa primeiro esse trabalho (seção 6) e só depois
  começa algo novo.
- Nunca os dois editando os mesmos arquivos ao mesmo tempo. Um marco (ou parte dele) por commit.
- Mudança de configuração (`.claude/`, skills, `AGENTS.md`, Gradle, versões, permissões do manifest) entra no Diário
  com o que mudou e por quê, para o outro assistente saber.
- Push só com autorização do autor.

## Diário (acrescente no topo; uma linha por alteração: data — agente — o que mudou — verificado — pendente)

- 2026-10-26 — Claude Code — `/testar-emulador` no Pixel_6_2 (API 35): regressão M0 passa; com fonte 1,3× "Medicamentos" era cortado na barra inferior → rótulo com `autoSize` (16 sp → mín. 12 sp) em `GenkiApp.kt`, a pedido do autor — build, testes e emulador em 1,0×/1,3×/1,5× OK — pendente: corte persiste a partir de 1,8×; TalkBack não testado.
- 2026-10-26 — Claude Code — cria este arquivo, a seção "Implementação e revisão" no `AGENTS.md` e `docs/revisoes/`; skills `/revisar` e `/testar-emulador` passam a apontar para as seções 5 e 6 (DEC-025) — só documentação — pendente: primeira revisão pelo Codex.
- 2026-10-26 — Claude Code — config: skills `/testar-emulador` e `/revisar` em `.claude/skills/` (texto do autor; só o Claude Code as lê) (`19fcb3f`) — não executadas — nenhuma.
- 2026-10-26 — Claude Code — tema e barra inferior alinhados ao protótipo (`53fe97a`); divergências em `docs/DIVERGENCIAS_PROTOTIPO.md` (`0f73cb0`) — build e testes passam — não visto no aparelho.
- 2026-10-26 — Claude Code — M0: estrutura base (`cec21ab`) sobre o template do autor (`c1f2b7e`) — build e testes passam — não aberto no aparelho.
