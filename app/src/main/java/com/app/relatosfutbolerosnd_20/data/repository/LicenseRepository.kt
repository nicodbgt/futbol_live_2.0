package com.app.relatosfutbolerosnd_20.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class LicenseRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "license_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val firestore = FirebaseFirestore.getInstance()

    fun getDeviceId(): String {
        var id = sharedPreferences.getString("device_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            sharedPreferences.edit().putString("device_id", id).apply()
        }
        return id
    }

    fun isLicenseActiveLocally(): Boolean {
        return sharedPreferences.getBoolean("is_active", false)
    }

    private fun getSavedLicenseKey(): String? {
        return sharedPreferences.getString("license_key", null)
    }

    /**
     * Verifica con el servidor si la licencia sigue siendo válida para este dispositivo.
     */
    suspend fun verifyLicenseWithServer(): Result<Unit> {
        val savedKey = getSavedLicenseKey() ?: return Result.failure(Exception("No hay clave guardada"))
        
        return try {
            val deviceId = getDeviceId()
            val docRef = firestore.collection("licencias").document(savedKey)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                clearLocalLicense()
                return Result.failure(Exception("La licencia ya no existe en el sistema"))
            }

            val isUsed = snapshot.getBoolean("is_used") ?: false
            val storedDeviceId = snapshot.getString("device_id")
            val expirationDate = snapshot.getTimestamp("fecha_vencimiento")

            // 1. Verificación de ID de dispositivo
            if (!isUsed || storedDeviceId != deviceId) {
                clearLocalLicense()
                return Result.failure(Exception("Licencia vinculada a otro dispositivo"))
            }

            // 2. Verificación de Vencimiento
            if (expirationDate != null && expirationDate.toDate().before(Calendar.getInstance().time)) {
                clearLocalLicense()
                return Result.failure(Exception("Su licencia ha vencido"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateLicense(key: String, firstName: String, lastName: String): Result<Unit> {
        return try {
            val deviceId = getDeviceId()
            val docRef = firestore.collection("licencias").document(key)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("La clave no existe"))
            }

            val isUsed = snapshot.getBoolean("is_used") ?: false
            val storedDeviceId = snapshot.getString("device_id")
            val validDays = snapshot.getLong("dias_validos") ?: 30L
            var expirationDate = snapshot.getTimestamp("fecha_vencimiento")

            // Caso: Ya está en uso por otro dispositivo
            if (isUsed && (storedDeviceId != deviceId)) {
                return Result.failure(Exception("Esta clave ya está en uso en otro dispositivo"))
            }

            // Caso: Primera vez que se usa la clave
            if (!isUsed) {
                // Si tú ya pusiste una fecha manual en Firestore, la usamos. 
                // Si está vacío (null), calculamos usando dias_validos.
                if (expirationDate == null) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, validDays.toInt())
                    expirationDate = Timestamp(calendar.time)
                }
                
                docRef.update(
                    mapOf(
                        "is_used" to true,
                        "device_id" to deviceId,
                        "user_first_name" to firstName,
                        "user_last_name" to lastName,
                        "activation_date" to Timestamp.now(),
                        "fecha_vencimiento" to expirationDate
                    )
                ).await()
            }

            // Caso: Re-activación o Verificación inicial (comprobar si ya venció)
            if (expirationDate != null && expirationDate.toDate().before(Calendar.getInstance().time)) {
                return Result.failure(Exception("Esta licencia ya ha vencido"))
            }

            // Guardar localmente si todo está bien
            sharedPreferences.edit()
                .putBoolean("is_active", true)
                .putString("license_key", key)
                .apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun clearLocalLicense() {
        sharedPreferences.edit()
            .putBoolean("is_active", false)
            .putString("license_key", null)
            .apply()
    }
}