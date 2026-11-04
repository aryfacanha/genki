# Conferência RV-125-08, 09 e 10

Escopo: diff c5eb7b3..68935f9 em app e comparação com AlarmSpeech.kt.

RV-125-08: confirmado na análise estática do diff.
- Vazio permanece opcional; 0 e valores acima de 999 são inválidos, exibem erro e não chamam onEnd.
- O campo mantém quatro dígitos: "1000" permanece inválido, sem virar "100".
- typedPair identifica a última alteração válida digitada; o efeito não reescreve esse par.
- O efeito depende de start/end e não os altera: não há ciclo de realimentação no trecho.
- Mudanças externas de início/término recalculam o número; entradas inválidas não alteram as datas.
- O diff não acrescenta escrita no banco nem caminho de persistência.
- Não verificado: interação real, digitação rápida/recomposição e seleção no calendário.
- Não verificado: exclusividade do DraftValidator até o banco; seus chamadores e persistência estão fora do escopo de leitura.

RV-125-09: confirmado na análise estática do diff.
- RepeatNote descreve o limite de repetições, sem prometer um próximo aviso após a terceira.
- Não verificado: cartão renderizado e execução das repetições em dispositivo.

RV-125-10: confirmado pela comparação com AlarmSpeech.kt.
- Um remédio: horário e nome; vários: quantidade, conforme a nova descrição.
- Não verificado: reprodução efetiva por síntese de voz.

Validação: .\gradlew.bat testDebugUnitTest executado uma vez; BUILD SUCCESSFUL.
A tarefa :app:testDebugUnitTest ficou UP-TO-DATE, sem reexecutar os testes.
Não verificado nesta chamada: contagem de 226 testes e resultado individual de zero falhas.
Sem assembleDebug, --rerun-tasks, emulador, operação do A31 ou commit.
