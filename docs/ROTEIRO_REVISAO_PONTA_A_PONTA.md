# Roteiro de revisão de ponta a ponta e pré-revisão heurística — Genki

Criado em 29/10/2026 por Claude Code (Opus 5.5), a pedido do autor ("roteiro de revisão de ponta a ponta do aplicativo
para ver se está intuitivo o suficiente … e uma análise heurística … no celular nativo, no ADB"). Execução e achados:
`docs/PRE_REVISAO_HEURISTICA_A31.md`.

**O que este roteiro não é.** Não é o MET-3 (revisão heurística da pesquisa, objetivo h): pela metodologia (Drive,
Avaliação/Genki — Metodologia de avaliação), o MET-3 é feito **pelo autor**, nos 4 fluxos principais, com a lista de
Galavi, Norouzi e Khajouei (2024), que ainda precisa ser conferida no artigo. Também não é teste com usuários (MET-4).
Esta é uma **pré-revisão assistida por IA**, no mesmo formato de registro do MET-3, para o autor validar, descartar ou
reaproveitar. Nenhum resultado daqui entra na monografia como resultado do MET-3 sem a passada do autor.

## Bases usadas

| Base | Como entra |
| --- | --- |
| Nielsen, 10 heurísticas de usabilidade (H1–H10) | Classificação de cada problema (`heurística`) |
| WCAG 2.2, nível AA (W3C) | Critérios aplicáveis a app nativo: 1.4.3/1.4.11 contraste (já avaliados em `AVALIACAO_CONTRASTE_WCAG.md`), 1.4.4 redimensionar texto, 1.3.1 informação e relações, 2.5.7 movimentos de arrastar, 2.5.8 tamanho do alvo, 3.3.1/3.3.3 erro e sugestão, 4.1.2 nome/função/valor, 4.1.3 mensagens de status |
| Diretrizes de acessibilidade do Android/Material 3 | Alvo de toque ≥ 48 × 48 dp, rótulo em todo controle, escala de fonte do sistema até 2,0× |
| Regras do produto (`AGENTS.md`, ESPEC) | Concentração ≠ dose; lembrete reconhecido não é ingestão; confirmação antes de salvar |

Galavi et al. (2024): **não aplicada aqui** (lista não conferida). O autor aplica no MET-3.

Severidade (escala do MET-3): **alta** = impede ou induz erro com consequência (horário/dose errados, lembrete que não
toca, estado falso); **média** = atrapalha ou confunde, mas há saída; **baixa** = cosmético ou de consistência.

## Preparação (via ADB)

- Aparelho: Samsung A31 (Android 12, 1080 × 2400, 420 dpi: 48 dp = 126 px), fonte do usuário (1,3×). Emulador Pixel 6
  (API 35) só para o que o ADB não consegue no aparelho (fala simulada; primeira abertura).
- Dados: usar só medicamentos fictícios ("Teste …") e excluir no fim. Não tocar nos medicamentos do usuário.
- Registrar antes e restaurar depois: `font_scale`, Ajustes do app, permissões, banco do emulador (cópia byte a byte).
- Por tela: captura (`screencap`), árvore de acessibilidade (`uiautomator dump`) e auditoria automática (clicáveis
  < 48 dp e clicáveis sem rótulo). A auditoria aponta candidatos; cada um é conferido na captura antes de virar achado.
- Notificações: ler pelo `dumpsys notification` só as do Genki; **não capturar a gaveta inteira** (pode haver
  notificações pessoais de outros apps).
- Limites do ADB: não fala (a voz exige fala simulada temporária no emulador, removida depois); `input tap` não imita
  o deslize do dedo (usar `input swipe x y x+6 y+4 120`); `input text` não digita acentos.

## Fluxos e o que observar

Os quatro primeiros são os fluxos do MET-3; P e A completam a ponta a ponta.

| Fluxo | Passos | Observar |
| --- | --- | --- |
| **P. Primeira abertura** | Introdução (2 páginas) → pergunta do tutorial → tutorial (7 passos) | Linguagem, pular/voltar, o que o destaque mostra, pergunta falada |
| **F1. Cadastro manual** (T1) | Hoje → Adicionar → Digitar → etapa 1 (nome, concentração, unidade/quantidade) → etapa 2 (dias, horário no relógio) → etapa 3 (duração) → revisão → salvar | Erros sem preencher, ajuda dos exemplos, escolha do horário (precisão, arrastar), consistência das escolhas, revisão, retorno ao salvar |
| **F2. Cadastro por voz** (T2, T3) | Usar voz → fala completa (um completo + um só com nome) → conferência dos nomes → perguntas → revisão → corrigir horário por voz → salvar; fala sem sentido | Status do que foi entendido, perguntas faladas, recuperação de "Não entendi", correção visível, duplicidade |
| **F3. Lembrete** | Horário 3–5 min à frente com o app em segundo plano → notificação (título, texto, botões) → Adiar pela notificação → cartão na Hoje → volta do adiado → Entendi | Chegada no horário, prioridade, conteúdo na tela bloqueada, estado do adiado, coerência entre cartão e alarme |
| **F4. Edição e exclusão** (T4) | Medicamentos → Detalhes → Editar → trocar horário → salvar; Desativar lembretes; Excluir | Caminho até editar, edição vs. cadastro, confirmação de exclusão, retorno |
| **A. Ajustes e escala** | Ajustes (repetição, intervalo, som, voz, permissões, tutorial); fonte 2,0× em Hoje, escolha, formulário, descarte, Ajustes | Explicação das opções, estado selecionado, nada cortado em 2,0× |

Fora do escopo desta passada: câmera (não é fluxo do MET-3), TalkBack real (a árvore de acessibilidade é só indício),
som audível, Não perturbe, reinício do aparelho.

## Registro

Uma linha por problema, no formato do MET-3 (planilha "Heurística" da metodologia):

`fluxo | tela | problema | heurística | severidade | correção`

Na coluna correção, nesta pré-revisão, vai a **sugestão** (não implementada). Separar sempre: problema observado no
aparelho · inferido do código · inconclusivo (precisa de TalkBack ou de pessoa).

## Restauração

Excluir os medicamentos de teste pela interface; voltar fonte, intervalo do adiamento e permissões; restaurar o banco e
o código do emulador (conferir `git diff -- app` vazio e o hash de `SpeechInput.kt`).
