---
name: revisar
description: Revisão independente do último commit (ou do intervalo indicado) contra a ESPEC, o AGENTS.md e o protótipo. Use só quando o autor pedir /revisar.
disable-model-invocation: true
---

# Revisar

Alvo: $ARGUMENTS (padrão: `git show HEAD`).

1. Leia o diff do alvo, docs/ESPEC_ARQUITETURA.md (seções pertinentes) e as regras do AGENTS.md.
2. Rode `.\gradlew.bat assembleDebug test`.
3. Verifique, no mínimo:
   - escopo: só o RF/marco pedido; nada de Hilt, WorkManager, DataStore, rede, LLM;
   - toda gravação passa pelo DraftValidator;
   - concentração ≠ dose; OCR não preenche dose/horários/dias/tratamento;
   - nada de "Tomado" nem histórico de ingestão;
   - datas civis no fuso do aparelho;
   - textos e fluxo coerentes com o protótipo (D:\dev\genki-prototype, se disponível);
   - testes unitários da lógica pura existem e passam.
4. Liste achados em tabela: ID, gravidade (S1 bloqueia · S2 erro relevante · S3 menor · S4 sugestão),
   arquivo:linha, problema, correção sugerida.
5. NÃO altere código. Diga o que não pôde verificar (aparelho, voz etc.).
