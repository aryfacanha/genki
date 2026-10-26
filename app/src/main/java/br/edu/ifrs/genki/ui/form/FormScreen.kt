package br.edu.ifrs.genki.ui.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.TreatmentType
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.FieldError
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.InfoRow
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.components.SectionTitle
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Cadastro manual em três etapas + revisão (`DigitarMedicamento.tsx` com `steps`, `ConfirmarMedicamento.tsx`)
 * e edição em página única (`EditarMedicamento.tsx`). [editId] nulo = cadastro novo.
 */
@Composable
fun MedicationFormScreen(
    editId: Long?,
    onClose: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: FormViewModel = viewModel(factory = FormViewModel.factory(editId)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var askDiscard by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.savedMessage) { state.savedMessage?.let(onSaved) }

    val handleBack = {
        if (!viewModel.back()) {
            if (state.changed) askDiscard = true else onClose()
        }
    }
    BackHandler(onBack = handleBack)

    Column(Modifier.fillMaxSize().imePadding()) {
        if (!state.loaded) return@Column
        val review = state.review
        if (state.step == REVIEW && review != null) {
            ReviewContent(
                draft = review,
                onBack = handleBack,
                onEditSection = viewModel::editSection,
                onConfirm = viewModel::save,
            )
        } else {
            FormContent(state, viewModel, onBack = handleBack)
        }
    }

    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            title = { Text("Descartar alterações?") },
            text = { Text("As informações que você não salvou serão perdidas.") },
            confirmButton = { TextButton(onClick = { askDiscard = false }) { Text("Continuar editando") } },
            dismissButton = {
                TextButton(onClick = { askDiscard = false; onClose() }) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}

@Composable
private fun ColumnScope.FormContent(state: FormUiState, viewModel: FormViewModel, onBack: () -> Unit) {
    val isEdit = viewModel.isEdit
    val steps = !isEdit
    val step = state.step
    val backLabel = when {
        isEdit -> "Cancelar edição"
        state.fromReview -> "Voltar para a revisão"
        step > 0 -> "Voltar para a etapa anterior"
        else -> "Voltar"
    }
    GenkiTopBar(if (isEdit) "Editar medicamento" else "Novo medicamento", onBack = onBack, backLabel = backLabel)

    // Cada etapa nova começa do topo.
    key(step) {
        val sections = remember { List(3) { BringIntoViewRequester() } }
        // Na edição (página única), leva até a primeira seção com erro.
        LaunchedEffect(state.errors) {
            val first = state.errors.keys.minByOrNull { field -> STEP_FIELDS.indexOfFirst { field in it } }
            if (isEdit && first != null) sections[STEP_FIELDS.indexOfFirst { first in it }].bringIntoView()
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
        ) {
            val show = { index: Int -> !steps || step == index }
            val stepLabel = { index: Int -> if (steps) index else null }
            if (show(0)) {
                Column(Modifier.bringIntoViewRequester(sections[0])) {
                    StepHeading("Medicamento", stepLabel(0))
                    if (steps && !state.fromReview) {
                        Text(
                            "Você vai conferir tudo antes de salvar.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    MedicationSection(state, viewModel)
                }
            }
            if (show(1)) {
                Column(Modifier.bringIntoViewRequester(sections[1])) {
                    StepHeading("Dias e horários", stepLabel(1))
                    ScheduleSection(state, viewModel)
                }
            }
            if (show(2)) {
                Column(Modifier.bringIntoViewRequester(sections[2])) {
                    StepHeading("Duração do tratamento", stepLabel(2))
                    TreatmentSection(state, viewModel)
                }
            }
        }
    }

    val continueLabel = when {
        isEdit -> "Salvar alterações"
        state.fromReview -> "Concluir alteração"
        step == STEP_FIELDS.lastIndex -> "Continuar para revisão"
        else -> "Continuar"
    }
    BottomActions {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (steps && !state.fromReview && step > 0) {
                SecondaryButton("Voltar", onClick = onBack, modifier = Modifier.weight(0.6f))
            }
            PrimaryButton(
                continueLabel,
                onClick = { if (isEdit) viewModel.save() else viewModel.next() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StepHeading(title: String, stepIndex: Int?) {
    Column(Modifier.padding(top = 24.dp, bottom = 8.dp)) {
        if (stepIndex != null) {
            Text(
                "Etapa ${stepIndex + 1} de ${STEP_FIELDS.size}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SectionTitle(title)
    }
}

@Composable
private fun MedicationSection(state: FormUiState, viewModel: FormViewModel) {
    val fields = state.fields
    TextInput(
        label = "Nome do medicamento",
        example = "Exemplo: Losartana",
        value = fields.name,
        error = state.errors[Field.NAME]?.let { MedicationText.error(Field.NAME, it) },
        onChange = { value -> viewModel.edit(Field.NAME) { it.copy(name = value) } },
    )
    Spacer(Modifier.height(16.dp))
    TextInput(
        label = "Concentração (opcional)",
        example = "Exemplo: 50 mg",
        value = fields.concentration,
        error = null,
        onChange = { value -> viewModel.edit { it.copy(concentration = value) } },
    )
    Spacer(Modifier.height(16.dp))
    // Quanto toma por vez: campo separado da concentração.
    TextInput(
        label = "Quanto você toma por vez",
        example = "Exemplo: 1 comprimido",
        value = fields.dose,
        error = state.errors[Field.DOSE]?.let { MedicationText.error(Field.DOSE, it) },
        onChange = { value -> viewModel.edit(Field.DOSE) { it.copy(dose = value) } },
        last = true,
    )
}

@Composable
private fun TextInput(
    label: String,
    example: String,
    value: String,
    error: String?,
    onChange: (String) -> Unit,
    last: Boolean = false,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            // O exemplo fica neutro mesmo com erro; a mensagem de erro vem logo abaixo.
            supportingText = { Text(example, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = if (last) ImeAction.Done else ImeAction.Next,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        FieldError(error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleSection(state: FormUiState, viewModel: FormViewModel) {
    val fields = state.fields
    Column(Modifier.selectableGroup()) {
        listOf(DaysMode.ALL to "Todos os dias", DaysMode.SPECIFIC to "Dias específicos").forEach { (mode, label) ->
            RadioRow(label, selected = fields.daysMode == mode) {
                viewModel.edit(Field.DAYS) { it.copy(daysMode = mode) }
            }
        }
    }
    if (fields.daysMode == DaysMode.SPECIFIC) {
        FlowRow(
            Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MedicationText.WEEK.forEach { day ->
                val selected = day in fields.specificDays
                FilterChip(
                    selected = selected,
                    onClick = {
                        viewModel.edit(Field.DAYS) {
                            it.copy(specificDays = if (selected) it.specificDays - day else it.specificDays + day)
                        }
                    },
                    label = { Text(MedicationText.dayShort(day)) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = MedicationText.dayName(day) },
                )
            }
        }
        val error = state.errors[Field.DAYS]
        if (error == null) {
            Text(
                if (fields.specificDays.isEmpty()) "Nenhum dia selecionado"
                else "Dias selecionados: ${MedicationText.days(fields.specificDays)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        FieldError(error?.let { MedicationText.error(Field.DAYS, it) })
    }

    Text(
        "Horários",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
    // `null` = fechado; -1 = novo horário; n = alterar o horário n.
    var editing by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        fields.times.forEachIndexed { index, time ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenkiCard(Modifier.weight(1f), onClick = { editing = index }) {
                    Column(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp).semantics(mergeDescendants = true) {
                            contentDescription = "Horário ${index + 1}: ${MedicationText.time(time)}. Toque para alterar."
                        },
                    ) {
                        Text("Horário ${index + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(MedicationText.time(time), style = MaterialTheme.typography.titleLarge)
                    }
                }
                if (fields.times.size > 1) {
                    Surface(
                        onClick = { viewModel.edit(Field.TIMES) { it.copy(times = it.times.filterIndexed { i, _ -> i != index }) } },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.heightIn(min = 56.dp).semantics {
                            contentDescription = "Remover horário ${MedicationText.time(time)}"
                        },
                    ) {
                        Column(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Text("Remover")
                        }
                    }
                }
            }
        }
        FieldError(state.errors[Field.TIMES]?.let { MedicationText.error(Field.TIMES, it) })
        OutlinedButton(
            onClick = { editing = -1 },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Adicionar horário")
        }
    }

    editing?.let { index ->
        TimeDialog(
            initial = fields.times.getOrNull(index) ?: LocalTime.of(8, 0),
            onDismiss = { editing = null },
            onConfirm = { time ->
                editing = null
                viewModel.edit(Field.TIMES) {
                    it.copy(times = if (index < 0) it.times + time else it.times.mapIndexed { i, t -> if (i == index) time else t })
                }
            },
        )
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun TreatmentSection(state: FormUiState, viewModel: FormViewModel) {
    val fields = state.fields
    Text("Por quanto tempo você vai usar?", modifier = Modifier.padding(bottom = 12.dp))
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TreatmentType.entries.forEach { type ->
            val selected = fields.treatment == type
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(
                    1.5.dp,
                    when {
                        selected -> MaterialTheme.colorScheme.primary
                        state.errors[Field.TREATMENT] != null -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    },
                ),
                modifier = Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton) {
                    viewModel.edit(Field.TREATMENT, Field.END_DATE) { it.copy(treatment = type) }
                },
            ) {
                Row(Modifier.heightIn(min = 56.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(MedicationText.treatment(type), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
    FieldError(state.errors[Field.TREATMENT]?.let { MedicationText.error(Field.TREATMENT, it) })
    if (fields.treatment == TreatmentType.UNDEFINED) {
        Text(
            MedicationText.UNKNOWN_DURATION_HELP,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
    if (fields.treatment == TreatmentType.TEMPORARY) {
        Spacer(Modifier.height(14.dp))
        DateField("Data de início", viewModel.startDateOrToday()) { date ->
            viewModel.edit(Field.END_DATE) { it.copy(startDate = date) }
        }
        Spacer(Modifier.height(10.dp))
        DateField("Data de término", fields.endDate, isError = state.errors[Field.END_DATE] != null) { date ->
            viewModel.edit(Field.END_DATE) { it.copy(endDate = date) }
        }
        FieldError(state.errors[Field.END_DATE]?.let { MedicationText.error(Field.END_DATE, it) })
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, isError: Boolean = false, onPick: (LocalDate) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
    OutlinedButton(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics {
            contentDescription = "$label: ${date?.let(MedicationText::date) ?: "não escolhida"}. Toque para escolher."
        },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline),
    ) {
        Text(
            date?.let(MedicationText::date) ?: "Escolher data",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (open) DateDialog(date, onDismiss = { open = false }) { open = false; onPick(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha o horário") },
        text = { TimePicker(state) },
        confirmButton = { TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(initial: LocalDate?, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    // O DatePicker representa o dia escolhido como meia-noite UTC; a conversão em UTC só desfaz isso
    // (a data continua civil; "hoje" vem do fuso do aparelho no validador).
    val state = rememberDatePickerState(initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) {
        DatePicker(state)
    }
}

/** "Confira antes de salvar" (RF17): uma seção por etapa, cada uma com "Alterar". */
@Composable
private fun ColumnScope.ReviewContent(
    draft: MedicationDraft,
    onBack: () -> Unit,
    onEditSection: (Int) -> Unit,
    onConfirm: () -> Unit,
) {
    GenkiTopBar("Confira antes de salvar", onBack = onBack, backLabel = "Voltar e corrigir")
    Column(
        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            "Para mudar algo, toque em Alterar.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp),
        )
        GenkiCard {
            Column {
                ReviewSection("Medicamento", onEdit = { onEditSection(0) }) {
                    InfoRow("Nome", draft.name.orEmpty())
                    draft.concentration?.let { InfoRow("Concentração", it) }
                    InfoRow("Quanto toma", draft.dose.orEmpty())
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ReviewSection("Dias e horários", onEdit = { onEditSection(1) }) {
                    InfoRow("Dias", MedicationText.days(draft.days.orEmpty()))
                    InfoRow("Horários", MedicationText.times(draft.times))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ReviewSection("Duração do tratamento", onEdit = { onEditSection(2) }) {
                    val treatment = draft.treatment ?: TreatmentType.UNDEFINED
                    InfoRow(
                        "Duração",
                        MedicationText.treatment(treatment),
                        secondary = if (treatment == TreatmentType.TEMPORARY) draft.startDate?.let { MedicationText.period(it, draft.endDate) } else null,
                        help = if (treatment == TreatmentType.UNDEFINED) MedicationText.UNKNOWN_DURATION_HELP else null,
                    )
                }
            }
        }
    }
    BottomActions {
        PrimaryButton("Confirmar e salvar", onClick = onConfirm)
        SecondaryButton("Voltar e corrigir", onClick = onBack)
    }
}

@Composable
private fun ReviewSection(title: String, onEdit: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(title, Modifier.weight(1f))
            TextButton(
                onClick = onEdit,
                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Alterar ${title.lowercase()}" },
            ) { Text("Alterar") }
        }
        content()
    }
}
