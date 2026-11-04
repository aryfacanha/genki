# Genki — guia básico de instalação e teste

Este guia acompanha o APK de teste do Genki, aplicativo Android desenvolvido por **Ary Façanha de Sá Neto** no TCC de Análise e Desenvolvimento de Sistemas do **IFRS — Campus Canoas**, orientado pelo **Prof. Dr. Sandro José Ribeiro da Silva**.

O objetivo é permitir que você instale, explore e dê retorno sobre o aplicativo. Use **dados fictícios**, sem alterar um tratamento real. O Genki organiza cadastros e lembretes; não recomenda medicamentos ou doses e não confirma que alguém tomou um remédio.

## 1. Instalar no celular

Você precisa de um **celular com Android 8.0 ou superior** e do arquivo **`Genki-1.0.25.apk`**, enviado pelo autor ou baixado da página [Releases](https://github.com/aryfacanha/genki/releases) do repositório. Não é necessário computador, conta no Genki ou Android Studio. O APK atual é destinado a celulares com processador ARM; não funciona no iPhone.

1. No celular, baixe o arquivo `.apk`. APK é o arquivo de instalação do aplicativo.
2. Toque no arquivo baixado. Se necessário, procure-o no aplicativo **Arquivos / Meus Arquivos → Downloads**.
3. Se o Android pedir autorização para instalar por essa origem, toque em **Configurações** e habilite **Permitir desta fonte** para o aplicativo que abriu o arquivo, como o navegador ou o gerenciador de arquivos. Os nomes variam entre celulares; essa autorização é específica da origem, conforme a [documentação do Android](https://developer.android.com/studio/publish).
4. Volte à instalação e toque em **Instalar**.
5. **Se aparecer o aviso do Play Protect, toque em Mais detalhes e depois em Instalar mesmo assim** (veja o destaque abaixo).
6. Ao terminar, toque em **Abrir** ou procure **Genki** na lista de aplicativos. Depois, você pode desativar novamente **Permitir desta fonte**.

> [!IMPORTANT]
> **Aviso do Play Protect: o passo mais importante.** O APK é assinado com a chave do autor, mas **não é distribuído nem verificado pela Play Store**. Por isso o Google Play Protect mostra um aviso de aplicativo desconhecido ou bloqueado. **Não é preciso desativar o Play Protect.** No próprio aviso:
>
> 1. toque em **Mais detalhes** (ou na seta ao lado do texto);
> 2. toque em **Instalar mesmo assim**.
>
> A opção **Instalar mesmo assim** fica escondida até tocar em **Mais detalhes**. Os nomes podem variar conforme o modelo do celular.

Se aparecer **“Aplicativo não instalado”**, outro bloqueio de segurança ou incompatibilidade, envie ao autor a mensagem exibida e o modelo do celular. Se já tiver o Genki, não o desinstale para tentar resolver: isso pode apagar seus cadastros.

**Para atualizar:** abra o novo APK enviado pelo autor e aceite a atualização por cima da instalação existente. Atualizações compatíveis, assinadas com a mesma chave, são preparadas para preservar os dados. Ao concluir, abra o Genki e confira os cadastros e os próximos lembretes. Não use **Limpar dados** nem desinstale para atualizar.

## 2. Na primeira abertura

Siga a apresentação do aplicativo e aceite o tutorial se quiser conhecer as telas. Você pode revê-lo em **Ajustes → Ver o tutorial**. As três abas principais são **Hoje**, **Medicamentos** e **Ajustes**.

Para testar os lembretes, entre em **Ajustes → Permissões** e confira:

| Permissão | Para que serve |
| --- | --- |
| Notificações | Permite mostrar os lembretes. |
| Alarmes no horário exato, quando aparecer | Permite agendar o aviso com maior precisão; sem ela, pode haver atraso. O botão abre o ajuste do Android. |
| Tela de alarme, quando aparecer | Permite mostrar a tela de alarme sobre a tela bloqueada. É relevante ao escolher **Como alarme**, especialmente no Android 14 ou superior. |

Microfone e câmera são solicitados ao usar esses recursos. Você pode testar o cadastro por **Digitar** sem conceder essas duas permissões.

Em **Ajustes → Som do lembrete**, escolha **Como notificação** ou **Como alarme**. A notificação respeita o silencioso; o alarme usa o volume de alarme do celular e toca por até um minuto. Confira o volume correspondente e o modo Não perturbe antes do teste.

## 3. Um teste rápido

Reserve cerca de **10 a 15 minutos**. Os dados abaixo são apenas exemplos de cadastro, sem orientação de uso de medicamentos.

### Cadastrar e receber um lembrete

1. Toque em **Adicionar medicamento → Digitar**.
2. Use o nome fictício **Teste manual**, deixe a concentração vazia e escolha **1 comprimido** como quantidade por uso.
3. Escolha todos os dias e um horário **três a cinco minutos à frente da hora atual**. Para facilitar, faça o teste sem atravessar a meia-noite. Escolha uso contínuo apenas para este cadastro fictício.
4. Confira o resumo e salve. Em **Medicamentos**, abra o cadastro e verifique nome, quantidade, horário e lembretes ativos. Na **Hoje**, confira o próximo lembrete.
5. Saia do Genki e bloqueie a tela. Aguarde o horário escolhido. Se aparecer somente uma notificação, expanda-a ou toque nela para abrir o aplicativo.
6. Toque em **Entendi**. O aviso correspondente deve encerrar. **Isso significa “vi o lembrete”, nunca “tomei o medicamento”.**

Para explorar o adiamento, escolha **Adiar** quando o aviso aparecer: ele deve voltar após o intervalo definido em **Ajustes → Tempo para avisar de novo** (10, 20 ou 30 minutos). Essa espera é opcional e aumenta a duração do teste.

### Experimentar a voz e a correção

1. Toque em **Adicionar medicamento → Usar voz** e permita o microfone.
2. Em um ambiente tranquilo, toque no microfone quando necessário e diga, como exemplo fictício: **“Vitamina C, um comprimido, todos os dias às oito da manhã, uso contínuo.”**
3. Confira os nomes entendidos. Se houver erro, toque no nome para corrigir; depois toque em **Continuar**. Responda às perguntas que aparecerem.
4. Confira os dados antes de salvar. Experimente corrigir um horário e verifique se o resumo acompanha a mudança. Você pode usar os botões de resposta, **Digitar a resposta** ou **Prefiro digitar**.

As perguntas do cadastro por voz são lidas em voz alta ao abrir esse fluxo. **Silenciar a voz** desliga essa leitura naquele cadastro; nesse caso, toque no microfone para responder. A opção **Ativar assistente de voz**, em Ajustes, controla a leitura do tutorial. Se a escuta não começar sozinha, tente o botão do microfone.

### Explorar a câmera, se desejar

Em **Adicionar medicamento → Usar câmera**, permita a câmera e fotografe uma embalagem com texto nítido, boa luz e pouco reflexo. A função é **experimental**: confira e corrija as sugestões de nome e concentração. Ela não deve preencher dose por uso, horários, dias ou duração. Você pode cancelar depois de conferir, sem salvar nada.

**Ao terminar:** em **Medicamentos**, abra e exclua os cadastros fictícios que criou, confirmando a exclusão, ou desative seus lembretes. Se excluir por engano, toque em **Desfazer** no aviso que aparece embaixo da tela (fica cerca de 10 segundos). Assim eles não voltarão a avisar nos dias seguintes. Preserve eventuais cadastros que já existiam.

## 4. Limitações que vale conhecer

- **Voz depende do celular.** É necessário um serviço de reconhecimento de fala configurado e, para a leitura das perguntas, uma voz em português disponível no sistema. O reconhecimento pode trocar nomes, cortar uma fala ou falhar. Frases curtas e a conferência final ajudam; digitar continua disponível.
- **A voz pode precisar de internet.** O Genki prioriza reconhecimento local quando disponível, mas o serviço de fala pode processar áudio externamente. Não há garantia de reconhecimento offline em todo aparelho. Veja a [documentação do serviço de reconhecimento do Android](https://developer.android.com/reference/android/speech/SpeechRecognizer).
- **A câmera apenas auxilia.** Pode não ler a embalagem ou confundir seu nome com outro texto. Concentração, como “500 mg” na embalagem, é diferente da quantidade por uso, como “1 comprimido”.
- **Os avisos dependem do Android.** Celular desligado, permissões bloqueadas, volume, Não perturbe e restrições de bateria podem impedir ou alterar a apresentação. Depois de usar **Forçar parada** nas configurações do Android, reabra o Genki. Tratamentos terminados ou com lembretes desativados não geram novos avisos.
- **Alarme falado é opcional.** Com **Como alarme**, a opção **Falar o nome dos remédios no alarme** faz o Genki dizer o horário e o nome do remédio (ou quantos são) a cada 20 segundos enquanto toca. Vem desligada; o nome é falado em voz alta, então pense em quem está por perto.
- **O som não é contínuo indefinidamente.** No modo alarme, cada disparo toca por até um minuto. Com **Repetir o lembrete** ligado, há até três repetições automáticas no intervalo escolhido; **Adiar** pede um novo aviso naquele intervalo.
- **Nem toda programação é suportada.** Use horários e dias da semana definidos. Expressões como “dia sim, dia não” não são interpretadas automaticamente.
- **Os dados ficam só no aparelho.** O Genki não tem conta ou sincronização própria, não guarda permanentemente áudio ou fotos e não participa do backup do Android nem da transferência para outro celular. Por isso, desinstalar, limpar os dados ou trocar de celular apaga os cadastros; não há exportação ou restauração própria.

O aplicativo está em desenvolvimento acadêmico. A compatibilidade, a voz e a acessibilidade não foram verificadas em todos os celulares. Este roteiro serve à exploração e ao retorno informal; não substitui o protocolo de avaliação da pesquisa.

## 5. Se algo não funcionar

| Situação | O que conferir |
| --- | --- |
| Não chegou lembrete | Horário futuro, dia selecionado, tratamento vigente e lembretes ativos; depois, **Ajustes → Permissões**. |
| Apareceu aviso sem som | Modo escolhido, volume de notificação ou alarme, silencioso e Não perturbe. |
| Voz não escuta ou erra o nome | Permissão de microfone, serviço de reconhecimento e conexão; tente novamente ou digite. |
| Não ouço as perguntas | Volume de mídia, botão **Ouvir as perguntas em voz alta** e voz em português no sistema. |
| A câmera não reconhece | Luz, foco e enquadramento; tente novamente ou use **Digitar**. |

Para dar retorno ao autor, envie: **versão do APK recebido, modelo do celular, versão do Android, o que tentou fazer, o que esperava e o que aconteceu**. Se possível, acrescente uma captura sem dados pessoais. Diga também quais textos, botões ou etapas ficaram confusos — esse retorno é tão útil quanto relatar um erro.

---

Guia preparado em **30/10/2026** e atualizado em **04/11/2026** para a versão **1.0.25**, que refina a interface a partir da revisão heurística da 1.0.23 (versão congelada para as avaliações) e inclui a correção da 1.0.24. O número da versão pode ser consultado nas informações do aplicativo nas configurações do Android. Para conhecer o contexto acadêmico e a documentação técnica, peça ao autor o acesso ao repositório do projeto.
