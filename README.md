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
| **Versão do aplicativo** | **1.0.25**. A 1.0.23, congelada em 03/11/2026, é a versão das avaliações do trabalho; a 1.0.24 corrigiu um problema encontrado nelas (lembrete adiado apagado ao editar o medicamento) e a 1.0.25 refina a interface a partir da revisão heurística (textos, fonte ampliada, avisos e ordem alfabética), sem mudar os lembretes |
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

O Genki é instalado por um arquivo **APK**, sem passar pela Play Store. Não é preciso computador nem conta.

1. **Baixe no celular** o arquivo `Genki-1.0.25.apk`, da página [Releases](https://github.com/aryfacanha/genki/releases)
   ou enviado pelo autor.
2. **Abra o arquivo** (aplicativo **Arquivos** ou **Meus Arquivos** → **Downloads**).
3. Se o Android pedir, toque em **Configurações**, ative **Permitir desta fonte** e volte. Toque em **Instalar**.
4. **No aviso do Play Protect, toque em Mais detalhes → Instalar mesmo assim** (veja abaixo).
5. Toque em **Abrir** ou procure **Genki** na lista de aplicativos.

> [!IMPORTANT]
> **Aviso do Play Protect: o passo mais importante.** O APK é assinado pelo autor, mas **não é distribuído nem
> verificado pela Play Store**, por isso o Play Protect avisa que o aplicativo é desconhecido. Não é preciso desativar
> o Play Protect: toque em **Mais detalhes** (ou na seta ao lado do texto) e depois em **Instalar mesmo assim**. Os
> nomes podem variar conforme o celular.

Permissões, um teste de 10 a 15 minutos e soluções para problemas comuns: [guia de instalação e
teste](docs/GUIA_INSTALACAO_TESTE.md). Ao experimentar, use **dados fictícios**.

## 2. Sobre o trabalho

O gerenciamento de medicamentos pode ser complexo para pessoas idosas, sobretudo com doenças crônicas, vários fármacos
e horários específicos. O Genki é um aplicativo de cadastro e organização de medicamentos e lembretes voltado
principalmente a esse público. A **interação por voz** é a funcionalidade central, com alternativas por toque e texto e
conferência de todos os dados antes de salvar. A leitura de embalagens pela câmera é uma prova de conceito.

**Questão de pesquisa.** Como desenvolver e avaliar a usabilidade de um aplicativo móvel multimodal para o cadastro e
o gerenciamento de medicamentos por pessoas idosas, com ênfase na interação por voz?

**Objetivo geral.** Desenvolver e avaliar um aplicativo móvel orientado por princípios de acessibilidade para o
cadastro e o gerenciamento de medicamentos e lembretes, destinado principalmente a pessoas idosas, com interação
multimodal e ênfase no cadastro assistido por voz.

**Objetivos específicos** (proposta aprovada):

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

**Escopo.** O Genki apoia a organização de tratamentos. Não prescreve, não calcula doses, não avalia interações
medicamentosas, não confirma a ingestão e não substitui profissionais de saúde.

## 3. Funcionalidades

| Recurso | O que permite fazer |
| --- | --- |
| Cadastro por voz | Dizer os dados de um ou mais remédios, responder só ao que faltou e corrigir antes de salvar. |
| Cadastro digitando | Formulário em três etapas, com quantidade por toque (incluindo meio e 1/4 de comprimido). |
| Câmera (experimental) | Sugerir nome e concentração a partir da foto da embalagem, sempre para conferência. |
| Gerenciamento | Consultar, editar e excluir medicamentos (com **Desfazer**); ativar e desativar lembretes. |
| Lembretes | Notificação ou alarme, com **Entendi** e **Adiar**; repetição e alarme falado opcionais. |
| Apoio ao uso | Introdução, tutorial guiado e leitura em voz alta das perguntas do cadastro por voz. |

**Entendi significa apenas que o aviso foi visto.** O aplicativo não registra tomadas.

## 4. Metodologia e estado do trabalho

Pesquisa aplicada, de caráter exploratório, com desenvolvimento tecnológico e avaliação de usabilidade.

| Etapa | Situação em 03/11/2026 |
| --- | --- |
| Requisitos e protótipo navegável | Concluídos ([protótipo](https://joyful-biscuit-677f74.netlify.app)) |
| Produto mínimo viável | Implementado; **congelado na 1.0.23** para as avaliações; corrigido na **1.0.24** e refinado na **1.0.25** |
| Verificação técnica da voz e da câmera | Prevista |
| Revisão heurística da interface | Prevista |
| Avaliação de usabilidade com tarefas e SUS | Condicionada aos procedimentos éticos |

O desenvolvimento contou com testes automatizados (226 na versão 1.0.25), revisões técnicas e testes manuais em um
Samsung Galaxy A31 (Android 12). Os resultados das avaliações serão apresentados na monografia.

## 5. Tecnologias e arquitetura

Android nativo com **Kotlin**, **Jetpack Compose** (Material 3), **ViewModel** e **Room**. Lembretes com AlarmManager
e notificações; voz com **SpeechRecognizer** e **TextToSpeech**; câmera com **CameraX** e **ML Kit Text Recognition**.
A interpretação da fala usa regras locais, sem servidor.

```text
app/src/main/java/br/edu/ifrs/genki/
├── data/       Banco, repositório e configurações
├── domain/     Regras e validação (DraftValidator)
├── reminders/  Agendamento, notificações e alarme
├── voice/      Reconhecimento e fluxo por voz
├── camera/     Captura e leitura de texto
└── ui/         Telas e componentes
```

Todo cadastro, por voz, câmera ou digitação, passa pelo `DraftValidator` antes de ser gravado.

## 6. Limitações

- **Voz:** depende do reconhecimento de fala do celular; pode errar nomes, e o funcionamento offline não é garantido.
- **Câmera:** prova de conceito; luz e foco afetam a leitura.
- **Lembretes:** dependem das permissões e ajustes do Android; não tocam com o celular desligado.
- **Programação:** horários e dias da semana definidos; esquemas como "dia sim, dia não" não são interpretados.
- **Compatibilidade:** testado principalmente no Samsung Galaxy A31; sem versão para iPhone.

## 7. Dados e privacidade

Os cadastros ficam **somente no celular**: sem conta, servidor, sincronização, anúncios ou permissão de internet, e
sem guardar áudio ou fotos. O backup do Android está desligado; desinstalar ou limpar os dados apaga os cadastros.

## 8. Executar a partir do código

Com o Android Studio e o SDK indicado pelo Gradle, na raiz do repositório (PowerShell):

```powershell
.\gradlew.bat assembleDebug test   # compila e roda os testes
.\gradlew.bat installDebug         # instala em emulador ou aparelho conectado
```

O APK de distribuição é assinado com a chave do autor, mantida fora do repositório.

## 9. Documentação do projeto

| Documento | Conteúdo |
| --- | --- |
| [Guia de instalação e teste](docs/GUIA_INSTALACAO_TESTE.md) | Instalação, permissões, teste rápido e como dar retorno |
| [Especificação técnica](docs/ESPEC_ARQUITETURA.md) e [mapa de requisitos](docs/REQUISITOS_MAPA.md) | Arquitetura e relação dos requisitos com o código |
| [Estado](docs/ESTADO.md) e [decisões](docs/DECISOES.md) | Situação do projeto e escolhas do autor |

A proposta e a planilha de requisitos oficiais estão disponíveis com o autor.

## 10. Uso de inteligência artificial

O desenvolvimento contou com assistentes de IA generativa (Claude Code e Codex no código; ChatGPT e Claude no apoio ao
texto), conforme a Instrução Normativa IFRS nº 16/2026. As sugestões foram revisadas e aceitas pelo autor, e os usos
estão no [registro de IA](docs/REGISTRO_IA.md). O aplicativo em si não usa IA generativa.
