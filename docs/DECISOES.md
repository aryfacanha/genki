# Genki — decisões

Situações: **aceita** (o autor disse que aceita; data registrada) · **proposta** (aguarda o autor) · **substituída**.
Numere decisões novas continuando a sequência. Não apague decisões: marque como substituídas.

| ID | Data | Tema | Decisão (resumo) | Situação |
| --- | --- | --- | --- | --- |
| DEC-001 | 19/10 | Voz semiestruturada | Fala única ou progressiva; pergunta uma coisa por vez só para completar o que falta ou está ambíguo. Refinamento da proposta ("por etapas", Introdução p. 4 e Motivação p. 5 impressas); a monografia explica a evolução | aceita 19/10 |
| DEC-002 | 19/10 | RF03 | Nova redação: "O cadastro deve permitir informar nome, concentração (opcional), dose/quantidade por uso e programação de uso do medicamento." | aceita 19/10 — aplicada na planilha em 21/10 |
| DEC-003 | 19/10 | RF08 | Observação: "Duração informada (ex.: 'por 7 dias') é convertida em data final e apresentada ao usuário na confirmação." | aceita 19/10 — aplicada na planilha em 21/10 |
| DEC-004 | 19/10 | RF24 | Acrescentar: "O reconhecimento do lembrete não deve ser registrado nem apresentado como confirmação da ingestão." | aceita 19/10 — aplicada na planilha em 21/10 |
| DEC-005 | 19/10 | RNF11 (novo) | "O MVP não deve exigir conta/autenticação, servidor de aplicação ou sincronização em nuvem, nem exibir anúncios." Prioridade Alta | aceita 19/10 — aplicada na planilha em 21/10 |
| DEC-006 | 19/10 | PoC de imagem | RF20 continua embalagem; a verificação técnica pode incluir prescrições fictícias se o orientador concordar (proposta §2.1 cita "embalagens ou prescrições") | aceita 19/10 — depende do orientador |
| DEC-007 | 19/10 | Fontes | Planilha = fonte oficial dos requisitos; protótipo de referência = `Genki_Prototipo_Figma_v1.zip` (congelado); `Genki(15).zip` é histórico | aceita 19/10 — protótipo: substituída em parte por DEC-022 |
| DEC-008 | 21/10 | Modelo de dados | Medicamento e programação na mesma entidade + tabela de horários; concentração e dose separadas; frequência sempre convertida em horários concretos (ESPEC §3) | aceita 21/10 |
| DEC-009 | 21/10 | "Ainda não sei" | Lembretes normais, sem data final; rótulo "Duração a definir" | aceita 21/10 |
| DEC-010 | 21/10 | Duplicidade | Recomendado em 19/10: aviso não bloqueante. **Simplificado (S1)**: sem tratamento de duplicidade; limitação registrada | aceita 21/10 com S1 (vetável) |
| DEC-011 | 21/10 | Versões | minSdk 26; compile/target e bibliotecas = template do Android Studio do autor, registrados no M0. Falta o modelo do celular de teste | aceita 21/10 |
| DEC-012 | 21/10 | Lembretes | AlarmManager exato com fallback inexato; repetição a cada intervalo dos Ajustes (padrão 20 min), até 3 vezes; "Entendi" ou deslizar encerra. **Simplificado (S2)**: sem tabela de alertas, adiamento único pelo intervalo dos Ajustes, repetição/adiamento não sobrevivem a reinício | aceita 21/10 com S2 (vetável) |
| DEC-013 | 21/10 | Infraestrutura | Injeção manual, Navigation Compose, sem Hilt/WorkManager/DataStore. **Simplificado (S3)**: ajustes em SharedPreferences | aceita 21/10 com S3 (vetável) |
| DEC-014 | 21/10 | Validação e datas | Um validador para manual, edição, voz e OCR; datas no fuso do aparelho; "por N dias" inclui o dia inicial. **Simplificado (S4)**: 6 regras essenciais | aceita 21/10 com S4 (vetável) |
| DEC-015 | 21/10 | Instrumentação | Build debug registra em Logcat (`GenkiEval`) tempos, transcrição e dados interpretados; nunca áudio. Necessário para o objetivo g | aceita 21/10 |
| DEC-016 | 21/10 | Ordem de corte | 1º simplificar RF14 (um medicamento por fala); 2º reduzir a PoC de imagem ao mínimo verificável. CRUD, lembretes e voz com confirmação não são cortados | aceita 21/10 |
| DEC-017 | 21/10 | Congelamento | App congelado em 02/11/2026; depois, só correções | aceita 21/10 |
| DEC-018 | 21/10 | Metodologia mínima | MET-1 a MET-8 (voz 12 frases × 2; imagem 10 embalagens × 2; heurística em 4 fluxos; tarefas T1–T4; sem gravação; SUS brasileira; dados fictícios com nomes reais; número de participantes com o orientador). Documento no Drive: Avaliação/ | aceita 21/10 |
| DEC-019 | 20/10 | Organização | Drive: proposta, requisitos, monografia, avaliação, fontes acadêmicas. GitHub: código, documentação técnica, estado, decisões, entregas, instruções para assistentes. Projeto do Claude vira histórico | aceita 20/10 (autor) |
| DEC-020 | 20/10 | Proposta oficial | `Arquivos/Genki_Proposta_Final.pdf` no Drive (mesmo envelope Docusign do PDF anexado ao Projeto do Claude) | aceita 20/10 (autor) |
| DEC-022 | 21/10 | Protótipo reaberto | O autor está revisando o protótipo no Figma Make, com o GPT, por razões de usabilidade. O v1 deixa de ser a referência final; a nova exportação passa a ser referência quando concluída e colocada no Drive. Até lá, a ESPEC vale para dados, regras e fluxos; telas seguem o protótipo mais recente | aceita 21/10 (autor) |
| DEC-021 | 21/10 | Testes de código | **Simplificado (S5)**: só testes unitários da lógica pura; UI e Android verificados manualmente no aparelho | aceita 21/10 com S5 (vetável) |
| DEC-023 | 26/10 | Plano até a entrega | Sequência de 16 tarefas e cronograma (Android a partir de 26/10, congelamento 02/11, heurística 02–03/11, verificação técnica 03–05/11, participantes 06–11/11 só com autorização). Classificação da pesquisa mantida conforme a proposta. Sessão única por participante (T1–T4 + SUS, sem gravação, mesmo aparelho). Protótipo web não é usado com participantes. Fluxo de voz V1–V6 do retorno do autor fica como trabalho futuro (V6 só com nova DEC). Perguntas ao orientador com propostas (cenário B proposto, só embalagens etc.) — **as propostas dependem do orientador**. Documento: Drive, Avaliação/Genki — Plano de etapas e cronograma até a entrega | aceita 26/10 (autor) |
| DEC-024 | 26/10 | Objetivo i após resposta do orientador | **Caminho C:** submeter o protocolo ao CEP-IFRS (submissão meta 30/10, comprovante guardado) e fazer avaliação guiada por 2 personas (T1–T4 no app congelado, 05–06/11) como complemento analítico; testes com pessoas reais só com parecer de aprovação (sessões 10–13/11 se aprovado até ~09/11). SUS nunca respondida por personas. Personas são acréscimo à proposta: registrar como divergência na monografia. **Meta de 10 participantes** (Faulkner, 2003). Substitui o cenário B e a amostra de 5 da DEC-023. Pendente: só embalagens na PoC de imagem | aceita 26/10 (autor) |
| DEC-025 | 26/10 | Protótipo como referência final de UX | Orientador avaliou o protótipo publicado (Netlify, genki-prototype `295212e`) como "muito bom" em 26/10; o autor decidiu seguir com ele. Encerra a revisão aberta pela DEC-022: telas do Android seguem esse protótipo; dados, regras e fluxos seguem a ESPEC. V1–V6 continuam fora (DEC-023) | aceita 26/10 (autor) |

## Pendentes

| Tema | Situação |
| --- | --- |
| Padrão de autoria dos commits de assistentes | A definir pelo autor antes do desenvolvimento |
| Contato com a coordenação (orientação do orientador em 26/10) | Justificativa formal (Drive, Avaliação/Genki - Justificativa formal da avaliação com personas) conciliada com a revisão do GPT em 26/10; e-mail final à Coordenação TADS (coord.tads@canoas.ifrs.edu.br); envio pelo autor. CEP não será contatado diretamente por ora (autor, 26/10) |
| CEP: submeter ou não | CEP-IFRS (cepesquisa@ifrs.edu.br) orienta coleta 45–60 dias após a submissão (página do CEP, 26/10) → coleta só após 16/11. Autor perguntou ao orientador em 26/10 se ainda submete (protocolo como apêndice) ou concentra nas personas. Aguarda resposta; a DEC-024 pode ser revista |
| Fontes metodológicas de personas e percurso cognitivo | Não estão na proposta; localizar e conferir antes de escrever o método |
| Âncoras/escolha da SUS | **Orientador (26/10):** pode usar, com justificativa própria da escolha. Pendente |
| Declaração de IA | **Orientador (26/10):** atualizar na versão final, com todas as IAs mencionadas e justificadas |
| Prescrições fictícias na PoC (DEC-006) | **Orientador (26/10):** decisão do autor, bem justificada. Pendente |
| Celular de teste | Ainda não definido pelo autor |
| Mudanças do protótipo que afetem dados ou regras (DEC-022/025) | Listar ao especificar cada marco do Android |

## Detalhes das decisões de 19/10

Os blocos completos (problema, alternativas, impacto) de DEC-001 a DEC-007 estão em `03_ESTADO_DECISOES.md` v1.1, no
Projeto do Claude (histórico). As decisões de 21/10 estão detalhadas em `docs/ESPEC_ARQUITETURA.md` e no documento de
avaliação do Drive.
