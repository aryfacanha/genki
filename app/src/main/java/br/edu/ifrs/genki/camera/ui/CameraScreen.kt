package br.edu.ifrs.genki.camera.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.CameraIcon
import br.edu.ifrs.genki.ui.components.FieldError
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.components.genkiFieldColors

/**
 * Usar câmera (`UsarCameraMedicamento.tsx`, ESPEC §8): foto da embalagem, sugestões de nome e concentração
 * editáveis e continuação por voz ou digitando (RF20–RF23). Falha → "Tentar de novo" ou "Digitar".
 */
@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onVoice: () -> Unit,
    onType: () -> Unit,
    viewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permitted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var denied by remember { mutableStateOf(false) }
    val askCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permitted = granted
        denied = !granted
    }
    LaunchedEffect(Unit) { if (!permitted) askCamera.launch(Manifest.permission.CAMERA) }

    Column(Modifier.fillMaxSize()) {
        GenkiTopBar("Usar câmera", onBack = onClose, actionLabel = "Cancelar", onAction = onClose)
        when {
            denied -> Message(
                "Sem acesso à câmera. Você pode permitir nas configurações ou digitar.",
                primary = "Digitar" to onType,
                secondary = "Voltar" to onClose,
            )
            !permitted -> Unit
            state.phase == CameraPhase.CAPTURE -> Capture(viewModel, onType)
            state.phase == CameraPhase.READING -> Reading()
            state.phase == CameraPhase.RESULT -> Result(
                state,
                viewModel,
                onVoice = { viewModel.confirm(); onVoice() },
                onType = { viewModel.confirm(); onType() },
            )
            else -> Message(
                state.message ?: "Não consegui ler a embalagem.",
                help = "Tente com mais luz, com o nome do medicamento de frente para a câmera.",
                primary = "Tentar de novo" to viewModel::retry,
                secondary = "Digitar" to onType,
            )
        }
    }
}

@Composable
private fun ColumnScope.Capture(viewModel: CameraViewModel, onType: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var taking by remember { mutableStateOf(false) }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val selector = if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            } catch (_: Exception) {
                viewModel.captureFailed()
            }
        }, ContextCompat.getMainExecutor(context))
        // A câmera desliga ao sair da foto (leitura, resultado ou outra tela).
        onDispose { if (future.isDone) future.get().unbindAll() }
    }

    Text(
        "Aponte para o nome do medicamento na embalagem.",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
    )
    AndroidView(factory = { previewView }, modifier = Modifier.weight(1f).fillMaxWidth())
    BottomActions {
        PrimaryButton("Tirar foto", icon = CameraIcon, onClick = {
            if (taking) return@PrimaryButton
            taking = true
            // A fase muda só com a foto pronta: sair da pré-visualização desliga a câmera e cancelaria a captura.
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val rotation = image.imageInfo.rotationDegrees
                        val bitmap = image.toBitmap()
                        image.close()
                        viewModel.capturing()
                        viewModel.read(bitmap, rotation)
                    }

                    override fun onError(exception: ImageCaptureException) = viewModel.captureFailed()
                },
            )
        })
        SecondaryButton("Digitar", onClick = onType)
    }
}

@Composable
private fun ColumnScope.Reading() {
    Column(
        Modifier.weight(1f).fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Lendo a embalagem…", style = MaterialTheme.typography.titleMedium)
    }
}

/** Sugestões editáveis; dose, dias, horários e duração são informados depois (RF23). */
@Composable
private fun ColumnScope.Result(state: CameraUiState, viewModel: CameraViewModel, onVoice: () -> Unit, onType: () -> Unit) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Confira o que eu li", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Corrija se precisar. Quanto você toma, os dias, os horários e a duração você informa em seguida.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        Field("Nome do medicamento", state.name, viewModel::editName)
        Spacer(Modifier.height(16.dp))
        Field("Concentração (opcional)", state.concentration, viewModel::editConcentration)
        SecondaryButton("Tirar outra foto", onClick = viewModel::retry, modifier = Modifier.padding(top = 20.dp))
    }
    BottomActions {
        PrimaryButton("Continuar por voz", onClick = onVoice)
        SecondaryButton("Continuar digitando", onClick = onType)
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        colors = genkiFieldColors(),
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ColumnScope.Message(
    text: String,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>,
    help: String? = null,
) {
    Column(Modifier.weight(1f).padding(20.dp)) {
        FieldError(text)
        if (help != null) Text(help, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
    BottomActions {
        PrimaryButton(primary.first, onClick = primary.second)
        SecondaryButton(secondary.first, onClick = secondary.second)
    }
}
