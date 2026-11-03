# Genki — especificação técnica do MVP Android

Versão 2.0 · 21/10/2026 · Claude Opus 5.5. Substitui a v1.0 de 19/10 (Projeto do Claude, `claude/10_ESPEC_ARQUITETURA_MVP.md`).
Situação: **aceita pelo autor em 21/10/2026** (DEC-008 a DEC-017), com a diretriz "mínimo suficiente para atender
os requisitos e a proposta". As simplificações S1–S5 em relação à v1.0 foram aplicadas pelo Claude sob essa diretriz
e estão marcadas; o autor pode vetar qualquer uma.

Requisitos: planilha oficial no Drive (ver `docs/00_INDICE.md`). Este documento não repete o texto dos requisitos.

## Princípios para quem implementa (vibe coding com Codex / Claude Code)

1. **Menor implementação que atende o RF.** Se uma tela, classe ou dependência não é exigida por um RF/RNF, não crie.
2. Uma tarefa = um RF (ou poucos) + teste do que for lógica pura + commit pequeno.
3. Lógica em Kotlin puro (`domain/`) com teste unitário. UI e Android sem teste automatizado; verificação manual no aparelho.
4. Não inventar escopo: sem histórico de ingestão, estoque, cuidadores, conta, nuvem, LLM, anúncios.
5. Antes de dizer "pronto": `./gradlew assembleDebug test` e informar o que não foi verificado no aparelho.

## 1. Stack

| Item | Escolha |
| --- | --- |
| Plataforma | Android nativo, Kotlin, Jetpack Compose, Material 3 |
| minSdk | 26 (Android 8.0). compile/target = maior estável do Android Studio do autor (DEC-011) |
| Persistência | Room (KSP) para medicamentos e horários; **SharedPreferences** para 2 ajustes (S3) |
| Estado | ViewModel + `StateFlow`; `collectAsStateWithLifecycle` |
| Navegação | Navigation Compose (rotas tipadas com kotlinx-serialization) |
| Injeção | Manual: `AppContainer` criado em `GenkiApplication` (sem Hilt) |
| Lembretes | `AlarmManager` + `BroadcastReceiver` + notificações (sem WorkManager) |
| Voz (M3) | `SpeechRecognizer` (on-device quando disponível), `TextToSpeech`, parser determinístico próprio |
| Imagem (M4) | CameraX + ML Kit Text Recognition (latino) |
| Testes | JUnit 4 + kotlinx-coroutines-test, só `test/` (unitário) |

Versões exatas: as do template "Empty Activity" do Android Studio, registradas em `gradle/libs.versions.toml` no M0.

## 2. Estrutura de pastas

```
app/src/main/java/br/edu/ifrs/genki/
  GenkiApplication.kt   AppContainer.kt   MainActivity.kt
  domain/      Medication.kt, MedicationDraft.kt, DraftValidator.kt, OccurrenceCalculator.kt
  data/        GenkiDatabase.kt, entidades, MedicationDao.kt, MedicationRepository.kt, Settings.kt
  reminders/   ReminderScheduler.kt, ReminderReceiver.kt, BootReceiver.kt, Notifications.kt
  ui/          theme/, navigation/, home/, medications/, form/, review/, details/, settings/
  voice/       (M3) VoiceParser.kt, VoiceSession.kt, ui/
  camera/      (M4) OcrParser.kt, ui/
```

`domain/` não importa `android.*`.

## 3. Dados (DEC-008)

```kotlin
enum class TreatmentType { CONTINUOUS, TEMPORARY, UNDEFINED }   // "Uso contínuo", "Tem data para terminar", "Ainda não sei"

@Entity(tableName = "medication")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val concentration: String?,      // opcional, ex. "50 mg". Nunca copiar para dose
    val dose: String,                // obrigatória, ex. "1 comprimido"
    val daysMask: Int,               // bit = DayOfWeek.value - 1; 127 = todos os dias
    val treatment: TreatmentType,
    val startDate: LocalDate,        // padrão: hoje no fuso do aparelho
    val endDate: LocalDate?,         // só TEMPORARY; inclusiva
    val remindersEnabled: Boolean = true,
)

@Entity(
    tableName = "schedule_time",
    foreignKeys = [ForeignKey(MedicationEntity::class, ["id"], ["medicationId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["medicationId", "minuteOfDay"], unique = true)],
)
data class ScheduleTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val minuteOfDay: Int,            // 0..1439
)
```

- Conversores: `LocalDate` ↔ String ISO. Sem migração destrutiva (DEC-042): esquema exportado em `app/schemas`; mudança de entidade exige `DB_VERSION` + `Migration` em `data/Migrations.kt`.
- "Duas vezes ao dia" / "a cada X horas" (RF06) **não são persistidos**: viram horários concretos antes da revisão.
- "Ainda não sei" (DEC-009): agenda como contínuo, sem data final; UI mostra "Ainda não sei" com a ajuda do protótipo (DEC-027).
- Duplicidade: **sem tratamento** (S1). Mesmo nome pode ser cadastrado duas vezes; limitação registrada.

Ajustes (S3), em `SharedPreferences`: `repeatEnabled: Boolean = true`, `intervalMinutes: Int = 20` (10 | 20 | 30).

## 4. Rascunho e validação únicos (DEC-014)

```kotlin
enum class DraftSource { MANUAL, VOICE, CAMERA }

data class MedicationDraft(
    val localId: String = UUID.randomUUID().toString(),
    val existingId: Long? = null,
    val source: DraftSource,
    val name: String? = null,
    val concentration: String? = null,
    val dose: String? = null,            // OCR nunca preenche (RF23)
    val days: Set<DayOfWeek>? = null,
    val times: List<LocalTime> = emptyList(),
    val treatment: TreatmentType? = null, // sem padrão silencioso
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val durationDays: Int? = null,       // "por 7 dias" → endDate = start + 6 (DEC-003)
)
```

`DraftValidator.normalizeAndValidate(draft, today): Result` — usado por manual, edição, voz e câmera.
Normaliza (trim; startDate padrão = hoje; durationDays → endDate; endDate = null se não for TEMPORARY;
horários ordenados; repetidos geram `DUPLICATE_TIME`, DEC-027) e valida (S4, regras mínimas):

| Campo | Regra |
| --- | --- |
| Nome | obrigatório |
| Dose | obrigatória |
| Dias | ao menos um |
| Horários | ao menos um; sem repetidos (DEC-027) |
| Tratamento | obrigatório |
| Data final | obrigatória se TEMPORARY; não anterior ao início |

Retorna `Valid(draft)` ou `Invalid(List<Pair<Field, Problem>>)` com `Problem ∈ {MISSING, END_BEFORE_START, DUPLICATE_TIME}`.
A voz usa os campos `MISSING` para decidir o que perguntar (RF15). Só `Valid` chega ao repositório.

Sessão de cadastro: `RegistrationSession` (no `AppContainer`) guarda `List<MedicationDraft>` entre voz, câmera,
formulário e revisão (RNF09). Limpa ao salvar ou cancelar. Morte do processo perde o rascunho (limitação).

## 5. Telas e ViewModels

| Tela | ViewModel | RF |
| --- | --- | --- |
| Hoje — horários de hoje calculados dos dados | `HomeViewModel` | RF09 (visualização) |
| Medicamentos — lista | `MedicationListViewModel` | RF02 |
| Detalhe — dados, liga/desliga lembretes, editar, excluir com confirmação | `DetailViewModel` | RF02, RF10 |
| Adicionar — escolher Voz / Digitar / Câmera | — | RF01, RF11, RF18, RF20 |
| Formulário — novo ou edição | `FormViewModel` | RF01, RF03–RF08 |
| Revisão — lista de rascunhos, "Corrigir", "Confirmar e salvar" | `ReviewViewModel` | RF17, RF22, RNF05 |
| Ajustes — repetição liga/desliga, intervalo 10/20/30, status das permissões | `SettingsViewModel` | RF24, RF25 |
| Voz (M3) | `VoiceViewModel` | RF11–RF19 |
| Câmera (M4) | `CameraViewModel` | RF20–RF23 |

Barra inferior: Hoje · Medicamentos · Ajustes. Edição salva direto (valida, sem tela de revisão), como no protótipo.
Visual: tokens do protótipo v1 (verde `#1b6b4a`, superfície `#f6faf7`, texto `#191c1a`, erro `#b3261e`), alvos ≥ 48dp,
fonte do sistema respeitada (RNF02). Texto de interface nunca usa "Tomado"/"Tomei".

`MedicationRepository`: `observeAll()`, `observe(id)`, `insert(drafts)`, `update(id, draft)`, `delete(id)`,
`setRemindersEnabled(id, on)`. Toda mutação chama `ReminderScheduler.sync(medId)` (ou `cancel`) depois do commit.

## 6. Lembretes (DEC-012, com S2)

- `OccurrenceCalculator.next(med, time, after): ZonedDateTime?` — próxima data/hora > `after` respeitando dias,
  startDate e endDate. `null` = não agendar (RF09). Função pura, testada.
- Um alarme por horário: `setExactAndAllowWhileIdle` se `canScheduleExactAlarms()`, senão `setAndAllowWhileIdle`
  com aviso em Ajustes. `PendingIntent` identificado por `genki://time/{timeId}`.
- `ReminderReceiver`, ações (estado só em extras do Intent — **sem tabela de alertas**, S2):

| Ação | Comportamento |
| --- | --- |
| `FIRE(timeId)` | Revalida; publica notificação "Lembrete das 08:00 — Losartana 50 mg · 1 comprimido" com botões **Entendi** e **Adiar N min** (N = intervalo dos Ajustes); se repetição ligada, agenda `REPEAT(timeId, 1)` em +N min; agenda próximo `FIRE` |
| `REPEAT(timeId, k)` | Republica; se k < 3, agenda `REPEAT(k+1)` (RF24) |
| `ACK(timeId)` — botão ou deslizar (`deleteIntent`) | Cancela notificação e `REPEAT`. Nada é registrado (DEC-004) |
| `SNOOZE(timeId)` | Cancela notificação e `REPEAT`; agenda `FIRE_SNOOZED(timeId)` em +N min (RF25) |

- Canal "Lembretes de medicamentos", `IMPORTANCE_HIGH`, `CATEGORY_REMINDER`, `autoCancel = false`.
- `BootReceiver` (`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`) e abertura do app → `syncAll()`.
  Limitação (S2): repetições e adiamentos pendentes não sobrevivem a reinício; ocorrências perdidas com o aparelho desligado
  não são emitidas depois.
- Permissões: `POST_NOTIFICATIONS` pedida (Android 13+) ao salvar o 1º medicamento, com explicação;
  `SCHEDULE_EXACT_ALARM` declarada, e em Android 14+ botão em Ajustes para a tela do sistema; `RECEIVE_BOOT_COMPLETED`.
  M3: `RECORD_AUDIO`. M4: `CAMERA`. Não usar `USE_EXACT_ALARM`, tela cheia, serviço em primeiro plano.

## 7. Voz (M3) — escopo mínimo

Fluxo: Toque no microfone → `SpeechRecognizer` (pt-BR; `createOnDeviceSpeechRecognizer` se disponível, API 31+) →
texto → `VoiceParser.parse(texto, rascunhos)` → rascunhos atualizados → **conferência dos nomes** (DEC-046: lista falada e na
tela; tocar no nome troca, X tira, "Separar" divide; por voz "remover X", "trocar X por Y", "separar X", mais nomes;
"Continuar"/"pronto") → pergunta o 1º campo `MISSING` (texto na tela + TTS) → repete → Revisão comum. Da câmera, a
conferência é pulada (o nome já foi conferido lá).

`VoiceParser` (Kotlin puro, regras e regex, sem LLM, testado com um corpus):

| Reconhece | Exemplos |
| --- | --- |
| Nome | primeira palavra/expressão que não é número, unidade, dia ou horário; "também tomo X" cria novo rascunho |
| Concentração | `50 mg`, `500 miligramas`, `20mg` |
| Dose | `1 comprimido`, `meio comprimido`, `10 gotas`, `uma cápsula` |
| Horários | `às 8`, `8 da manhã`, `20h`, `8 e 20`; "duas vezes ao dia"/"de 8 em 8 horas" pedem o horário inicial e geram horários |
| Dias | "todo dia", "segunda e quarta", "dia sim dia não" **não** (fora do escopo) |
| Tratamento | "uso contínuo", "por N dias", "até dia X", "não sei" |
| Correção | "na verdade é às 9" altera o horário mais próximo do mencionado; "os dois também às 18" acrescenta |

RF14 (vários medicamentos numa fala, prioridade Média): separar por " e " entre nomes; se ambíguo, perguntar.
Corte previsto (DEC-016): se atrasar, suportar um medicamento por vez.
"Prefiro digitar" leva os rascunhos ao formulário (RF18). Nenhum áudio é gravado (RNF07).
Instrumentação (DEC-015): em build debug, `Log.i("GenkiEval", ...)` com ensaio, fim da fala, resultado do reconhecedor,
rascunho interpretado e tempos (`SystemClock.elapsedRealtime()`). Só texto.

## 8. Câmera / OCR (M4) — PoC mínima

CameraX (captura de foto única) → ML Kit → `OcrParser.parse(texto): MedicationDraft(source = CAMERA)`:
nome = linha de texto mais longa sem dígitos entre as 3 maiores; concentração = 1ª ocorrência de
`\d+([.,]\d+)?\s?(mg|g|mcg|ml|UI)`. Dose, horários, dias e tratamento ficam vazios (RF23, teste unitário).
Resultado editável → segue para formulário ou voz → Revisão (RF22). Falha → "Tentar de novo" ou "Digitar" (RF20).
Instrumentação: `GenkiEval` com texto bruto, campos sugeridos e tempo.

## 9. Marcos e aceite

| Marco | Período (meta) | Aceite mínimo |
| --- | --- | --- |
| M0 Base | 21–22/10 | Projeto compila e abre no aparelho; barra inferior; tema; `./gradlew test` roda |
| M1 CRUD | 23–24/10 | RF01–RF05, RF07, RF08, RF17 (manual); dados persistem após fechar o app (RNF06). Testes do validador |
| M2 Lembretes | 25–26/10 | RF09, RF10, RF24, RF25 no aparelho; Hoje com dados reais. Testes do `OccurrenceCalculator` |
| M3 Voz | 27–30/10 | RF06, RF11–RF19 com o corpus de teste passando no parser |
| M4 Câmera | 31/10–01/11 | RF20–RF23; teste do `OcrParser` |
| Congelamento | 02/11 | Só correções depois (DEC-017) |

Ordem de corte se atrasar (DEC-016): 1º RF14 simplificado; 2º PoC de imagem reduzida ao mínimo verificável.
CRUD, lembretes e voz com confirmação não são cortados.

## Simplificações aplicadas em 21/10 (vetáveis)

| ID | v1.0 | v2.0 |
| --- | --- | --- |
| S1 | Aviso de duplicidade por nome normalizado | Sem tratamento de duplicidade (nenhum RF exige) |
| S2 | Tabela `pending_alert`, restauração após reinício, tela de alerta com 10/20/30 | Estado nos extras do Intent; adiamento único = intervalo dos Ajustes; sem restaurar repetição após reinício |
| S3 | Ajustes em tabela Room | SharedPreferences |
| S4 | Validador com 9 regras e limites de tamanho | 6 regras essenciais |
| S5 | Teste instrumentado de DAO | Só testes unitários; verificação manual no aparelho |
