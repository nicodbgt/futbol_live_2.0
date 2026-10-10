package com.app.relatosfutbolerosnd_20.ui.screens

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.app.relatosfutbolerosnd_20.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream

data class MatchSetupUiState(
    val platform: StreamPlatform = StreamPlatform.YOUTUBE,
    val customUrl: String = "",
    val streamKey: String = "",
    val radioLogoUri: String? = null,
    val homeTeamName: String = "",
    val awayTeamName: String = "",
    val homePlayers: List<String> = emptyList(),
    val awayPlayers: List<String> = emptyList(),
    val adUris: List<String> = emptyList()
)

class MatchSetupViewModel(
    application: Application,
    private val repository: SettingsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MatchSetupUiState())
    val uiState: StateFlow<MatchSetupUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val savedPlatformName = repository.getPlatform()
        val platform = try { StreamPlatform.valueOf(savedPlatformName) } catch (e: Exception) { StreamPlatform.YOUTUBE }
        
        _uiState.update { 
            it.copy(
                platform = platform,
                customUrl = repository.getCustomUrl(),
                streamKey = repository.getStreamKey(platform.name),
                radioLogoUri = repository.getRadioLogoUri(),
                homeTeamName = repository.getHomeTeamName(),
                awayTeamName = repository.getAwayTeamName(),
                homePlayers = repository.getPlayers(true),
                awayPlayers = repository.getPlayers(false),
                adUris = repository.getAds()
            )
        }
    }

    fun onPlatformSelected(platform: StreamPlatform) {
        val savedKey = repository.getStreamKey(platform.name)
        _uiState.update { it.copy(platform = platform, streamKey = savedKey) }
        repository.savePlatform(platform.name)
    }

    fun onCustomUrlChanged(url: String) {
        _uiState.update { it.copy(customUrl = url) }
        repository.saveCustomUrl(url)
    }

    fun onStreamKeyChanged(key: String) {
        val currentPlatform = _uiState.value.platform.name
        _uiState.update { it.copy(streamKey = key) }
        repository.saveStreamKey(currentPlatform, key)
    }

    fun onTeamNamesChanged(home: String, away: String) {
        _uiState.update { it.copy(homeTeamName = home, awayTeamName = away) }
        repository.saveTeamNames(home, away)
    }

    fun onPlayersChanged(isHome: Boolean, players: List<String>) {
        if (isHome) { _uiState.update { it.copy(homePlayers = players) } } else { _uiState.update { it.copy(awayPlayers = players) } }
        repository.savePlayers(isHome, players)
    }

    fun onAdsChanged(uriStrings: List<String>) {
        if (uriStrings.isEmpty()) {
            _uiState.update { it.copy(adUris = emptyList()) }
            repository.saveAds(emptyList())
            return
        }

        try {
            val context = getApplication<Application>()
            val adsDir = File(context.filesDir, "ads").apply { if (!exists()) mkdirs() }
            
            val savedInternalUris = mutableListOf<String>()
            val timestamp = System.currentTimeMillis()

            uriStrings.forEachIndexed { index, uriString ->
                if (uriString.startsWith("file://")) {
                    savedInternalUris.add(uriString)
                    return@forEachIndexed
                }

                try {
                    val uri = Uri.parse(uriString)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val adFile = File(adsDir, "ad_${timestamp}_$index.png")
                    val outputStream = FileOutputStream(adFile)
                    inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
                    val internalUri = Uri.fromFile(adFile).toString()
                    savedInternalUris.add(internalUri)
                } catch (e: Exception) {
                    Log.e("MatchSetupVM", "Error copying ad image: $uriString", e)
                }
            }

            _uiState.update { it.copy(adUris = savedInternalUris) }
            repository.saveAds(savedInternalUris)
        } catch (e: Exception) {
            Log.e("MatchSetupVM", "Error processing ads", e)
        }
    }

    fun onRadioLogoChanged(uriString: String?) {
        if (uriString == null) {
            _uiState.update { it.copy(radioLogoUri = null) }
            repository.saveRadioLogoUri(null)
            return
        }

        try {
            val context = getApplication<Application>()
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val logoFile = File(context.filesDir, "radio_logo.png")
            val outputStream = FileOutputStream(logoFile)
            inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
            val internalUri = Uri.fromFile(logoFile).toString()
            val previewUri = "$internalUri?t=${System.currentTimeMillis()}"
            _uiState.update { it.copy(radioLogoUri = previewUri) }
            repository.saveRadioLogoUri(internalUri)
        } catch (e: Exception) {
            android.util.Log.e("MatchSetupVM", "Error copying logo", e)
        }
    }
}
