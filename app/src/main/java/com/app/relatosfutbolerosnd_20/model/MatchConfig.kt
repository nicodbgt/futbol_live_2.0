package com.app.relatosfutbolerosnd_20.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MatchConfig(
    val homeTeamName: String = "",
    val awayTeamName: String = "",
    val homeTeamColor: Long = 0xFF1E40AF,
    val homeTeamColor2: Long = 0xFFFFFFFF,
    val awayTeamColor: Long = 0xFFDC2626,
    val awayTeamColor2: Long = 0xFFFFFFFF,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val rtmpUrl: String = "",
    val streamKey: String = "",
    val radioLogoUri: String? = null,
    val matchTitle: String = "",
    val streamResolution: StreamResolution = StreamResolution.HD_720P,
    val homePlayers: List<String> = emptyList(),
    val awayPlayers: List<String> = emptyList(),
    // Lista de rutas de imágenes publicitarias
    val adUris: List<String> = emptyList()
) : Parcelable {
    val fullRtmpUrl: String
        get() = if (streamKey.isNotBlank()) "$rtmpUrl/$streamKey" else rtmpUrl
}

enum class StreamResolution(val width: Int, val height: Int, val displayName: String) {
    SD_480P(848, 480, "480p"),
    HD_720P(1280, 720, "720p"),
    HD_1080P(1920, 1080, "1080p");
}
