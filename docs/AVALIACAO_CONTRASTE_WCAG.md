# Avaliação de contraste — WCAG 2.2, nível AA (app Android)

28/10/2026, Claude Code (Opus 5.5), a pedido do autor ("Melhorar contraste entre textos preenchíveis pelo user e títulos
importantes. Fazer avaliação das diretrizes W3C (cores, transparência, contraste)").

Critérios: **1.4.3** texto ≥ 4,5:1 (texto grande, ≥ 18,66 px negrito ou 24 px, ≥ 3:1); **1.4.11** bordas e partes
de componentes necessárias para identificá-los e estados ≥ 3:1; **1.4.1** cor não é o único meio de informação.
Elementos desabilitados e puramente decorativos são dispensados pela própria norma. Razões calculadas com a fórmula de
luminância relativa da WCAG, a partir das cores de `ui/theme/Color.kt` (`F6FAF7` é o fundo das telas). Transparências
foram compostas sobre o fundo real antes do cálculo.

## Resultado

| Par (frente / fundo) | Uso | Razão | Mínimo | Situação |
|---|---|---|---|---|
| `191C1A` / `F6FAF7` | Texto principal | 16,31 | 4,5 | ok |
| `404944` / `F6FAF7` | Rótulos, exemplos, textos secundários | 8,84 | 4,5 | ok |
| `1B6B4A` / `F6FAF7` | Títulos verdes, links | 6,14 | 4,5 | ok |
| `FFFFFF` / `1B6B4A` | Texto dos botões verdes | 6,46 | 4,5 | ok |
| `B3261E` / `F6FAF7` | Mensagens de erro | 6,21 | 4,5 | ok |
| `00210F` / `B7F0D4` | Texto sobre verde-claro | 13,42 | 4,5 | ok |
| `0F3D2A` / `E4F2EB` | Caixa marcada (texto e ✓) | 10,57 | 4,5 | ok |
| `404944` / `DBE5DE` | Horário "Passou" na Hoje | 7,22 | 4,5 | ok |
| `5E6761` / `F6FAF7` | Ícone e rótulo inativos da barra inferior | 5,56 | 4,5 | ok |
| `CEF5E2` / `1B6B4A` | Subtexto do cartão verde da Hoje | 5,47 | 4,5 | ok |
| `F4EFF4` / `313033` | Aviso "O volume está baixo" | 11,55 | 4,5 | ok |
| `7A857E` / `F6FAF7` | Borda de campos e caixas | 3,64 | 3 | ok |
| `7A857E` / `FFFFFF` | Borda dos campos (fundo branco, abaixo) | 3,83 | 3 | ok |
| branco 65 % / `1B6B4A` | Borda dos botões do cartão "Agora" | 3,74 | 3 | ok |
| `00210F` / `B7F0D4` | Cartão "Lembrete adiado" (DEC-040), texto | 13,42 | 4,5 | ok |
| `0F3D2A` / `B7F0D4` | Cartão adiado, textos secundários | 9,54 | 4,5 | ok |
| `1B6B4A` / `F6FAF7` | Borda do cartão adiado | 6,14 | 3 | ok |
| `BFC9C2` / `F6FAF7` | Linhas divisórias | 1,61 | — | decorativo, dispensado |
| branco 18 % / `1B6B4A` | Fundo do ícone no cartão verde | 1,49 | — | decorativo, dispensado |

Estado das caixas de escolha: o ✓ (10,57:1) indica a marcada, não só a cor de fundo (1.4.1). Botão − apagado no
mínimo: desabilitado, dispensado.

**Conclusão:** nenhuma cor do app fica abaixo do mínimo AA. O incômodo apontado pelo autor é de **hierarquia**: campo
vazio e texto digitado tinham o mesmo fundo da tela, e as perguntas do formulário tinham o mesmo tamanho (16 sp) do
texto digitado.

## Ajustes feitos

- Campos de texto (formulário, voz, câmera) com **fundo branco** (`genkiFieldColors()` em `ui/components`), destacados
  do fundo esverdeado; borda continua `7A857E` (3,83:1 sobre branco).
- "Quanto você toma por vez" e "Horários" passam de `titleSmall` (16 sp SemiBold) para `titleLarge` (18 sp Bold).

Conferido no A31 com fonte 1,5× (captura `build/a31_contraste.png`, não versionada). Observação visual: o fundo branco
aparece também atrás do rótulo flutuante do campo, um pouco além da borda; diferença sutil sobre `F6FAF7`.

## Limites

Avaliação só de cores, transparências e tamanhos do tema e dos pontos citados. Não cobre o restante da WCAG (foco,
ordem de leitura, alvos de toque, TalkBack), nem avaliação com usuários. Cores do sistema (teclado, diálogos do
Android) não são do app.
