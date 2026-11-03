# Genki

**Aplicativo móvel para gerenciamento de medicamentos com cadastro assistido por voz e foco em acessibilidade**

| | |
| --- | --- |
| **Instituição** | Instituto Federal de Educação, Ciência e Tecnologia do Rio Grande do Sul (IFRS) — Campus Canoas |
| **Curso** | Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas |
| **Trabalho** | Trabalho de Conclusão de Curso (TCC) |
| **Autor** | Ary Façanha de Sá Neto |
| **Orientador** | Prof. Dr. Sandro José Ribeiro da Silva |
| **Local e ano** | Canoas, 2026 |
| **Versão do aplicativo** | **1.0.23**, congelada em 03/11/2026 para as avaliações do trabalho |
| **Plataforma** | Android 8.0 ou superior |

## Sumário

1. [Instalação rápida](#1-instalação-rápida)
2. [Sobre o trabalho](#2-sobre-o-trabalho)
3. [Funcionalidades](#3-funcionalidades)
4. [Metodologia e estado do trabalho](#4-metodologia-e-estado-do-trabalho)
5. [Tecnologias e arquitetura](#5-tecnologias-e-arquitetura)
6. [Limitações](#6-limitações)
7. [Dados e privacidade](#7-dados-e-privacidade)
8. [Executar a partir do código](#8-executar-a-partir-do-código)
9. [Documentação do projeto](#9-documentação-do-projeto)
10. [Uso de inteligência artificial](#10-uso-de-inteligência-artificial)

## 1. Instalação rápida

O Genki é instalado por um arquivo **APK** (o instalador de aplicativos do Android), sem passar pela Play Store. Não é
preciso computador, conta ou Android Studio.

1. **Baixe o APK no celular.** Use o arquivo `Genki-1.0.23.apk` da página [Releases](https://github.com/aryfacanha/genki/releases)
   deste repositório ou o arquivo enviado pelo autor.
2. **Abra o arquivo baixado** (aplicativo **Arquivos** ou **Meus Arquivos** → **Downloads**).
3. **Permita a instalação por essa origem**, se o Android pedir: toque em **Configurações** e ative **Permitir desta
   fonte** para o navegador ou gerenciador de arquivos que abriu o APK. Depois volte e toque em **Instalar**.
4. **Ignore o aviso do Play Protect e instale mesmo assim** (veja o destaque abaixo).
5. Toque em **Abrir** ou procure **Genki** na lista de aplicativos.

> [!IMPORTANT]
> **Aviso do Play Protect: o passo mais importante.** O APK é assinado com a chave do autor, mas **não é distribuído
> nem verificado pela Play Store**. Por isso o Google Play Protect mostra um aviso de aplicativo desconhecido ou
> bloqueado. **Não é preciso desativar o Play Protect.** No próprio aviso:
>
> 1. toque em **Mais detalhes** (ou na seta ao lado do texto);
> 2. toque em **Instalar mesmo assim**.
>
> Se a opção **Instalar mesmo assim** não aparecer, ela está escondida em **Mais detalhes**. Os nomes podem variar
> conforme o modelo do celular.

Passo a passo completo, permissões, um teste de 10 a 15 minutos e o que fazer se algo falhar: [guia de instalação e
teste](docs/GUIA_INSTALACAO_TESTE.md). Use **dados fictícios** ao experimentar.

## 2. Sobre o trabalho

O gerenciamento de medicamentos pode ser complexo para pessoas idosas, sobretudo com doenças crônicas, vários
fármacos e horários específicos. O Genki propõe um aplicativo de cadastro e organização de medicamentos e lembretes
voltado principalmente a esse público, em que a **interação por voz** é a funcionalidade central, com alternativas por
toque e texto e conferência de todos os dados antes de salvar. O reconhecimento de embalagens pela câmera é uma
**prova de conceito** de apoio ao preenchimento.

**Questão de pesquisa.** Como desenvolver e avaliar a usabilidade de um aplicativo móvel multimodal para o cadastro e
o gerenciamento de medicamentos por pessoas idosas, com ênfase na interação por voz?

**Objetivo geral.** Desenvolver e avaliar um aplicativo móvel orientado por princípios de acessibilidade para o
cadastro e o gerenciamento de medicamentos e lembretes, destinado principalmente a pessoas idosas, com interação
multimodal e ênfase no cadastro assistido por voz.

**Objetivos específicos** (conforme a [proposta aprovada](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view)):

1. identificar, por meio de revisão bibliográfica, dificuldades, necessidades e requisitos relacionados ao
   gerenciamento de medicamentos por pessoas idosas;
2. levantar recomendações de usabilidade, acessibilidade e design centrado no usuário aplicáveis à solução;
3. projetar uma interface móvel simplificada e multimodal para o cadastro e o gerenciamento de medicamentos;
4. implementar funcionalidades de cadastro, consulta, edição e exclusão de medicamentos, configuração de horários e
   emissão de lembretes;
5. implementar um fluxo de cadastro assistido por voz, com apresentação, confirmação e correção das informações
   reconhecidas;
6. desenvolver e verificar uma prova de conceito de reconhecimento por imagem como apoio ao preenchimento do cadastro;
7. avaliar o fluxo de cadastro por voz quanto ao tempo de resposta, ao preenchimento correto dos campos e à
   necessidade de correção das informações reconhecidas;
8. revisar a interface desenvolvida com base em heurísticas gerais de usabilidade e recomendações aplicáveis a
   aplicativos móveis de saúde;
9. avaliar a usabilidade do produto mínimo viável por meio de tarefas representativas e da versão brasileira da
   System Usability Scale (SUS).

**Escopo.** O Genki é uma ferramenta de apoio à organização de tratamentos. Ele não prescreve, não calcula doses, não
avalia interações medicamentosas, não confirma a ingestão de medicamentos e não substitui profissionais de saúde. O
trabalho não pretende comprovar melhora clínica da adesão.

## 3. Funcionalidades

| Recurso | O que permite fazer |
| --- | --- |
| Cadastro por voz | Dizer nome, concentração, quantidade, dias, horários e duração, inclusive de vários remédios de uma vez; conferir os nomes entendidos; responder só ao que faltou; corrigir por voz, toque ou digitação antes de salvar. |
| Cadastro digitando | Formulário em três etapas e revisão final, com quantidade por toque (− e +, incluindo meio e 1/4 de comprimido). |
| Câmera (experimental) | Fotografar uma embalagem e conferir as sugestões de nome e concentração lidas por OCR. Nunca preenche dose, dias, horários ou duração. |
| Gerenciamento | Consultar, editar e excluir medicamentos (com **Desfazer**); ativar e desativar lembretes. |
| Hoje | Agenda do dia, próximo lembrete e avisos pendentes. |
| Lembretes | Notificação ou alarme em tela cheia, com **Entendi** e **Adiar**; repetição opcional sem resposta; alarme falado opcional com o horário e o nome dos remédios. |
| Apoio ao uso | Introdução, tutorial guiado, leitura em voz alta das perguntas do cadastro por voz e textos em linguagem simples. |

**Entendi significa apenas que o aviso foi visto.** O aplicativo não registra que o medicamento foi tomado e não
mantém histórico de tomadas.

## 4. Metodologia e estado do trabalho

Pesquisa aplicada, de caráter exploratório, com desenvolvimento tecnológico e avaliação de usabilidade por dados
quantitativos e qualitativos, organizada em desenvolvimento da solução, avaliação da solução e aspectos éticos.

| Etapa (proposta) | Situação em 03/11/2026 |
| --- | --- |
| Requisitos e protótipo navegável | Concluídos; o [protótipo](https://joyful-biscuit-677f74.netlify.app) foi a referência de interface |
| Produto mínimo viável (cadastro, lembretes, voz e câmera) | Implementado e **congelado na versão 1.0.23** |
| Verificação técnica da voz e da câmera | Prevista após o congelamento |
| Revisão heurística da interface | Prevista após o congelamento |
| Avaliação de usabilidade com tarefas e SUS | Condicionada aos procedimentos éticos aplicáveis |

Durante o desenvolvimento, o código passou por testes automatizados (187 testes unitários na versão 1.0.23), revisões
técnicas independentes e testes manuais em um Samsung Galaxy A31 (Android 12). Esses registros são evidências de
desenvolvimento, **não resultados das avaliações do trabalho**, que serão apresentados na monografia.

## 5. Tecnologias e arquitetura

Android nativo com **Kotlin** e **Jetpack Compose** (Material 3), arquitetura em camadas de interface e dados com
**ViewModel** e repositório, e armazenamento local com **Room** (SQLite). Lembretes com AlarmManager e notificações do
Android; voz com **SpeechRecognizer** e **TextToSpeech**; câmera com **CameraX** e **ML Kit Text Recognition**
(modelo embarcado). O interpretador da fala usa regras locais, sem LLM e sem servidor.

```text
app/src/main/java/br/edu/ifrs/genki/
├── data/       Banco Room, repositório e configurações
├── domain/     Regras de domínio e validação (DraftValidator)
├── reminders/  Agendamento, notificações e alarme
├── voice/      Reconhecimento, interpretação e fluxo por voz
├── camera/     Captura e reconhecimento de texto
└── ui/         Telas e componentes de interface
app/src/test/   Testes unitários
docs/           Guia de instalação, especificação e registros do desenvolvimento
```

Todo cadastro, por voz, câmera ou digitação, passa por um rascunho comum e pelo `DraftValidator` antes de ser gravado.
A [especificação técnica](docs/ESPEC_ARQUITETURA.md) descreve a arquitetura; as [decisões](docs/DECISOES.md)
registram os ajustes posteriores.

## 6. Limitações

- **Voz:** depende do serviço de reconhecimento de fala do celular e de permissão para o microfone. Pode trocar nomes
  ou cortar falas; a conferência final é obrigatória. O serviço do Android pode usar internet; não há garantia de
  reconhecimento offline em todos os aparelhos.
- **Câmera:** prova de conceito. Luz, foco e texto da embalagem afetam o resultado; as sugestões sempre precisam de
  revisão.
- **Lembretes:** dependem das permissões, do volume, do modo Não perturbe e das restrições de bateria do Android. Não
  tocam com o celular desligado.
- **Programação:** horários definidos e dias da semana; esquemas como "dia sim, dia não" não são interpretados.
- **Compatibilidade:** verificado principalmente no Samsung Galaxy A31; sem versão para iPhone; acessibilidade com
  leitor de tela (TalkBack) não verificada por completo.

## 7. Dados e privacidade

Os cadastros ficam **somente no celular**. O Genki não exige conta, não tem servidor nem sincronização própria, não
exibe anúncios, não tem permissão de internet e não guarda áudio nem fotos. O backup do Android está desligado para o
aplicativo: desinstalar, limpar os dados ou trocar de celular apaga os cadastros, e não há exportação. Para
experimentar, use dados fictícios.

## 8. Executar a partir do código

Requer Android Studio com o SDK indicado pelo Gradle (`compileSdk`/`targetSdk` 37, `minSdk` 26). Na raiz do
repositório (PowerShell):

```powershell
.\gradlew.bat assembleDebug test   # compila e roda os testes
.\gradlew.bat installDebug         # instala em emulador ou aparelho conectado
```

O APK de desenvolvimento fica em `app/build/outputs/apk/debug/`. O APK de distribuição é assinado com a chave do
autor, mantida fora do repositório; um APK debug não atualiza uma instalação feita com o APK de distribuição.

## 9. Documentação do projeto

| Documento | Conteúdo |
| --- | --- |
| [Guia de instalação e teste](docs/GUIA_INSTALACAO_TESTE.md) | Instalação do APK, permissões, teste rápido, limitações e como dar retorno |
| [Proposta aprovada](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view) | Marco acadêmico do trabalho |
| [Requisitos](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit) e [mapa](docs/REQUISITOS_MAPA.md) | RF01–RF25 e RNF01–RNF11 e sua relação com o código |
| [Especificação técnica](docs/ESPEC_ARQUITETURA.md) | Arquitetura, dados e regras do aplicativo |
| [Estado](docs/ESTADO.md), [decisões](docs/DECISOES.md) e [handoff técnico](docs/HANDOFF_APP.md) | Situação do projeto, escolhas do autor e diário técnico |
| [Revisões](docs/revisoes/) e [entregas](docs/ENTREGAS.md) | Revisões técnicas independentes e rastreabilidade do desenvolvimento |

Documentos no Google Drive podem exigir permissão de acesso.

## 10. Uso de inteligência artificial

O desenvolvimento contou com assistentes de IA generativa (Claude Code e Codex no código; ChatGPT e Claude no apoio ao
texto), conforme a Instrução Normativa IFRS nº 16/2026. As sugestões dos assistentes foram revisadas e aceitas pelo
autor antes de serem incorporadas, e cada uso está registrado em [registro de IA](docs/REGISTRO_IA.md). A declaração
completa constará na monografia. O aplicativo em si não usa IA generativa.
