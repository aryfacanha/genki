# Tela de introdução — proposta para aprovação do autor

28/10/2026, Claude Code (Opus 5.5). Parte 3 da DEC-040. **Aprovada pelo autor em 28/10 com A, B e C (DEC-041) e
implementada no mesmo dia; D fica para depois.** Origem: pedido do autor na bateria do A31 ("tela de introdução na primeira instalação, instruindo
como ele funciona, e logo de cara com uma personalização"), DEC-032 (som como alarme e estilo de pop-up) e DEC-034
(horário do primeiro remédio, anotado como sugestão).

**Fora da proposta do TCC:** a introdução é um acréscimo. A divergência vai para a monografia como refinamento de
usabilidade, sem mudar RF/RNF.

## Onde fica

- **Só direto no app.** O protótipo web está parado desde 26/10 e não mostra som, permissões nem pop-up do Android.
  Registrar na monografia que o protótipo não tem essa tela.
- Aparece **na primeira abertura**. Depois de concluída ou pulada, não volta sozinha.
- Em Ajustes, "Ver a introdução de novo".
- Fica guardada como um ajuste a mais em SharedPreferences (`genki_settings`), como os que já existem. Sem DataStore
  nem banco novo.

## Telas (3 + permissão), todas com "Pular" no topo e "Continuar" embaixo

| # | Título | Conteúdo |
| --- | --- | --- |
| 1 | Bem-vindo ao Genki | "O Genki avisa na hora de tomar seus remédios." "Ele não registra se você tomou e não substitui a orientação do médico ou do farmacêutico." "Tudo fica só neste celular." |
| 2 | Como cadastrar | Três linhas com ícone: **Falar** ("Diga o nome, quanto toma e os horários"); **Digitar**; **Câmera** ("Lê o nome e a concentração da caixa"). "Você sempre confere antes de salvar." |
| 3 | Do seu jeito | Personalização (abaixo), já com o padrão marcado; dá para mudar depois em Ajustes |
| 4 | Permitir avisos | "Para avisar na hora, o Genki precisa mostrar notificações." Botão **Permitir notificações** (pedido do Android). Sem permissão, explica que dá para permitir depois em Ajustes. Termina em **Começar**, que leva à Hoje |

## Tela 3: o que personalizar (para o autor escolher)

| Opção | Escolhas | Padrão | Situação no app |
| --- | --- | --- | --- |
| A. Som do lembrete | "Como notificação" (respeita o silencioso) / "Como alarme" (toca mesmo no silencioso) | Notificação | **Novo** (DEC-032): um segundo canal de notificação com som de alarme |
| B. Avisar de novo se eu não responder | Sim / Não; e depois de 10, 20 ou 30 min | Sim, 20 min | Já existe em Ajustes; a tela só repete |
| C. Na voz, ouvir sozinho depois da pergunta | Sim / Não (tocar no microfone a cada vez) | Sim | **Novo**: a escuta automática da DEC-040 hoje não tem como desligar |
| D. Horário do seu primeiro remédio do dia | Relógio (ex.: 08:00) | Sem resposta | **Novo** (DEC-034): no cadastro, os horários sugeridos (botões da voz e relógio) partem dele |

Recomendação: **A, B e C**. D é menos essencial e mexe nas sugestões da voz e do formulário. Deixar para depois,
a não ser que o autor queira agora.

O tamanho do texto não entra: o app segue a fonte do Android (até 2,0×), conferida na bateria. A tela 1 pode dizer
"Para letras maiores, aumente a fonte nas configurações do celular".

## Fora desta proposta

- Orientar o estilo de pop-up da Samsung (DEC-032). Como cada fabricante faz diferente, fica só uma frase na tela 4:
  "Se o aviso aparecer pequeno, nas configurações de notificação do Genki escolha mostrar como pop-up".
- Tutorial interativo, vídeo ou cadastro de exemplo.

## Verificação prevista

Testes unitários da lógica pura (primeira abertura, ajustes gravados). No emulador e no A31: primeira instalação
limpa, "Pular", fonte 2,0×, TalkBack na sequência das telas, som como alarme com o celular no silencioso.
