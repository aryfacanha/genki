# Conferência — correção de RV-UN-01

28/10/2026 — Codex (GPT-6). Partida e alvo: `main`/`be24b9dcf47545a7d3d2e77841e833112021d8b4`.
`git pull`: atualizado. Revisados o reparo de `DoseInput`, os títulos de quantidade e os ajustes do roteiro
em `d3e09ef`/`be24b9d`, à luz da DEC-036/037 e do relatório anterior.

**RV-UN-01 fechado na cobertura executada. Nenhum novo achado S1–S4 confirmado no diff.**

## Escopo e achados

| ID | Gravidade anterior | Arquivo:linha | Resultado | Ação |
| --- | --- | --- | --- | --- |
| RV-UN-01 | S2 | `app/src/main/java/br/edu/ifrs/genki/ui/form/FormScreen.kt:301` | Fechado: editar ou apagar o texto de Outro zera `amount`; escolher unidade estruturada exige nova quantidade. A volta sem editar mantém a quantidade anterior. | Nenhum reparo adicional indicado nesta conferência. |

O reparo se limita ao estado de interface. Não acrescenta caminho de gravação, dependência ou regra clínica;
`DraftValidator`, concentração, OCR, datas e lembretes não mudaram. Títulos agora concordam com cápsulas/gotas.
Proposta oficial (§2.1) e planilha oficial (`Requisitos!A1:F5`, RF01–RF03) reabertas pelo conector do Drive;
ESPEC e handoff consultados. Configurações locais preexistentes de `.idea/` e `.claude/` preservadas.

## Verificação

- `.\gradlew.bat assembleDebug test`: **passou**. XMLs: **128 testes, zero falhas/erros**, com resultados reutilizados (`UP-TO-DATE`); não foi execução nova da suíte nesta rodada.
- APK do código revisado instalado somente no emulador com `adb -s emulator-5554 install -r`: Success. Pixel_6_2, API 35.
- **T08:** Comprimido/2 → Cápsula → Outro → substituir por `1 colher de sopa` → fechar teclado → Cápsula: só a unidade marcada, nenhuma quantidade. Continuar permanece na etapa 1 com `Informe quanto você toma por vez.`
- **T09:** Comprimido/2 → Outro → apagar todo o texto → Comprimido: nenhuma quantidade marcada; Continuar também exige quantidade.
- **Preservação:** Comprimido/2 → Cápsula mantém 2 (T01); Outro recebe `2 cápsulas`; voltar à Cápsula sem editar continua com 2. A edição do texto, e não a mera passagem por Outro, invalida a quantidade.
- **Títulos:** `Quantos comprimidos`, `Quantas cápsulas`, `Quantas gotas` e `Quantos ml` conferidos na interface.
- **Regressão curta:** depois do bloqueio, escolher quantidade válida permite etapa 2; Cancelar confirma descarte. Hoje, Medicamentos e Ajustes abrem; Metformina/Losartana anteriores continuam na lista. Nenhum crash AndroidRuntime observado no log consultado.
- Evidências locais ignoradas pelo Git: `build/unfix_*.xml`, especialmente `other_edited`, `T08`, `T08_error`, `T09_empty`, `T09`, `T09_error`, `roundtrip`, `valid_step2` e `medications_final`.

## Limites e estado final

Nenhum código/configuração alterado pelo revisor; nenhum teste ou instrumento temporário criado.
Sem medicamento salvo/editado/excluído; rascunho descartado, emulador deixado na Hoje. Fonte, mídia, permissões
e relógio não alterados. A31 não conectado nesta rodada e não operado; instalação às 11:09 é registro do Claude.
Sem commit ou push.

Não repetidos: edição de registro persistido, fonte 2,0×, TalkBack, voz/TTS audíveis, câmera/OCR em tela,
ciclo temporal de lembretes ou todas as demais trocas de unidade. Não há teste automatizado da transição Compose;
a reprodução manual acima complementa a suíte de lógica existente, conforme ESPEC/S5. O fechamento técnico do
achado não equivale ao aceite do autor no A31. Próximo: teste manual do autor pelo roteiro e verificações técnicas já previstas.
