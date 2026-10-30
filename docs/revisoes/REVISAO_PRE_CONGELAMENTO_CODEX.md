# Revisão pré-congelamento — Codex — 1.0.12 a 1.0.17

30/10/2026 · Codex · alvo `8a27111..a184acd` (1.0.17, versionCode 18).

**Parecer: a 1.0.17 está tecnicamente apta ao congelamento de 02/11, na cobertura desta revisão, com duas limitações S3 registradas. Nenhum S1/S2 confirmado.** Os S3 abaixo podem ficar para correção posterior; essa recomendação não equivale ao aceite do autor. Não houve correção permanente de código, commit, push ou operação no A31.

## Partida e fontes

- Partida local: `main`/`d8be24ea6aea11810143faa028c20fd9918fc013`. O commit só acrescenta o roteiro; `git diff a184acd HEAD -- app` vazio. `git pull` tentado e impedido por falta de escrita em `.git/FETCH_HEAD`; atualização do remoto não confirmada.
- Lidos: AGENTS, ESTADO, HANDOFF (§2/§3/§5/§7), DEC-004/014/040/046–050, ESPEC, REQUISITOS_MAPA, pré-revisão heurística e todo o diff do app no intervalo pedido.
- Reabertos pelo conector Drive: [proposta oficial](https://drive.google.com/file/d/1aSbtKncsWQdCIbnItByySUQRuO5xzBIb/view) e `Requisitos!A1:H50` da [planilha oficial](https://docs.google.com/spreadsheets/d/18YdF337444bBPXeIh2Hd9LQZaB6gOKD6LdlAr2FKrVI/edit). As decisões aceitas posteriores prevalecem sobre os trechos antigos da ESPEC que ainda dizem duplicidade sem tratamento, perda de adiados no reinício e ausência de serviço/tela cheia. A divergência foi identificada antes de usar esses trechos como critério.
- O relato do autor de teste satisfatório da 1.0.17 no A31 é evidência fornecida pelo autor, separada da verificação do Codex. Esta revisão não é MET-3 nem teste com participantes.

## Achados confirmados

S1 = bloqueia; S2 = erro relevante; S3 = menor; S4 = sugestão.

| ID | Gravidade | Arquivo:linha | Evidência e impacto | Correção sugerida, pendente do autor |
| --- | --- | --- | --- | --- |
| RV-PC-01 | **S3** | `ui/details/DetailScreen.kt:142` (faixa em `:119`) | Em tratamento terminado, desligar e ligar os lembretes mostra **“Lembretes ativados”**, simultaneamente a **“Sem lembretes: o tratamento terminou”**. A faixa está correta e o agendador continua respeitando a data final; o retorno da ação promete uma ativação que não produzirá novos avisos. Residual de transparência do N4. Confirmado no emulador com APK original. | Ao ligar um tratamento terminado, explicar que é preciso editar o período para voltar a receber lembretes, ou ajustar o retorno da ação. Preservar a possibilidade de editar e a data final inclusiva. |
| RV-PC-02 | **S3** | `voice/Speaker.kt:99` | Se `tts.speak()` retorna `TextToSpeech.ERROR` imediatamente e não emite evento posterior, `speaking` fica falso, mas `done` permanece pendente: o callback de término não é chamado e a escuta automática não começa. O microfone por toque continua disponível; não perde rascunhos nem impede concluir por toque. **Preexistente em `8a27111`**, agora explicitamente conferido. Reproduzido em teste isolado com a fonte original e substitutos controlados das APIs Android, não por falha real do motor do emulador. | Tratar a recusa síncrona do motor explicitamente, informando o fallback e permitindo responder por toque; evitar depender de um `onDone` que não virá. Não abrir a escuta imediatamente enquanto a pessoa precisa ler a pergunta. |

### Reprodutores

**RV-PC-01:** cadastrar/preparar um medicamento temporário com início 20/10 e término 29/10; abrir Detalhes em 30/10; tocar **Desativar lembretes** e depois **Ativar lembretes**. A captura/árvore `ended-reactivated` contém a faixa de tratamento terminado e o aviso “Lembretes ativados”. A edição segue disponível. Nenhuma emissão após o término foi observada ou inferida desse texto.

**RV-PC-02:** instanciar `Speaker`; concluir a inicialização com `SUCCESS` e idioma disponível; configurar o substituto de `TextToSpeech.speak()` para retornar `ERROR`, sem chamar o listener; executar `speaker.speak("pergunta") { contador++ }` e esvaziar a fila principal. Resultado: `contador == 0`, `speaking == false`. O teste usa uma cópia byte a byte de `Speaker.kt`; não substitui a verificação de motores reais. Artefato reproduzível: `build/pre_congelamento/speaker_probe.py` e `speaker-probe/result.log`.

Não há S4 novo formalizado. As sugestões históricas N6–N19 não corrigidas mantêm seu estado pendente; este parecer não as transforma em decisões aceitas nem afirma uma nova avaliação completa delas.

## Execução e cobertura

| Ponto | Resultado e limite da evidência |
| --- | --- |
| Compilação/testes | Build final **passou**, com fonte original, e **179 testes executados, zero falhas/erros/ignorados**. Uma primeira execução reutilizou os testes; em seguida foram forçados a executar, inclusive no build final. Nenhum teste temporário entrou nessa contagem. |
| Ambiente de build | O wrapper tentou baixar o Gradle e foi bloqueado pela rede. A distribuição 9.6.0 e o cache de dependências existentes foram copiados para `build/pre_congelamento/`; a execução offline em área gravável passou. O processo novo evitou reutilizar JARs da distribuição em área sem escrita. O build final usou a propriedade de compilação Kotlin em processo, só na linha de comando. Não foram alterados Gradle, versões, manifest ou configurações versionadas. |
| Instalação | A primeira instalação foi recusada por assinatura de debug diferente. O APK foi reassinado com a chave **de debug** existente, sem desinstalar o app nem acessar chave de release. APK final original 1.0.17 instalado apenas em `emulator-5554`, API 35, retrato 1080 × 2400, 420 dpi. APK anterior, que declarava 1.0.16, guardado no backup. |
| Regras de produto | Cadastro/edição continuam chegando ao repositório como `DraftValidator.Result.Valid`; nenhuma nova rota de gravação no diff. Exclusão e liga/desliga mantêm os caminhos já existentes. “Entendi” só reconhece/retira aviso, sem ingestão ou histórico. Nenhuma nova rede, conta, nuvem, LLM, Hilt, WorkManager ou DataStore. OCR continua sem dose/dias/horários/tratamento; concentração e dose permanecem separadas. |
| Datas/término | `LocalDate.now()`/`LocalDateTime.now()` novos usam o fuso do aparelho. `ended` usa `date > endDate`, coerente com `isScheduledOn` inclusivo. A lista mostrou “Até 30/10/2026” para o último dia e “Tratamento terminou em 29/10/2026” para o anterior. Detalhes e edição acessíveis; ressalva RV-PC-01. |
| Nome repetido no formulário | Cadastro manual com Metformina e dose preenchida: aviso ao sair da etapa 1; Voltar e conferir permanece nela; repetir e Continuar mesmo assim avança à etapa 2. Editar Teste Manhã sem mudar o próprio nome salvou sem aviso indevido. Não se criou medicamento nesse cadastro novo. |
| Nome repetido na voz | Fala simulada com Losartana, Metformina e Omeprazol: conferência sem microfone automático; Continuar abriu o aviso do nome realmente existente, Metformina; aceitar levou às perguntas. Os testes oficiais cobrem o aceite nessa fase sem novo aviso ao salvar. Inspeção confirmou a checagem final e o aceite por nome normalizado; não se removeu a validação final. |
| Leitura da voz | Preferência `voice_assistant` ausente/desligada: leitura ligada ao abrir, botão Silenciar e inicialização TTS pt-BR registrados. Silenciar durante TTS impediu abertura posterior automática observável; responder por toque com leitura silenciada avançou sem nova escuta automática. Estado silenciado conservado na recriação/fonte/rotação. Troca da leitura durante escuta real do reconhecedor sem áudio também conferida; audibilidade não avaliada. |
| Callbacks do Speaker | Teste isolado: texto pendente antes da inicialização conclui; dois `onDone` consomem o callback uma vez; callback da fala substituída é ignorado; `stop` descarta callback já enfileirado e limpa `speaking`; sem português, o callback pendente conclui uma vez. Recusa síncrona: RV-PC-02. Motor que nunca conclui uma requisição aceita não foi reproduzido no Android. |
| Fonte 2,0×/rolagem da voz | Com três rascunhos, perguntas de dias, horários e duração visíveis no retrato; outros medicamentos compactos; rodapé empilha quando necessário. Pergunta de horários permite rolar aos links. Digitar a resposta deixou campo e Enviar acima do teclado; enviar avançou para duração. O `LaunchedEffect` de BringIntoView tem chaves de medicamento/campo, não roda a cada recomposição; não se observou disputa com a rolagem do teclado nos casos percorridos. Não é garantia para qualquer tamanho de nome/tela. |
| Hoje | Fixture com seis medicamentos/18 horários, incluindo dois registros desligados de mesmo nome, um ativo com esse nome, um terminado e um no último dia. Doze ocorrências passadas recolhidas; expandir mostrou as linhas; fonte 2,0× permitiu alcançar Agora e os próximos. Com todas as 15 ocorrências do dia passadas, botão contou 15 e houve um marcador Agora, sem acessar índice inválido. Com todos desligados, “Nenhum lembrete para hoje” veio acompanhado dos nomes/motivo. O texto mantém nomes repetidos, sem confundir os registros na programação. |
| RV-048, regressão curta | APK original: dois `FIRE` às 20:03, horários 1101/1102, publicaram as duas notificações; serviço usou `foregroundId=2147483647`, canal `alarme_tocando`. Entendi de 1102 removeu só a sua; notificação 1101 e serviço permaneceram. Entendi de 1101 parou o serviço por resposta e não restou notificação do app. Troca Como notificação removeu o próximo despertador do sistema; Como alarme recriou para 31/10 às 20:03. Texto de 1 minuto visível. Não se repetiram limite de 1 minuto, vídeo, adiado ou tela bloqueada: regressão curta solicitada. |

Trecho do log do alarme original:

```text
20:03:00.144 lembrete_emitido timeId=1102 acao=FIRE repeticao=0
20:03:00.844 alarme_tocando timeId=1102
20:03:00.968 lembrete_emitido timeId=1101 acao=FIRE repeticao=0
20:03:00.985 alarme_tocando timeId=1101
20:03:40.095 lembrete_reconhecido timeId=1102
20:03:45.358 lembrete_reconhecido timeId=1101
20:03:45.466 alarme_parado motivo=resposta
```

## Restauração e limites

- Backup anterior à instalação/testes: APK instalado, banco e preferências via `run-as`, oito arquivos com SHA-256, estado de fonte/rotação/hora automática/permissões. Fixtures SQLite serviram apenas para montar cenários no emulador, sem criar caminho de gravação no produto.
- A entrada temporária em `SpeechInput.start()` consumiu texto de um arquivo privado somente no emulador. Retirada; SHA-256 original de `SpeechInput.kt`: `9fa49b3cdac551f0d948a0f5f1613f96aadb816077d69e40e2f74767c37fbb85`. APK final recompilado e verificado sem a string de entrada simulada. `git diff -- app` vazio ao terminar.
- **Os oito arquivos restaurados coincidiram byte a byte com o backup, inclusive após reabrir.** Conferidos dois medicamentos originais: Metformina desligada e Losartana 50 mg ligada, ambos com horário 23:05. Testes removidos; preferências originais de alarme/intervalo preservadas.
- Fonte 1,0×, rotação automática ligada, orientação 0, `auto_time=0` como encontrado; relógio não foi alterado. Microfone/câmera novamente sem permissão. Não se operou o A31. Alterações preexistentes em IDE/configuração local e logs preservadas.
- Não verificados: fala real, som ouvido, TalkBack, motor TTS real sem português/com recusa síncrona, Android 14+ com tela cheia permitida (o emulador negou o full-screen intent), capacidade para todas as combinações de nomes/fontes e nova bateria completa física. Sem crash do app observado nesta execução.
- Logs, capturas, testes isolados e hashes locais em `build/pre_congelamento/` (artefatos ignorados pelo Git). Este relatório registra a evidência necessária para passagem de contexto; não é integração à monografia nem aceite das correções sugeridas.

**Bloqueios S1/S2: nenhum confirmado. Adiáveis: RV-PC-01 e RV-PC-02, ambos S3.** A decisão de congelar e de aceitar essas limitações cabe ao autor; nenhuma decisão nova foi acrescentada a DECISOES.
