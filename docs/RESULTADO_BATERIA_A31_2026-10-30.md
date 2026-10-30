# Resultado da bateria no A31 — 30/10/2026 (versão 1.0.12)

Executada pelo autor com `ROTEIRO_BATERIA_COMPLETA.md` (versão de 30/10). Seções A–F testadas; G, H, I e M não
testadas nesta rodada (o autor fará no congelamento). Inclui um retorno informal de terceiro sobre o texto de Ajustes.
Triagem por Claude Code (Opus 5.5). Categorias: **sugestão pendente** até o autor aceitar cada item.

| # | Retorno do autor | Triagem |
|---|---|---|
| 1 | (Informal) "Avisar de novo se eu não responder": a descrição confunde, por usar negativa e os termos "Adiar" e "Entendi" sem contexto | Texto. Reescrever sem negativa e sem os nomes dos botões |
| 2 | Opção nas telas iniciais, ligada por padrão, para os guias falados (tutorial e cadastro por voz); mudar em Ajustes; botão de silenciar a voz (temporário) no tutorial e na voz | Ajuste novo; fala já existe (TTS), falta o liga/desliga e o botão |
| 3 | B14: avisar o nome repetido logo no começo do cadastro | Hoje o aviso vem ao salvar. Mover para depois do nome (etapa 1 e "Confira os remédios" na voz), mantendo a checagem final |
| 4 | Telas iniciais: escolher o horário do primeiro remédio, base dos horários sugeridos | Era o item D da DEC-041, adiado. Ajuste novo |
| 5 | C1: "Lembretes ativos" com indicação maior | Visual |
| 6 | D7: tocar na notificação no meio de um cadastro não abre a Hoje e continua no cadastro | **Comportamento de propósito** (DEC-032/033: não descartar o cadastro). O roteiro estava errado ao dizer "abre a Hoje". Alternativa: abrir a Hoje por cima do cadastro (Voltar retorna a ele) |
| 7 | D8: "Resolver em Ajustes" deve rolar até Permissões | Ajuste de navegação |
| 8 | Pergunta: o que acontece sem resposta? Pedido de aviso automático após o intervalo; adiar conta do toque | **Já é assim** (RF24/RF25): sem resposta, repete após o intervalo, até 3 vezes; Adiar cancela a repetição e conta o intervalo a partir do toque; o aviso adiado sem resposta também repete |
| 9 | K1: sem ícone de despertador na barra de status | O sistema registra o Genki como próximo alarme (`dumpsys alarm`, 30/10); a barra do A31 não mostrou o ícone. Expectativa do roteiro corrigida |
| 10 | K: dois remédios no mesmo horário abriram duas notificações; preferir uma só dizendo que são vários | Mudança na notificação (agrupar por horário, com Entendi todos/Adiar todos). Toca a lógica reparada em RV-048 |
| 11 | E: ler em voz alta um exemplo de resposta | Fala; acrescentar exemplo curto à primeira pergunta |
| 12 | E2: ainda corta se demoro para falar | Pausa atual 1,5 s (`SpeechInput.PAUSE_MS`; 3 s foi achado longo em 29/10). Possível meio-termo |
| 13 | E3: entendeu "Tomou losartana" e cadastrou "Tomou Losartana"; separação funcionou | **Defeito** do interpretador: "tomou" (e variações) não é ignorado como "tomo" |
| 14 | E24: offline funciona, mas parece demorar mais | Esperado: o reconhecimento offline roda no próprio A31, mais lento e menos preciso que o do servidor do Google; não depende do Genki |
| 15 | F: câmera ainda inconsistente; rotular "Beta" ou "Experimental" | Rótulo; registrar como limitação na monografia |
| 16 | G, H, I, M | Não testados nesta rodada |

Observação do assistente (30/10, 15:29, por `dumpsys alarm`): no A31, o alarme das **20:00 de hoje da Losartana não
estava agendado**; as 08:00 de amanhã (Losartana e Rivaroxabana) e quatro horários de teste estavam. A confirmar com o
autor (edição da Losartana?) antes de tratar como defeito. **Esclarecido em 30/10:** o autor excluiu de propósito a Losartana (08:00/20:00) e a Rivaroxabana originais; a Losartana atual (50 mg, 08:00) é de teste. Não é defeito.

**Lote 1 aceito pelo autor em 30/10 ("Faça o lote 1") e implementado na 1.0.13:** itens 1, 3, 5, 7, 11, 12, 13, 15. **Lote 2 na fila até o autor aceitar o lote 1:** itens 2, 4, 10. Item 6 (D7) mantido como está; 8, 9 e 14 respondidos.

**Retorno do lote 1 (30/10, 1.0.13):** itens 1, 3, 4 (B14, C1, D8), 8 e 13 ok ("No demais, passou ok"). Refeitos na 1.0.14:
- **5 (exemplo falado):** fala longa não deve ser padrão → DEC-049, assistente de voz opcional, desligado por padrão, com botão de silenciar.
- **6 (pausa):** ainda cortava no meio → espera conta desde que o reconhecedor volta a ouvir, 2,5 s, aviso "Pode continuar falando". A conferência dos nomes não pergunta mais por voz: instrui tocar em Continuar.
- **7 (verbos):** pergunta sobre a "calibragem" respondida no chat (lista fixa `COMMON` em `voice/VoiceParser.kt`; sem aprendizado).

Fila depois das baterias (pedido do autor, 30/10): redução do tamanho do APK (`ESTADO.md`, pendências).
