package com.app.relatosfutbolerosnd_20.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.sources.audio.MicrophoneSource
import com.pedro.encoder.input.sources.video.Camera2Source
import com.pedro.encoder.utils.gl.TranslateTo
import com.pedro.library.generic.GenericStream
import com.app.relatosfutbolerosnd_20.model.MatchConfig
import com.app.relatosfutbolerosnd_20.ui.stream.ScoreboardBitmapGenerator
import com.app.relatosfutbolerosnd_20.ui.stream.ScoreboardObjectFilterRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface StreamStatus {
    data object Idle : StreamStatus
    data object Connecting : StreamStatus
    data object Streaming : StreamStatus
    data class Error(val message: String) : StreamStatus
}

data class CautionAlert(
    val playerName: String = "",
    val cardColor: Color = Color.Yellow,
    val isVisible: Boolean = false,
    val alpha: Float = 0f,
    val isHome: Boolean = true
)

data class AdAlert(
    val bitmap: Bitmap? = null,
    val isVisible: Boolean = false,
    val alpha: Float = 0f
)

data class LiveStreamUiState(
    val matchConfig: MatchConfig = MatchConfig(),
    val streamStatus: StreamStatus = StreamStatus.Idle,
    val elapsedSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeRedCards: Int = 0,
    val awayRedCards: Int = 0,
    val extraTime: Int = 0,
    val homeCautions: List<Pair<String, Color>> = emptyList(),
    val awayCautions: List<Pair<String, Color>> = emptyList(),
    val cautionAlert: CautionAlert = CautionAlert(),
    val adAlert: AdAlert = AdAlert(), // Nuevo estado para publicidad
    val isCameraAvailable: Boolean = true,
    val cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
    val streamBitrate: Long = 0L,
    val hasCameraPermission: Boolean = false,
    val hasAudioPermission: Boolean = false
)

class LiveStreamViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LiveStreamUiState())
    val uiState: StateFlow<LiveStreamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var genericStream: GenericStream? = null
    private val scoreboardFilter = ScoreboardObjectFilterRender()
    private var currentTextureView: TextureView? = null
    private var radioLogoBitmap: Bitmap? = null
    private val loadedAds = mutableMapOf<String, Bitmap>()

    private val connectChecker = object : ConnectChecker {
        override fun onConnectionStarted(url: String) { _uiState.update { it.copy(streamStatus = StreamStatus.Connecting) } }
        override fun onConnectionSuccess() { _uiState.update { it.copy(streamStatus = StreamStatus.Streaming) } }
        override fun onNewBitrate(bitrate: Long) { _uiState.update { it.copy(streamBitrate = bitrate) } }
        override fun onConnectionFailed(reason: String) { 
            Log.e("LiveStreamVM", "Connection failed: $reason")
            genericStream?.stopStream()
            _uiState.update { it.copy(streamStatus = StreamStatus.Error(reason)) } 
        }
        override fun onDisconnect() { _uiState.update { it.copy(streamStatus = StreamStatus.Idle, streamBitrate = 0L) } }
        override fun onAuthError() { _uiState.update { it.copy(streamStatus = StreamStatus.Error("Error de autenticación")) } }
        override fun onAuthSuccess() {}
    }

    fun setMatchConfig(config: MatchConfig) {
        _uiState.update { it.copy(matchConfig = config, homeScore = config.homeScore, awayScore = config.awayScore) }
        loadRadioLogo(config.radioLogoUri)
        preloadAds(config.adUris)
    }

    private fun loadRadioLogo(uriString: String?) {
        if (uriString == null) { radioLogoBitmap = null; return }
        val cleanUri = uriString.substringBefore("?")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(getApplication())
                val request = ImageRequest.Builder(getApplication()).data(Uri.parse(cleanUri)).allowHardware(false).build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val image = result.image
                    radioLogoBitmap = image.toBitmap(image.width, image.height, Bitmap.Config.ARGB_8888).apply { setHasAlpha(true) }
                    withContext(Dispatchers.Main) { updateScoreboardOverlay() }
                }
            } catch (e: Exception) { Log.e("LiveStreamVM", "Error cargando logo radio", e) }
        }
    }

    private fun preloadAds(uris: List<String>) {
        uris.forEach { uri ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(getApplication())
                    val request = ImageRequest.Builder(getApplication()).data(Uri.parse(uri)).allowHardware(false).build()
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val image = result.image
                        loadedAds[uri] = image.toBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun showAd(uri: String) {
        val bitmap = loadedAds[uri] ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(adAlert = AdAlert(bitmap, true, 0f)) }
            for (i in 1..10) { delay(30); _uiState.update { it.copy(adAlert = it.adAlert.copy(alpha = i / 10f)) }; updateScoreboardOverlay() }
            delay(6000) // Publicidad visible por 6 segundos
            for (i in 10 downTo 0) { delay(30); _uiState.update { it.copy(adAlert = it.adAlert.copy(alpha = i / 10f)) }; updateScoreboardOverlay() }
            _uiState.update { it.copy(adAlert = AdAlert(isVisible = false)) }
            updateScoreboardOverlay()
        }
    }

    fun setPermissions(camera: Boolean, audio: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = camera, hasAudioPermission = audio) }
        if (camera && currentTextureView != null) startPreviewInternal()
    }

    fun initStream(context: Context, view: View) {
        val textureView = view as? TextureView ?: return
        if (currentTextureView == textureView && genericStream != null) return
        currentTextureView = textureView
        if (genericStream == null) {
            try {
                genericStream = GenericStream(context, connectChecker, Camera2Source(context), MicrophoneSource()).apply { getGlInterface().autoHandleOrientation = true }
                genericStream?.getGlInterface()?.setFilter(scoreboardFilter)
                // Usamos el 100% de la pantalla para el lienzo de resolución nativa.
                // Esto garantiza nitidez total y tamaño real del marcador.
                scoreboardFilter.setScale(100f, 100f)
                scoreboardFilter.setPosition(TranslateTo.TOP)
            } catch (e: Exception) { Log.e("LiveStreamVM", "Error init", e) }
        }
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) { startPreviewInternal() }
            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) { genericStream?.getGlInterface()?.setPreviewResolution(w, h) }
            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean = false
            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
        }
        if (textureView.isAvailable) startPreviewInternal()
    }

    private fun startPreviewInternal() {
        val stream = genericStream ?: return
        val view = currentTextureView ?: return
        val state = _uiState.value
        if (!state.hasCameraPermission) return
        try {
            val config = state.matchConfig
            val bitrate = when (config.streamResolution.width) { 1920 -> 4500 * 1024; 1280 -> 2800 * 1024; else -> 1500 * 1024 }
            if (!stream.isOnPreview && !stream.isStreaming) {
                if (stream.prepareVideo(config.streamResolution.width, config.streamResolution.height, bitrate, 30, 2, 0) && stream.prepareAudio(44100, true, 128 * 1024)) { stream.startPreview(view) }
            } else if (stream.isOnPreview && !stream.isStreaming) { stream.startPreview(view) }
            updateScoreboardOverlay()
        } catch (e: Exception) { Log.e("LiveStreamVM", "Error preview", e) }
    }

    private fun updateScoreboardOverlay() {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                val bitmap = ScoreboardBitmapGenerator.generate(
                    state.matchConfig.homeTeamName, state.matchConfig.awayTeamName,
                    state.homeScore, state.awayScore,
                    state.matchConfig.homeTeamColor.toInt(), state.matchConfig.homeTeamColor2.toInt(),
                    state.matchConfig.awayTeamColor.toInt(), state.matchConfig.awayTeamColor2.toInt(),
                    formatElapsedTime(state.elapsedSeconds), state.isTimerRunning,
                    radioLogo = radioLogoBitmap,
                    cautionAlert = state.cautionAlert,
                    adAlert = state.adAlert, // Pasamos el anuncio
                    homeRedCards = state.homeRedCards,
                    awayRedCards = state.awayRedCards,
                    extraTime = state.extraTime,
                    streamWidth = state.matchConfig.streamResolution.width
                )
                scoreboardFilter.updateScoreboard(bitmap)
            } catch (e: Exception) { Log.e("LiveStreamVM", "Error update scoreboard", e) }
        }
    }

    fun addCaution(isHome: Boolean, name: String, color: Color) {
        if (isHome) { _uiState.update { it.copy(homeCautions = it.homeCautions + (name to color), homeRedCards = if (color == Color.Red) it.homeRedCards + 1 else it.homeRedCards) } } else { _uiState.update { it.copy(awayCautions = it.awayCautions + (name to color), awayRedCards = if (color == Color.Red) it.awayRedCards + 1 else it.awayRedCards) } }
        viewModelScope.launch {
            _uiState.update { it.copy(cautionAlert = CautionAlert(name, color, true, 0f, isHome)) }
            for (i in 1..10) { delay(30); _uiState.update { it.copy(cautionAlert = it.cautionAlert.copy(alpha = i / 10f)) }; updateScoreboardOverlay() }
            delay(4000); for (i in 10 downTo 0) { delay(30); _uiState.update { it.copy(cautionAlert = it.cautionAlert.copy(alpha = i / 10f)) }; updateScoreboardOverlay() }
            _uiState.update { it.copy(cautionAlert = CautionAlert(isVisible = false)) }; updateScoreboardOverlay()
        }
    }

    fun startStream() {
        val state = _uiState.value
        val rtmpUrl = state.matchConfig.rtmpUrl.trim().replace(" ", "")
        val streamKey = state.matchConfig.streamKey.trim().replace(" ", "")
        if (rtmpUrl.isBlank() || streamKey.isBlank()) return
        val baseUrl = rtmpUrl.removeSuffix("/"); val cleanKey = streamKey.removePrefix("/"); val fullUrl = "$baseUrl/$cleanKey"
        viewModelScope.launch { try { val stream = genericStream ?: return@launch; if (!stream.isStreaming) { stream.startStream(fullUrl); updateScoreboardOverlay() } } catch (e: Exception) { _uiState.update { it.copy(streamStatus = StreamStatus.Error(e.message ?: "Error")) } } }
    }

    fun stopStream() { genericStream?.stopStream(); _uiState.update { it.copy(streamStatus = StreamStatus.Idle, streamBitrate = 0L) } }
    fun switchCamera(context: Context) { try { (genericStream?.videoSource as? Camera2Source)?.switchCamera(); val newFacing = if (_uiState.value.cameraFacing == CameraCharacteristics.LENS_FACING_BACK) CameraCharacteristics.LENS_FACING_FRONT else CameraCharacteristics.LENS_FACING_BACK; _uiState.update { it.copy(cameraFacing = newFacing) } } catch (e: Exception) { Log.e("LiveStreamVM", "Error switch", e) } }
    fun setZoom(scaleFactor: Float) { try { val cameraSource = genericStream?.videoSource as? Camera2Source ?: return; val currentZoom = cameraSource.getZoom(); val range = cameraSource.getZoomRange(); val adjustedScale = 1f + (scaleFactor - 1f) * 1.5f; cameraSource.setZoom((currentZoom * adjustedScale).coerceIn(range.lower, range.upper)) } catch (_: Exception) {} }
    fun toggleTimer() { if (_uiState.value.isTimerRunning) { timerJob?.cancel(); _uiState.update { it.copy(isTimerRunning = false) } } else { _uiState.update { it.copy(isTimerRunning = true) }; timerJob = viewModelScope.launch { while (true) { delay(1000); _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }; updateScoreboardOverlay() } } }; updateScoreboardOverlay() }
    fun resetTimer() { timerJob?.cancel(); _uiState.update { it.copy(elapsedSeconds = 0, isTimerRunning = false) }; updateScoreboardOverlay() }
    fun incrementHome() { _uiState.update { it.copy(homeScore = it.homeScore + 1) }; updateScoreboardOverlay() }
    fun decrementHome() { _uiState.update { it.copy(homeScore = maxOf(0, it.homeScore - 1)) }; updateScoreboardOverlay() }
    fun incrementAway() { _uiState.update { it.copy(awayScore = it.awayScore + 1) }; updateScoreboardOverlay() }
    fun decrementAway() { _uiState.update { it.copy(awayScore = maxOf(0, it.awayScore - 1)) }; updateScoreboardOverlay() }
    fun incrementExtraTime() { _uiState.update { it.copy(extraTime = it.extraTime + 1) }; updateScoreboardOverlay() }
    fun decrementExtraTime() { _uiState.update { it.copy(extraTime = maxOf(0, it.extraTime - 1)) }; updateScoreboardOverlay() }
    fun clearError() { _uiState.update { it.copy(streamStatus = StreamStatus.Idle) } }
    override fun onCleared() { super.onCleared(); timerJob?.cancel(); genericStream?.stopStream(); genericStream?.stopPreview() }
    companion object { fun formatElapsedTime(seconds: Int): String = String.format("%02d:%02d", seconds / 60, seconds % 60) }
}
