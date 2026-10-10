package com.app.relatosfutbolerosnd_20.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "stream_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveStreamKey(platform: String, key: String) {
        sharedPreferences.edit().putString("stream_key_$platform", key).apply()
    }

    fun getStreamKey(platform: String): String {
        return sharedPreferences.getString("stream_key_$platform", "") ?: ""
    }

    fun savePlatform(platformName: String) {
        sharedPreferences.edit().putString("platform_name", platformName).apply()
    }

    fun getPlatform(): String {
        return sharedPreferences.getString("platform_name", "YOUTUBE") ?: "YOUTUBE"
    }

    fun saveCustomUrl(url: String) {
        sharedPreferences.edit().putString("custom_url", url).apply()
    }

    fun getCustomUrl(): String {
        return sharedPreferences.getString("custom_url", "") ?: ""
    }

    fun saveRadioLogoUri(uri: String?) {
        sharedPreferences.edit().putString("radio_logo_uri", uri).apply()
    }

    fun getRadioLogoUri(): String? {
        return sharedPreferences.getString("radio_logo_uri", null)
    }

    fun saveTeamNames(home: String, away: String) {
        sharedPreferences.edit()
            .putString("home_team_name", home)
            .putString("away_team_name", away)
            .apply()
    }

    fun getHomeTeamName(): String = sharedPreferences.getString("home_team_name", "") ?: ""
    fun getAwayTeamName(): String = sharedPreferences.getString("away_team_name", "") ?: ""

    fun savePlayers(isHome: Boolean, players: List<String>) {
        val key = if (isHome) "home_players" else "away_players"
        sharedPreferences.edit().putString(key, players.joinToString("|")).apply()
    }

    fun getPlayers(isHome: Boolean): List<String> {
        val key = if (isHome) "home_players" else "away_players"
        val saved = sharedPreferences.getString(key, "") ?: ""
        return if (saved.isBlank()) emptyList() else saved.split("|")
    }

    fun saveAds(adUris: List<String>) {
        sharedPreferences.edit().putString("ad_uris", adUris.joinToString("|")).apply()
    }

    fun getAds(): List<String> {
        val saved = sharedPreferences.getString("ad_uris", "") ?: ""
        return if (saved.isBlank()) emptyList() else saved.split("|")
    }
}