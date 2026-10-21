# Genki — mapa de requisitos

O **texto oficial** dos requisitos está somente na planilha do Drive (ver `docs/00_INDICE.md`).
Este arquivo guarda (1) alterações aceitas ainda não aplicadas na planilha e (2) em que marco cada requisito é atendido.

## 1. Alterações aceitas pendentes na planilha (conferido em 21/10/2026)

| ID | Alteração | DEC |
| --- | --- | --- |
| RF03 | Texto: "O cadastro deve permitir informar nome, concentração (opcional), dose/quantidade por uso e programação de uso do medicamento." | DEC-002 |
| RF08 | Observações: "Duração informada (ex.: 'por 7 dias') é convertida em data final e apresentada ao usuário na confirmação." | DEC-003 |
| RF24 | Acrescentar ao texto: "O reconhecimento do lembrete não deve ser registrado nem apresentado como confirmação da ingestão." | DEC-004 |
| RNF11 | Nova linha: "O MVP não deve exigir conta/autenticação, servidor de aplicação ou sincronização em nuvem, nem exibir anúncios." — RNF, Alta, Definido | DEC-005 |

Quando forem aplicadas, apague esta seção e registre em `docs/ENTREGAS.md`.

## 2. Requisito → marco → verificação

| Requisito | Resumo | Marco | Como verificar |
| --- | --- | --- | --- |
| RF01, RF03 | Cadastro manual com nome, concentração, dose, programação | M1 | Aparelho |
| RF02 | Consultar, editar, excluir | M1 | Aparelho |
| RF04, RF05 | Horários; dias | M1 | Teste do validador + aparelho |
| RF06 | "Duas vezes ao dia", "a cada X horas" → horários | M3 | Teste do parser |
| RF07, RF08 | Tipo de tratamento; datas/duração | M1 (manual), M3 (voz) | Teste do validador |
| RF09 | Lembretes locais; param após a data final | M2 | Teste do `OccurrenceCalculator` + aparelho |
| RF10 | Ativar/desativar/modificar lembretes | M2 | Aparelho |
| RF11–RF16 | Voz: reconhecimento, fala única/progressiva, contexto, vários medicamentos, faltas/ambiguidade, correção | M3 | Corpus no teste do parser + aparelho |
| RF17 | Revisão antes de salvar | M1 (manual), M3, M4 | Aparelho |
| RF18 | Alternativa por toque/texto | M1, M3 | Aparelho |
| RF19 | Retorno visual e auditivo (TTS) | M3 | Aparelho |
| RF20–RF23 | Câmera, OCR, revisão, sem inferir dose/horários | M4 | Teste do `OcrParser` + aparelho |
| RF24 | Repetição até reconhecer (não é ingestão) | M2 | Aparelho |
| RF25 | Adiar lembrete | M2 | Aparelho |
| RNF01–RNF04 | Simplicidade, legibilidade, voz não obrigatória, estado perceptível | Todos | Revisão heurística (MET-3) |
| RNF05 | Nada reconhecido salvo sem confirmação | M3, M4 | Aparelho |
| RNF06 | Dados locais | M1 | Aparelho (fechar e reabrir) |
| RNF07 | Sem gravar áudio | M3 | Inspeção do código |
| RNF08 | Resposta rápida da voz | M3 | MET-1 (limite numérico definido após a PoC, conforme planilha) |
| RNF09 | Falha parcial não apaga dados válidos | M3, M4 | Aparelho |
| RNF10 | Processamento local; on-device priorizado | M3 | Inspeção do código + MET-1 |
| RNF11 | Sem conta, servidor, nuvem, anúncios | Todos | Inspeção |
