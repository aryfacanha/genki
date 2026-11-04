# Refinamento heurístico — Genki 1.0.25

Claude Code (Sonnet 5.5), 03/11/2026. Pedido do autor (03/11, 20:45); reabertura do congelamento na DEC-057. Fonte: `achados_claude_code.csv` (pasta `genki-avaliacao`, fora do Git) e aba "Consolidação CC". Correspondência: Pn = CC-(n+1). O CC-01 foi corrigido na 1.0.24 e não faz parte. Partida: `main`/`d292a2a`. Só experiência do usuário: nenhuma mudança na lógica de lembretes nem no esquema do banco.

**Legenda de verificação.** *Emulador* = visto na tela do emulador Pixel_6_2 (API 35), com fonte 2,0× quando indicado. *Compila/testes* = só `assembleDebug test` (214 testes, sem falha). **Nada foi visto no A31** (o A31 não foi operado nesta tarefa). Tudo que não diz "Emulador" **não foi visto em tela**.

## Corrigidos

| P | CC | O que mudou | Arquivos | Commit | Como conferir no aparelho | Verificação |
| --- | --- | --- | --- | --- | --- | --- |
| P1 | CC-02 | Indicador discreto "ver mais" (seta num círculo de 48 dp, sobreposto, não muda o layout) só enquanto há conteúdo abaixo; toque rola ¾ de tela; some no fim. Na revisão | `ui/components/Components.kt` (`ScrollHintColumn`, `showScrollHint`), `ui/form/FormScreen.kt` | `5470da3` | Fonte 2,0×: cadastrar até "Confira antes de salvar": seta embaixo; tocar rola até "Duração do tratamento" e a seta some | Emulador 2,0× (revisão e etapa 3); 3 testes de `showScrollHint` |
| P2 | CC-03 | "Silenciar a voz" e "Prefiro digitar" sobem para logo abaixo do microfone; "Você poderá revisar tudo…" desce; espaços menores; mesmo indicador | `voice/ui/VoiceScreen.kt` (`Intro`) | `5470da3` | Fonte 2,0×: Adicionar > Usar voz: os dois botões aparecem rolando uma vez (ou tocando a seta) | Emulador 2,0× (antes e depois de rolar). Leitura em voz alta e ordem do TalkBack **não verificadas** |
| P19 | CC-20 | Aviso não bloqueante "N comprimidos por vez. Está certo?" ao salvar, com "Voltar e conferir" e "Está certo, salvar". **Limite proposto: 20, uma constante só (`DoseUnit.UNUSUAL_AMOUNT`)** para comprimido, cápsula, gota e ml; "Outro" (texto livre) nunca avisa. Autor manteve 20 como proposta (03/11) | `ui/form/DoseUnit.kt`, `FormViewModel.kt`, `FormScreen.kt` | `53178fb` | Cadastro manual com 999 comprimidos: o aviso abre ao tocar em "Confirmar e salvar" | Emulador 2,0×; 2 testes novos de `DoseUnit`. **Só no formulário manual e na edição**: o cadastro por voz não tem o aviso (a dose aparece na revisão) |
| P20 | CC-21 | Campo "Por quantos dias? (opcional)" na etapa 3, com "Termina em dd/mm/aaaa"; grava a data de término (início + N − 1, mesma regra do `DraftValidator`); escolher a data no calendário atualiza o número; 1 a 999 | `domain/TreatmentDuration.kt`, `FormScreen.kt` | `6ad2897` | "Tem data para terminar" > digitar 7: "Termina em" = início + 6 e a "Data de término" muda | Emulador 2,0× (7 dias → 09/11 com início 03/11); 4 testes. Mudar a data de início depois **não** recalcula o término (mantém a data e o número muda) |
| P21 | CC-22 | Ajustes > Sobre: "Versão 1.0.25" (lida do app) e bloco "Privacidade e dados" com o texto aprovado pelo autor em 03/11 | `ui/settings/SettingsScreen.kt` | `d7aa8a0` | Ajustes, rolar até Sobre | Emulador 2,0× (texto). A versão é lida de `BuildConfig`; o emulador ainda tinha a 1.0.24 instalada |
| P22 | CC-23 | Lista de Medicamentos em ordem alfabética (sem acento nem maiúsculas, id como desempate); Hoje, alarme e formulário seguem a ordem de cadastro. Busca fora, como pedido | `domain/MedicationOrder.kt`, `ui/medications/MedicationsScreen.kt` | `64e3037` | Cadastrar nomes fora de ordem: a aba Medicamentos os mostra de A a Z | Compila/testes (3 testes). **Não visto em tela** |
| P6 | CC-07 | Depois de "Entendi", aviso de 6 s na Hoje: "Lembrete encerrado. O Genki não registra se o remédio foi tomado." (não sugere ingestão) | `ui/home/HomeScreen.kt` | `7353be1` | Disparar um lembrete e tocar "Entendi" | Compila/testes. **Não visto** |
| P7 | CC-08 | Cartão "Agora" diz "Sem resposta, o Genki avisa de novo em N min (até 3 vezes)." quando a repetição está ligada em Ajustes | `HomeScreen.kt` | `7353be1` | Lembrete tocando, repetição ligada | Compila/testes. **Não visto**. Não mostra a hora exata da repetição (a lógica de lembretes não foi tocada) |
| P8 | CC-09 | O horário que está no cartão "Agora" ou adiado não conta nem aparece entre os "que já passaram" | `HomeScreen.kt` | `7353be1` | Com um aviso em curso, o link "Ver os N horários que já passaram" não conta o dele | Compila/testes. **Não visto** |
| P11 | CC-12 | Texto do pop-up reescrito sem termo técnico e com o aviso de que o nome da opção muda por celular | `ui/intro/IntroScreen.kt` | `7353be1` | Introdução, página 2 | Compila/testes. **Não visto** |
| P12 | CC-13 | Passo 2 do tutorial: "Quando houver remédios cadastrados, aqui aparecem…" (serve para o cartão e para a mensagem vazia) | `ui/tutorial/Tutorial.kt` | `7353be1` | Tutorial sem remédios, passo 2 | Compila/testes. **Não visto** |
| P15 | CC-16 | "Silenciar a voz": borda mais escura (1,5 dp, cor `outline`) e o piscar desce a 0,5 (era 0,3) | `Components.kt` | `7353be1` | Voz, botão com leitura ligada | **Não visto depois da mudança** (a captura do emulador é anterior) |
| P16 | CC-17 | Opção marcada na grade (dias específicos) ganha borda verde de 2 dp, além do ✓ e do fundo | `Components.kt` (`ChoiceCell`) | `7353be1` | Editar > Dias específicos | Compila/testes. **Não visto** |
| P23 | CC-24 | "Ativar assistente de voz" vira "Ler o tutorial em voz alta", com a descrição só do que ele faz | `ui/settings/SettingsControls.kt` | `7353be1` | Ajustes > Voz e introdução, página 1 | Compila/testes. **Não visto** |
| P27 | CC-28 | Sem concentração, revisão e Detalhes dizem "Concentração: Não informada" | `FormScreen.kt`, `ui/details/DetailScreen.kt` | `7353be1` | Cadastrar sem concentração | Emulador (revisão). Detalhes não visto |
| P28 | CC-29 | Balão do tutorial ganha "Voltar" a partir do passo 2 | `Tutorial.kt` | `7353be1` | Tutorial, passo 2 em diante; testar com fonte 2,0× (três botões na linha) | Compila/testes. **Não visto** |
| P29 | CC-30 | Cartão do horário (etapa 2) ganha o ícone de lápis à direita | `FormScreen.kt` | `7353be1` | Etapa 2, com um horário na lista | Compila/testes. **Não visto** |
| P3 | CC-04 | Com fonte ≥ 1,5×, o relógio abre direto em "Digitar o horário" (`TYPED_TIME_FONT_SCALE`); "Escolher no relógio" continua disponível | `ui/components/TimeDialog.kt` | `60400a9` | Fonte 2,0×: Adicionar horário | Emulador 2,0×; 1 teste |
| P9 | CC-10 | Introdução, página 2, sem a permissão: "Permitir notificações" é o botão cheio e o de baixo vira "Começar sem permitir" (secundário); com a permissão, "Começar" volta a ser o principal | `IntroScreen.kt` | `60400a9` | Instalação limpa ou dados limpos | Compila/testes. **Não visto** |
| P10 | CC-11 | Introdução, página 2: frase quando o celular não permite alarmes exatos ("o lembrete pode atrasar alguns minutos"). A Hoje já avisava (`PermissionWarning`) | `IntroScreen.kt` | `60400a9` | Aparelho com "Alarmes e lembretes" negado | Compila/testes. **Não visto** (o emulador tem a permissão) |
| P13 | CC-14 | "Desfazer" da exclusão fica 15 s (era ~10 s), constante `UNDO_MILLIS`; **proposta, o autor ajusta** (a DEC-055 dizia 10 s) | `ui/navigation/GenkiApp.kt` | `60400a9` | Excluir e esperar | Compila/testes. **Não visto**; o toque dentro do prazo não foi testado |
| P18 | CC-19 | "Adicionar horário" abre no primeiro horário cheio livre a partir de 08:00 (09:00 se 08:00 já existe), em vez do erro "já foi adicionado" | `domain/NewTimeSuggestion.kt`, `FormScreen.kt` | `60400a9` | Etapa 2 com 08:00 na lista | Compila/testes (5 testes). **Não visto** |
| P24 | CC-25 | Campo do nome quebra em até 3 linhas (mostrava só o fim do texto). O limite de tamanho do nome não mudou (o validador não foi tocado) | `FormScreen.kt` | `60400a9` | Digitar um nome longo | Compila/testes. **Não visto** |
| P25 | CC-26 | No cartão "Próximo lembrete" (e no do próximo dia), tocar no remédio abre os Detalhes; o cartão inteiro não é tocável | `HomeScreen.kt` | `60400a9` | Hoje com remédio cadastrado, tocar no nome dentro do cartão verde | Compila/testes. **Não visto** |
| P26 | CC-27 | Dias com inicial maiúscula ("Segunda e sexta") em lista, Detalhes e revisão; separador "·" também na lista de Medicamentos. **Não mexido:** chips abreviados (Seg, Sex) e "ml" no menu de unidade (são o formato do app) | `ui/MedicationText.kt`, `DetailScreen.kt`, `FormScreen.kt` | `60400a9` | Aba Medicamentos e Detalhes | Compila/testes (1 teste novo). **Não visto** |

## Correção depois da instalação no A31 (autor, 03/11)

Com fonte 2,0×, o balão do tutorial passava da tela e os botões ficavam inacessíveis (visto no A31 por captura de tela; piorou com o "Voltar" do P28 e o texto maior do P12). Corrigido em `ui/tutorial/Tutorial.kt`: o texto do balão e da pergunta inicial rola dentro de uma área limitada, com o indicador "ver mais" (`ScrollHintColumn(fill = false)`); os botões ficam sempre visíveis e quebram de linha (`FlowRow`); o balão respeita a barra de navegação e a barra de status. **Verificado no A31 (SM-A315G, fonte 2,0×, instalação limpa):** pergunta inicial e os 7 passos do tutorial com todos os botões à vista. Mesmo APK 1.0.25 (versionCode 26), reconstruído.

## Introdução em páginas dinâmicas (pedido do autor, 03/11)

Com fonte 200% a introdução (2 páginas) exigia rolar muito. Agora o conteúdo são 4 blocos (boas-vindas, "Tudo fica só neste celular" + voz, som do lembrete, permissão) e o app **mede** cada bloco com a fonte atual (`SubcomposeLayout`, `ui/intro/IntroScreen.kt`) e junta em cada página os que cabem sem rolar (`ui/intro/IntroPagination.kt`, função pura com 5 testes). "N de M", "Pular", "Continuar" e "Começar" seguem o número de páginas. **Visto no emulador (API 35):** fonte 1,0× = 2 páginas; 1,3× = 3; 2,0× = 4. No 2,0× o bloco do som do lembrete ainda passa um pouco da tela e rola, com o indicador "ver mais". **Não visto no A31** (a introdução só aparece na primeira abertura).

## Ajustes pedidos pelo autor depois (03/11)

- **Som do lembrete mais curto:** "Como alarme" agora diz só "Toca como despertador, mesmo no silencioso." (saíram "até 1 minuto", "tela bloqueada" e "sem resposta, a repetição toca de novo"); o alarme falado diz "O Genki diz o horário e o nome do remédio enquanto o alarme toca." Vale para a introdução e para Ajustes (`ui/settings/SettingsControls.kt`).
- **Alarme falado na introdução:** ao escolher "Como alarme", aparece abaixo a opção "Falar o nome dos remédios no alarme" (como em Ajustes). É um bloco à parte: no 2,0× vai para a página seguinte (5 páginas com o alarme escolhido).
- **Adicionar medicamento:** removido o título "Como você quer cadastrar?"; com fonte 2,0× os três cartões cabem sem rolar (emulador). A rolagem fica só como reserva, com o indicador.

- **Barra de baixo e botão da Hoje (fonte grande):** os três rótulos da barra têm o mesmo tamanho, o maior em que "Medicamentos" cabe (antes só ele encolhia); "Adicionar medicamento" fica numa linha só (, texto encolhe só o necessário). Substitui a tentativa revertida do P4.
- **Alarme falado na mesma página do som:** com "Como alarme", a opção aparece logo abaixo, na mesma página, em todos os tamanhos de fonte; para caber, "Como notificação" diz só "Respeita o modo silencioso." e o alarme falado, "Diz o horário e o nome do remédio." (vale também em Ajustes). Visto no emulador com fonte 2,0×.

## Não corrigidos

| P | CC | Motivo |
| --- | --- | --- |
| P4 | CC-05 | Tentei subir o piso do rótulo da barra de 8 sp para 11 sp: com fonte 2,0× "Medicamentos" virou "Medicamen…" (visto no emulador), pior que o texto pequeno. Revertido (`bd78b36`). O espaço do botão fixo e da barra continua ocupando cerca de ¼ da tela; resolver pede decisão de layout (ex.: tirar o botão fixo com fonte grande) e conferência no A31. O indicador de rolagem (P1) não foi aplicado à Hoje |
| P5 | CC-06 | Pedir revisão na edição é fluxo novo (~2–3 h, risco médio). Autor decidiu não corrigir (03/11). Limitação: a edição grava direto em "Salvar alterações" |
| P14 | CC-15 | Não reproduzido no código: a abertura da voz mostra o exemplo sempre (`Intro`), e as capturas `cap-F2-03` e `cap-F2-05` também o mostram. O caso "Não entendi" com fala real não foi visto. Nada alterado |
| P17 | CC-18 | Voltar da voz/formulário vai para a Hoje porque a escolha sai da pilha (`popUpTo`) e `onClose` serve a Cancelar, Descartar e Voltar; mexer arrisca o tutorial e o pós-salvar (~1–2 h). Autor decidiu não corrigir (03/11) |

## O que o autor precisa decidir ou conferir

- Limite de 20 do aviso de quantidade (P19) e prazo de 15 s do Desfazer (P13): valores **propostos**, constantes únicas.
- P1 a P3, P19, P20, P21 e P27 foram vistos no emulador; **os demais só compilam e passam nos testes**. No A31: fonte 2,0× (P1, P2, P3), alarme real (P6, P7, P8), instalação limpa (P9, P10, P11), TalkBack (indicador de rolagem, "Voltar" do tutorial, lápis do horário).
- Decisões implícitas a aceitar: P8 tira o horário em curso da lista "já passaram"; P9 troca o botão principal da introdução enquanto a permissão não for dada; P26 não mexe nos chips abreviados.
- README e guia de instalação ainda citam a 1.0.24; tag e Release `v1.0.25` não criadas, nada enviado ao GitHub.
- O Codex revisa depois (papel de revisor, DEC-026).

APK: `app/build/outputs/apk/release/app-release.apk` (30,4 MB, assinado, certificado SHA-256 `48c13c88…e23c`; arquivo SHA-256 `889e0994…c935`); cópia em `Desktop/Genki-teste/Genki-1.0.25-teste.apk`. Não instalado no A31.
