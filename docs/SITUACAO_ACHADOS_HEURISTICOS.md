# Genki — situação dos achados da avaliação heurística (referência: 04/11/2026)

**Para quem lê (GPT, Claude ou outra ferramenta de apoio à escrita do TCC):** este documento é autocontido. Ele responde a duas perguntas: **(1) os achados da avaliação heurística foram resolvidos?** e **(2) em que versão do aplicativo estamos?** Foi preparado pelo Claude Code (Sonnet 5.5) em 04/11/2026 a pedido do autor, a partir do repositório `genki`, da planilha "Genki — Avaliação heurística 1.0.23 — planilha de achados" (Drive do autor, aba "Consolidação CC") e dos registros do desenvolvimento. **É um relato de apoio, não uma decisão do autor.** Tudo que for citado na monografia deve ser conferido pelo autor contra o registro.

## 1. Resposta curta

| Pergunta | Resposta |
| --- | --- |
| Versão **avaliada** (heurística, verificação técnica, personas) | **1.0.23**, congelada em 03/11/2026 (tag `v1.0.23`, commit `7bb00bc`) |
| Última versão **publicada** | **1.0.25** (Release `v1.0.25`, 04/11/2026, tag em `8717429`). O ramo `main` ainda não foi enviado ao GitHub: o README e o guia públicos continuam mostrando a 1.0.24 até o push |
| Versão **em curso**, ainda **não publicada** | **1.0.26** (versionCode 27), commits `cf3afd1` e `67a5055`; sem tag nem Release. O autor fará uma análise geral antes de decidir a Release. **Não citar a 1.0.26 como publicada.** |
| Achados da avaliação heurística | **30 registrados** pelo Claude Code (CC-01 a CC-30), todos aceitos pelo autor em 03/11/2026 |
| Resolvidos | **26 de 30**: o CC-01 (1.0.24) e 25 dos 29 restantes (1.0.25), sendo 2 deles **em parte** |
| Não corrigidos | **4**, todos de severidade baixa: dois por decisão do autor (P5, P17), um não reproduzido (P14) e um cuja tentativa de correção piorou a tela e foi revertida (P4) (seção 4) |
| Achado novo depois da avaliação | **1** (AU-01, 04/11), corrigido na 1.0.26 (não publicada) |
| A versão corrigida foi **reavaliada** por heurística? | **Não.** As correções foram conferidas em emulador e no aparelho do autor, item a item e de forma parcial (seção 5), mas não houve nova avaliação heurística completa |

## 2. O que se pode e o que não se pode afirmar

**Pode** (com a ressalva de que cada item vem do registro do desenvolvimento):
- "A avaliação heurística foi feita sobre a versão 1.0.23, congelada. Depois dela, o autor reabriu o congelamento só para a experiência do usuário (DEC-057) e as correções foram feitas nas versões 1.0.24 e 1.0.25."
- "Dos 29 achados heurísticos aceitos, 25 foram corrigidos (2 em parte) e 4 ficaram como limitações. O defeito de funcionamento CC-01 foi corrigido à parte."
- "Parte das correções foi conferida em emulador (Android 15) e no Samsung Galaxy A31 (Android 12) pelo ADB; outras só passaram pelos testes unitários."

**Não pode** (não há registro que sustente):
- que o aplicativo "não tem mais problemas de usabilidade" ou que a versão corrigida foi reavaliada;
- que todas as correções foram testadas em aparelho (várias só compilam e passam nos testes: tabela da seção 5);
- que houve teste com leitor de tela (TalkBack): **não houve**;
- que a avaliação cobriu fala real, câmera com embalagem real ou som audível: não cobriu (a avaliação foi no emulador, sem áudio);
- que as ferramentas de IA foram avaliadoras independentes: o protocolo as trata como **apoio rastreável**, e o autor conhecia as telas do aplicativo porque participou do desenvolvimento.

## 3. Linha do tempo das versões

| Versão | Data | O que mudou em relação aos achados | Situação |
| --- | --- | --- | --- |
| 1.0.23 | 03/11/2026 | Versão **avaliada**; congelada pelo autor depois de conferir as tarefas 2, 3 e 11 e a rotação no A31. Inclui, entre outros, 1/4 de comprimido, "Desfazer" na exclusão e app só em retrato (DEC-055) | Publicada (tag e Release `v1.0.23`) |
| 1.0.24 | 03/11/2026 | **Só o CC-01** (editar um medicamento apagava o lembrete adiado e a repetição). Commits `a2e318a` e `55a9c3b` | Publicada (tag e Release `v1.0.24`) |
| 1.0.25 | 03/11 a 04/11/2026 | **Refinamento heurístico P1 a P29** (DEC-057), só de interface; sem mudança na lógica de lembretes nem no esquema do banco. Revisado pelo Claude Code e depois pelo Codex (seção 7) | Publicada (tag e Release `v1.0.25`) |
| 1.0.26 | 04/11/2026 | **AU-01**: página das notificações da introdução com texto enxuto; a dica do pop-up foi para Ajustes (seção 6). Também só interface | **Em curso; não publicada** |

Corte de prazo da DEC-057: o que não estivesse pronto e conferido até 05/11/2026, 12:00, ficaria como não corrigido.

## 4. Estado de cada achado

**Numeração.** O autor atribuiu, em 03/11/2026 (19:51), os identificadores **P1 a P29** para a monografia, na ordem Pn = CC-(n+1). O **CC-01 foi reclassificado pelo autor como defeito de funcionamento (lógica), fora da contagem e das heurísticas** na monografia, e por isso não tem P. Severidade: a da coluna "final" é a **do autor**; as do CSV original eram sugestões do Claude Code. As heurísticas (H1 a H20) são as de Galavi, Norouzi e Khajouei (2024), conforme a planilha.

**Legenda da coluna "Verificação"** (nível mais forte que consta no registro): **A31** = visto no Samsung Galaxy A31 (Android 12) pelo Claude Code por ADB em 04/11; **Emu** = visto no emulador Pixel_6_2 (Android 15, API 35); **Testes** = só compilado e coberto pelos testes unitários; **—** = nada a verificar.

| P | CC | Achado (resumo) | H | Sev. | Situação | Versão (commit) | Verificação |
| --- | --- | --- | --- | --- | --- | --- | --- |
| — | CC-01 | Salvar a edição de um medicamento apagava, sem aviso, o lembrete adiado e a repetição | H1; H16 | alta (defeito de lógica) | **Corrigido** | 1.0.24 (`a2e318a`) | A31 por ADB em 03/11 (editar sem mudança, mudar dose, mudar horário). Limite: mudar o minuto do próprio horário adiado ainda apaga o adiado dele. Falta o autor repetir F3 e F4 |
| P1 | CC-02 | Em fonte 2,0×, a duração ficava cortada sob os botões na revisão, sem sinal de que há mais conteúdo | H5; H7; H12 | média | **Corrigido** (indicador "ver mais") | 1.0.25 (`5470da3`) | A31 + Emu |
| P2 | CC-03 | Em fonte 2,0×, "Silenciar a voz" e "Prefiro digitar" ficavam abaixo da dobra na abertura da voz | H3; H8; H12 | média | **Corrigido** | 1.0.25 (`5470da3`) | Emu |
| P3 | CC-04 | Em fonte 2,0×, os números do relógio se sobrepunham | H7; H12; H14 | baixa | **Corrigido** (abre direto em "digitar o horário" com fonte ≥ 1,5×) | 1.0.25 (`60400a9`) | A31 + Emu |
| P4 | CC-05 | Em fonte 2,0×, o botão fixo e a barra inferior ocupam cerca de ¼ da tela na Hoje | H7; H4; H12 | baixa | **Não corrigido** (tentativa revertida: cortava "Medicamentos") | revertido em `bd78b36` | — |
| P5 | CC-06 | A edição grava direto, sem tela de revisão como no cadastro | H4; H5 | baixa | **Não corrigido** por decisão do autor (fluxo novo) | — | — |
| P6 | CC-07 | "Entendi" não dava confirmação do que aconteceu | H1 | baixa | **Corrigido** (aviso "Lembrete encerrado. O Genki não registra se o remédio foi tomado.") | 1.0.25 (`7353be1`) | A31 + Emu |
| P7 | CC-08 | O cartão "Agora" não informava a repetição agendada | H1 | baixa | **Corrigido** | 1.0.25 (`7353be1`) | A31 + Emu |
| P8 | CC-09 | O horário em curso ficava escondido entre os "que já passaram" | H1; H4 | baixa | **Corrigido** | 1.0.25 (`7353be1`) | Emu (no A31 não foi visto por completo) |
| P9 | CC-10 | A introdução destacava "Começar" e deixava "Permitir notificações" secundário | H5 | baixa | **Corrigido** | 1.0.25 (`60400a9`) | A31 |
| P10 | CC-11 | O aviso de que o alarme exato negado pode atrasar o lembrete só aparecia em Ajustes | H1; H5 | baixa | **Corrigido** (frase na introdução) | 1.0.25 (`60400a9`) | A31 (negando a permissão por `appops`) |
| P11 | CC-12 | Texto técnico sobre "pop-up" na introdução | H2; H10 | baixa | **Corrigido**; na 1.0.26 a dica **mudou de lugar** (Ajustes > Permissões) | 1.0.25 (`7353be1`) e 1.0.26 | A31 |
| P12 | CC-13 | Passo 2 do tutorial destacava a mensagem vazia, não o cartão descrito | H15; H16 | baixa | **Corrigido** (texto) | 1.0.25 (`7353be1`) | Parcial: tutorial visto no A31 em 03/11; o texto do passo 2 não foi conferido à parte |
| P13 | CC-14 | O "Desfazer" da exclusão sumia em cerca de 9 a 10 s | H3 | baixa | **Corrigido** (15 s, valor **proposto**; o autor ajusta) | 1.0.25 (`60400a9`) | Testes; o toque dentro do prazo não foi testado |
| P14 | CC-15 | O exemplo sumia depois de "Não ouvi nada" na voz | H9; H8 | baixa | **Não corrigido: não reproduzido** no código nem nas capturas; decisão do autor pendente | — | — |
| P15 | CC-16 | "Silenciar a voz" parecia desativado | H1; H20 | baixa | **Corrigido** (borda mais escura) | 1.0.25 (`7353be1`) | Testes |
| P16 | CC-17 | Dias selecionados diferiam só pelo "✓" | H1; H20 | baixa | **Corrigido** (borda verde de 2 dp) | 1.0.25 (`7353be1`) | Testes |
| P17 | CC-18 | Voltar da voz leva à Hoje | H3 | baixa | **Não corrigido** por decisão do autor (mexe na navegação) | — | — |
| P18 | CC-19 | "Adicionar horário" sempre abria em 08:00 (erro se já existisse) | H5; H9 | baixa | **Corrigido** (sugere o primeiro horário cheio livre) | 1.0.25 (`60400a9`) | Testes |
| P19 | CC-20 | 999 comprimidos aceitos sem aviso | H5 | média | **Corrigido** (aviso não bloqueante; limite **proposto** de 20) | 1.0.25 (`53178fb`) | Emu |
| P20 | CC-21 | A duração só podia ser dada por data, não por "por quantos dias" | H2 | média | **Corrigido** (campo "Por quantos dias?") | 1.0.25 (`6ad2897`) | Emu |
| P21 | CC-22 | "Sobre" sem versão nem informação de privacidade | H11; H10 | média | **Corrigido** (versão e texto de privacidade aprovado pelo autor) | 1.0.25 (`d7aa8a0`) | A31 + Emu |
| P22 | CC-23 | Lista sem ordem alfabética nem busca | H6; H7 | média | **Corrigido em parte**: ordem alfabética sim; **busca não** (trabalho futuro) | 1.0.25 (`64e3037`) | Testes |
| P23 | CC-24 | "Ativar assistente de voz" prometia mais do que fazia | H2; H4 | baixa | **Corrigido** (agora "Ler o tutorial em voz alta") | 1.0.25 (`7353be1`) | A31 |
| P24 | CC-25 | Nome muito longo mostrava só o fim do texto | H5; H8 | baixa | **Corrigido** (campo quebra em até 3 linhas; o limite de tamanho não mudou) | 1.0.25 (`60400a9`) | Testes |
| P25 | CC-26 | O cartão "Próximo lembrete" não abria os Detalhes | H4 | baixa | **Corrigido** (tocar no remédio abre os Detalhes) | 1.0.25 (`60400a9`) | Testes |
| P26 | CC-27 | Dias, chips, unidade e separadores inconsistentes | H4 | baixa | **Corrigido em parte**: dias com inicial maiúscula e separador unificado; chips abreviados e "ml" mantidos | 1.0.25 (`60400a9`) | Testes |
| P27 | CC-28 | Sem concentração, a linha sumia da revisão e dos Detalhes | H1 | baixa | **Corrigido** ("Concentração: Não informada") | 1.0.25 (`7353be1`) | A31 (revisão) + Emu; Detalhes não visto |
| P28 | CC-29 | O balão do tutorial não tinha "Voltar" | H3 | baixa | **Corrigido** | 1.0.25 (`7353be1`) | Parcial: tutorial no A31 em 03/11, com todos os botões à vista |
| P29 | CC-30 | O cartão do horário era tocável sem indício visual | H6; H8 | baixa | **Corrigido** (ícone de lápis) | 1.0.25 (`7353be1`) | A31 |

**Contagem (os 29 heurísticos, P1 a P29):** média 6 (P1, P2, P19 a P22), baixa 23. Corrigidos 25 (23 por inteiro e 2 em parte: P22 e P26); não corrigidos 4 (P4, P5, P14, P17), **todos de severidade baixa**. Os 6 de severidade média foram tratados: 5 por inteiro e o P22 em parte (sem busca).

**Limitações que ficam para a monografia** (decisão do autor, a registrar como limitações ou trabalhos futuros): P4 (Hoje em fonte 2,0×), P5 (edição sem revisão; a planilha anota que sobe para média se o autor entender que a edição exige revisão, RF17), P14 (não reproduzido), P17 (navegação do Voltar), a **busca** de medicamentos (P22) e os chips abreviados de dias e a unidade "ml" (P26).

## 5. Como foi verificado, e o que não foi

- **Quem verificou:** o mesmo assistente que implementou as correções (Claude Code). O Codex revisou o **código** da 1.0.25 (seção 7), mas **não operou o aparelho** nem refez a avaliação heurística. O autor declarou, em 04/11, ter testado manualmente as correções anteriores da 1.0.25 ("deram certo"), sem detalhar item a item; essa declaração **não** está nas colunas acima.
- **Itens vistos no A31 em 04/11** (por ADB, com fonte 2,0× e instalação do zero quando necessário): P1, P3, P6, P7, P9, P10, P11, P21, P23, P27 (revisão), P29. Lembrete real de um remédio disparado e encerrado. A fala do alarme foi **ouvida pelo autor** ("realmente lê"); o conteúdo falado não foi registrado.
- **Itens vistos só em emulador:** P2, P8, P19 e P20. (P1, P3, P6, P7, P21 e P27 foram vistos nos dois.)
- **Itens que só compilam e passam nos testes (228 testes, 0 falhas na 1.0.26):** P13, P15, P16, P18, P22, P24, P25, P26, além de partes de P12 e P28.
- **Nunca verificado:** **TalkBack** (leitor de tela), voz real no aparelho, câmera com embalagem real, som do alarme com tela bloqueada, Doze/economia de bateria, outros aparelhos além do A31, Android 13 ou superior.
- **A avaliação original (1.0.23):** feita no emulador `Genki_V1_Teste` (Android 15, sem áudio), instalação limpa do APK da Release v1.0.23, com remédios fictícios. O A31 não foi usado nela. Fala real, câmera real, som, TalkBack e A31 ficaram como não verificados.

## 6. Achado novo depois da avaliação: AU-01 (na 1.0.26, não publicada)

- **Origem:** o autor, ao ver a introdução no A31 em instalação do zero (04/11/2026). É um achado **novo** (não vem da passada do Claude Code); o identificador **AU-01** é interno. Se o autor o incluir na monografia, a numeração segue a dele (P30 é a proposta usada nos registros do repositório).
- **Problema:** a última página da introdução, na instalação do zero, tinha texto demais (cerca de 50 palavras com tudo permitido e cerca de 75 com tudo negado, contagem do Claude Code), podendo caracterizar texto excessivo. Em fonte 2,0× a página já precisava rolar.
- **Heurística e diretriz:** H7 (design estético e minimalista), H10 (ajuda e documentação) e G17 (mensagens curtas). Severidade sugerida pelo Claude Code: média; a **final é do autor** (pendente).
- **Correção (1.0.26, commits `cf3afd1`, `67a5055`):** a página ficou com a frase das notificações, o botão ou o estado da permissão e, só com o alarme exato negado, uma frase curta. A dica do pop-up foi para Ajustes > Permissões, abaixo de "Notificações" (lugar escolhido pelo Claude Code; **o autor ainda não aceitou**).
- **Verificação:** A31, instalação do zero, fonte 2,0×, três casos de permissão: a página cabe sem rolar nos três.
- **Relação com outros achados:** liga-se ao P11 (CC-12), que reescreveu a mesma dica, e ao P10 (CC-11), que acrescentou a frase do alarme exato; ambas aumentaram o texto da página. O CC-10 (P9) trata do botão principal da mesma página.

## 7. Correções de qualidade da própria 1.0.25 (revisões de código)

O refinamento (P1 a P29) foi revisado pelo Claude Code (7 achados, sem S1/S2, no lugar do Codex, que parou por limite de uso) e depois pelo **Codex** (revisão independente do código: **nenhum achado de gravidade S1 ou S2**; três achados novos). Os principais, já corrigidos antes da Release:

| Achado | Ligação | Corrigido |
| --- | --- | --- |
| RV-125-01 (S3): a página da introdução podia voltar à 2 se a tela fosse recriada | introdução em páginas dinâmicas | sim |
| RV-125-02 (S3): o aviso de quantidade alta reaparecia ao editar sem mudar a dose | P19 | sim |
| RV-125-08 (S3): "Por quantos dias?" aceitava texto que não correspondia à data gravada | P20 | sim |
| RV-125-09 (S3): o cartão continuava dizendo "avisa de novo" depois da 3ª repetição | P7 | sim |
| RV-125-10 (S4): a descrição do alarme falado omitia que, com vários remédios, diz só a quantidade | texto de "Como alarme" | sim |
| RV-125-03 (S4): o aviso "Lembrete encerrado" não reiniciava os 6 s; RV-125-04 (S4): quebra de linha colada no nome | P6, P24 | sim |
| RV-125-05, 06, 07 (S4) | texto curto de "Como alarme" mantido pelo autor; README e guia; imports sem uso | 05 mantido, 06 feito, 07 ignorado |

Esses itens são **revisão de código**, não avaliação heurística, e não entram na contagem de achados heurísticos.

## 8. As pré-revisões heurísticas do desenvolvimento

Antes da avaliação formal, o Claude Code fez duas pré-revisões durante o desenvolvimento (29/10, versão 1.0.9; 30/10, versão 1.0.16, com as 10 heurísticas de Nielsen e WCAG 2.2 AA), mais uma avaliação de contraste WCAG (28/10). O protocolo as trata como **avaliação formativa ao longo das versões**, separada da avaliação do autor sobre a 1.0.23. A passada de 03/11 conferiu quais achados abertos delas continuavam presentes na 1.0.23: **N2, N8, N9, N10, N11, N12, N13, N14, N17, N18, N19** e os itens 11, 12, 18 e 20 de 29/10 reapareceram como CC-03, CC-20, CC-21, CC-22, CC-23, CC-24, CC-25, CC-19, CC-26, CC-27, CC-28, CC-29 e CC-30, registrados como "reconfirmação de pré-revisão". N5 estava corrigido e N2 e N12 atenuados; **N6, N7, N16 e os itens 5 e 6 de 29/10 não foram verificados** por falta de fala real e continuam sem verificação.

## 9. Pontos de atenção para a escrita (integridade do registro)

1. **Origem dos achados.** A coluna "Origem" da consolidação diz "Autor e Claude Code" para todos os 30, e a aba "Achados" da planilha só tem a linha de exemplo (a passada 1 do autor **não está itemizada** na planilha). O protocolo exige informar quantos problemas o autor encontrou sozinho e quantos vieram das ferramentas e foram aceitos; **com o registro atual essa separação não é possível**. O autor precisa resolver isso antes de relatar números. A consolidação registra ainda que o autor declarou ter confirmado no A31 a existência dos 30 problemas na 1.0.23 (03/11/2026, 18:40).
2. **Independência.** O Claude Code avaliou, corrigiu e verificou parte das correções. O protocolo proíbe apresentar concordância entre IAs, contagem de achados ou aceite humano como prova de cobertura, e manda declarar que o autor conhecia as telas.
3. **Versão.** Toda afirmação sobre "o aplicativo avaliado" refere-se à **1.0.23**; as correções são posteriores. Indicar a versão sempre que citar um comportamento.
4. **Planilha desatualizada.** A coluna "Ação" da aba "Consolidação CC" ainda diz "implementado na 1.0.25 (…, local); A31 pendente" para a maioria dos itens, e para o CC-01 cita a tag `v1.0.24`. Hoje a 1.0.25 está publicada e vários itens foram vistos no A31 (seção 5). A planilha precisa ser atualizada pelo autor ou a pedido dele.
5. **Registro corrigido.** Notas anteriores do repositório diziam que a tag `v1.0.24` não existia; **ela existe** (tag e Release desde 03/11, 19:34).
6. **Valores propostos.** O limite de 20 do aviso de quantidade (P19) e os 15 s do "Desfazer" (P13) são propostas do Claude Code que o autor aceitou como estão por ora; podem mudar.
7. **Escopo.** A introdução e o tutorial (F6) estão **fora da proposta** (DEC-041, DEC-044); os achados deles (P9 a P12, P23, P28, AU-01) devem ser relatados à parte, como recursos fora da proposta.

## 10. O que ainda pode mudar este documento

- A análise geral do autor antes de decidir a Release da 1.0.26 (pode surgir achado novo ou ajuste).
- A Release da 1.0.26 (hoje inexistente) e o apontamento da monografia para ela.
- A decisão do autor sobre o P14 (retirar ou manter), a severidade final do AU-01 e o lugar da dica do pop-up em Ajustes.
- Verificação por TalkBack, do texto falado do alarme e do P8 no A31.
- A atualização da planilha "Consolidação CC".

## Fontes

Repositório `genki` (público): `docs/REFINAMENTO_HEURISTICO_1.0.25.md` (tabela P1 a P29 e P30), `docs/DECISOES.md` (DEC-057), `docs/HANDOFF_VERSAO.md` e `docs/ESTADO.md` (estado e verificações), `docs/revisoes/REVISAO_REFINAMENTO_1.0.25_CLAUDE.md` e `…_CODEX.md`, `docs/PRE_REVISAO_HEURISTICA_1.0.16.md` e `…_A31.md`. Pasta local da avaliação (fora do Git): `genki-avaliacao/` (`achados_claude_code.csv`, `RELATORIO_passada2_claude_code.md`, `ESFORCO_correcoes.md`, `protocolo.txt`, `achados_autor/`). Drive do autor: planilha "Genki — Avaliação heurística 1.0.23 — planilha de achados" (abas "Achados" e "Consolidação CC"). Tags e Releases: `v1.0.23`, `v1.0.24` e `v1.0.25` no GitHub.
