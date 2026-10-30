# Genki

**Aplicativo móvel para gerenciamento de medicamentos com cadastro assistido por voz e foco em acessibilidade.**

Trabalho de Conclusão de Curso de **Ary Façanha de Sá Neto**, do Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas do **IFRS — Campus Canoas**, sob orientação do **Prof. Dr. Sandro José Ribeiro da Silva**. Canoas, 2026.

O Genki busca facilitar o cadastro e a organização de medicamentos e lembretes, principalmente para pessoas idosas. A interação por voz é o foco do projeto, com alternativas por toque e texto e conferência dos dados antes de salvar.

**Quer instalar e experimentar?** Consulte o [guia básico de instalação e teste](docs/GUIA_INSTALACAO_TESTE.md). Para instalar, solicite ao autor o APK de teste que acompanha o guia; não é necessário Android Studio.

## O projeto

A questão de pesquisa é como desenvolver e avaliar a usabilidade de um aplicativo móvel multimodal para o cadastro e o gerenciamento de medicamentos por pessoas idosas, com ênfase na interação por voz.

O trabalho contempla um produto mínimo viável (MVP), a verificação técnica dos recursos de voz e imagem e a avaliação de usabilidade. A [proposta aprovada](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view) é o marco acadêmico do projeto. O aplicativo não prescreve tratamentos, não comprova melhora clínica da adesão e não confirma a ingestão de medicamentos.

## Funcionalidades implementadas

| Recurso | O que permite fazer |
| --- | --- |
| Cadastro manual | Informar nome, concentração opcional, quantidade por uso, horários, dias da semana e duração do tratamento. |
| Cadastro assistido por voz | Dizer os dados, conferir os nomes reconhecidos, responder às perguntas sobre informações faltantes e corrigir antes de salvar. |
| Gerenciamento | Consultar, editar e excluir medicamentos; ativar e desativar lembretes. |
| Agenda do dia | Ver horários, próximo lembrete e avisos pendentes na tela Hoje. |
| Lembretes locais | Receber notificação ou alarme, reconhecer o aviso com **Entendi** e adiá-lo. |
| Câmera experimental | Fotografar uma embalagem e revisar sugestões de nome e concentração extraídas por OCR. |
| Apoio à interação | Tutorial guiado, leitura das perguntas do cadastro por voz e alternativas por toque ou digitação. |

**Entendi significa apenas que o aviso foi visto.** O aplicativo não mantém histórico de tomadas. A câmera nunca preenche dose de uso, horários, dias ou duração do tratamento.

## Estado e verificação

Em **30/10/2026**, a versão registrada é a **1.0.19**, candidata ao congelamento. Os marcos de cadastro, lembretes, voz e câmera estão implementados. O [handoff técnico](docs/HANDOFF_APP.md) registra compilação, 180 testes automatizados e instalação dessa versão no Samsung Galaxy A31; a 1.0.19 reduz o tamanho do APK e mantém o código funcional da 1.0.18.

Esses registros são evidências de desenvolvimento. Permanecem verificações manuais do autor, incluindo fala e som audíveis e partes da bateria no A31. Não há comprovação de compatibilidade com todos os aparelhos nem de acessibilidade completa com TalkBack. O [estado do projeto](docs/ESTADO.md) e os [relatórios de revisão](docs/revisoes/) delimitam a cobertura executada.

A proposta prevê avaliação do fluxo de voz, prova de conceito de imagem, revisão heurística e avaliação de usabilidade com tarefas e SUS. Este README não apresenta resultados dessas avaliações como concluídos. O teste de exploração descrito no guia não constitui, por si só, uma sessão de pesquisa com participantes.

## Compatibilidade e limitações

- **Android 8.0 ou superior.** O APK de distribuição atual inclui processadores ARM de 32 e 64 bits. Não há versão para iPhone.
- **Voz em português brasileiro:** depende de um serviço de reconhecimento configurado no aparelho e de permissão para o microfone. Nomes podem ser reconhecidos incorretamente; confirme todos os dados. O interpretador usa regras locais, sem LLM.
- **Internet e voz:** o Genki prioriza reconhecimento no aparelho quando disponível. O serviço de fala do Android pode usar internet e processamento externo; funcionamento offline universal não é garantido. Veja a [documentação do SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer).
- **Câmera experimental:** iluminação, foco e texto da embalagem afetam o resultado. Pode sugerir um nome incorreto ou não encontrar informação útil; a revisão é obrigatória.
- **Lembretes:** dependem das permissões e das configurações do Android. O modo notificação respeita o silencioso; o modo alarme usa o volume de alarme e toca por até um minuto por disparo. A repetição automática, quando ligada, é limitada a três repetições, com intervalo de 10, 20 ou 30 minutos. Não toca com o celular desligado.
- **Escopo de programação:** horários definidos e dias da semana; esquemas como “dia sim, dia não” não são interpretados automaticamente.

O aplicativo auxilia a organização de informações inseridas pela pessoa. Não calcula a adequação de doses, não avalia interações medicamentosas e não substitui orientação profissional.

## Dados e privacidade

Os cadastros ficam em banco Room no dispositivo. O Genki não exige conta, não possui servidor ou sincronização própria, não exibe anúncios e não armazena permanentemente áudio ou fotografias da câmera. O manifesto remove a permissão de internet do aplicativo; o serviço de reconhecimento de fala é uma dependência do sistema, com comportamento próprio.

**O backup do Android está habilitado na configuração atual.** Conforme o aparelho e os ajustes do sistema, dados do aplicativo podem participar de backup ou transferência. Portanto, armazenamento local não significa garantia de que nenhuma cópia sairá do aparelho. O Genki não oferece exportação ou restauração própria dos cadastros. Para experimentar, use dados fictícios.

## Tecnologias e organização

Android nativo com **Kotlin, Jetpack Compose, Material 3, ViewModel e Room**. Os lembretes usam AlarmManager e notificações do Android; a voz usa SpeechRecognizer e TextToSpeech; a câmera usa CameraX e ML Kit Text Recognition com modelo embarcado.

```text
app/src/main/java/br/edu/ifrs/genki/
├── data/       Banco, repositório e configurações
├── domain/     Regras de domínio e validação
├── reminders/  Agendamento, notificações e alarme
├── voice/      Reconhecimento, interpretação e fluxo por voz
├── camera/     Captura e reconhecimento de texto
└── ui/         Telas e componentes de interface
app/src/test/   Testes automatizados
docs/          Especificação, decisões e evidências de desenvolvimento
```

Todos os fluxos de cadastro passam por um rascunho comum e pelo `DraftValidator` antes da gravação. A [especificação técnica](docs/ESPEC_ARQUITETURA.md) descreve a arquitetura; as [decisões posteriores](docs/DECISOES.md) registram seus ajustes.

## Executar a partir do código

Para desenvolvimento, abra o projeto no Android Studio, configure o SDK solicitado pelo Gradle e sincronize as dependências. A configuração atual usa `compileSdk`/`targetSdk` 37 e `minSdk` 26. As versões estão em [libs.versions.toml](gradle/libs.versions.toml) e [gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties).

No PowerShell, na raiz do repositório:

```powershell
.\gradlew.bat assembleDebug test
```

O APK de desenvolvimento fica em `app/build/outputs/apk/debug/app-debug.apk`. Para instalar em um emulador ou aparelho de desenvolvimento conectado:

```powershell
.\gradlew.bat installDebug
```

O APK de distribuição requer assinatura com a chave do autor, mantida fora do repositório. Uma compilação debug não substitui uma instalação release assinada com outra chave. Consulte [como rodar e distribuir](docs/HANDOFF_APP.md#4-como-rodar) antes de preparar atualizações.

## Documentação do TCC

| Documento | Conteúdo |
| --- | --- |
| [Guia de instalação e teste](docs/GUIA_INSTALACAO_TESTE.md) | Orientação para o orientador e outras pessoas que receberem o APK. |
| [Índice do projeto](docs/00_INDICE.md) | Localização das fontes acadêmicas e dos documentos no Drive. |
| [Requisitos oficiais](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit) | Texto dos RF01–RF25 e RNF01–RNF11; o [mapa local](docs/REQUISITOS_MAPA.md) relaciona requisitos e marcos. |
| [Estado atual](docs/ESTADO.md) e [decisões](docs/DECISOES.md) | Implementações, verificações, escolhas aceitas e pendências. |
| [Handoff do aplicativo](docs/HANDOFF_APP.md) | Ambiente, procedimentos de revisão e diário técnico. |
| [Bateria manual completa](docs/ROTEIRO_BATERIA_COMPLETA.md) | Verificação detalhada do aplicativo. |
| [Entregas](docs/ENTREGAS.md) e [registro de IA](docs/REGISTRO_IA.md) | Rastreabilidade do desenvolvimento e da assistência por IA. |

Documentos no Drive podem exigir permissão de acesso. O [protótipo navegável](https://joyful-biscuit-677f74.netlify.app) é uma referência de interface anterior ao aplicativo Android; não instala o app nem reproduz todas as funções atuais.

O desenvolvimento utiliza assistência de IA, com atribuição e registros no repositório. Implementações, testes técnicos e sugestões dos assistentes são distinguidos de decisões aceitas pelo autor e de resultados acadêmicos.
