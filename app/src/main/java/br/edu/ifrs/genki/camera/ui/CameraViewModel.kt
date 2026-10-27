package br.edu.ifrs.genki.camera.ui

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.camera.OcrLine
import br.edu.ifrs.genki.camera.OcrParser
import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.RegistrationSession
import br.edu.ifrs.genki.ui.genkiContainer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CameraPhase { CAPTURE, READING, RESULT, FAILED }

data class CameraUiState(
    val phase: CameraPhase = CameraPhase.CAPTURE,
    /** Sugestões editáveis (RF22): só nome e concentração (RF23). */
    val name: String = "",
    val concentration: String = "",
    val message: String? = null,
)

/**
 * Foto da embalagem → ML Kit (modelo latino embutido) → [OcrParser] → resultado editável (ESPEC §8, RF20–RF23).
 * A foto fica só na memória durante a leitura; nada é gravado.
 */
class CameraViewModel(private val session: RegistrationSession) : ViewModel() {

    private val _state = MutableStateFlow(CameraUiState())
    val state: StateFlow<CameraUiState> = _state.asStateFlow()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        session.clear()
        GenkiEval.log("cadastro_iniciado", "fonte" to DraftSource.CAMERA)
    }

    fun capturing() = _state.update { it.copy(phase = CameraPhase.READING, message = null) }

    fun captureFailed() = _state.update {
        it.copy(phase = CameraPhase.FAILED, message = "Não consegui tirar a foto.")
    }

    /** Lê o texto da foto; [rotation] em graus, como a câmera informa. */
    fun read(bitmap: Bitmap, rotation: Int) {
        val started = SystemClock.elapsedRealtime()
        recognizer.process(InputImage.fromBitmap(bitmap, rotation))
            .addOnSuccessListener { text ->
                val lines = text.textBlocks.flatMap { it.lines }.map { OcrLine(it.text, it.boundingBox?.height() ?: 0) }
                val draft = OcrParser.parse(lines)
                GenkiEval.log(
                    "ocr_resultado",
                    "texto" to "\"${lines.joinToString(" | ") { it.text }}\"",
                    "nome" to draft.name,
                    "concentracao" to draft.concentration,
                    "ms" to SystemClock.elapsedRealtime() - started,
                )
                _state.update {
                    if (draft.name == null && draft.concentration == null) {
                        it.copy(phase = CameraPhase.FAILED, message = "Não consegui ler a embalagem.")
                    } else {
                        it.copy(phase = CameraPhase.RESULT, name = draft.name.orEmpty(), concentration = draft.concentration.orEmpty())
                    }
                }
            }
            .addOnFailureListener { error ->
                GenkiEval.log("ocr_erro", "erro" to error.javaClass.simpleName)
                _state.update { it.copy(phase = CameraPhase.FAILED, message = "Não consegui ler a embalagem.") }
            }
    }

    fun editName(value: String) = _state.update { it.copy(name = value) }

    fun editConcentration(value: String) = _state.update { it.copy(concentration = value) }

    fun retry() = _state.update { CameraUiState() }

    /** Leva as sugestões conferidas para a voz ou o formulário; o resto é perguntado lá. */
    fun confirm() {
        val s = _state.value
        val draft = MedicationDraft(
            source = DraftSource.CAMERA,
            name = s.name.trim().ifEmpty { null },
            concentration = s.concentration.trim().ifEmpty { null },
        )
        GenkiEval.log("ocr_confirmado", "nome" to draft.name, "concentracao" to draft.concentration)
        session.set(listOf(draft))
    }

    override fun onCleared() = recognizer.close()

    companion object {
        val Factory = viewModelFactory {
            initializer { CameraViewModel(genkiContainer().registrationSession) }
        }
    }
}
