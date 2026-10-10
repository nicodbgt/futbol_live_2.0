package com.app.relatosfutbolerosnd_20.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.relatosfutbolerosnd_20.data.repository.LicenseRepository
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val repository: LicenseRepository = koinInject()

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        
        if (repository.isLicenseActiveLocally()) {
            // Si hay licencia local, VERIFICAMOS obligatoriamente con el servidor
            repository.verifyLicenseWithServer()
                .onSuccess {
                    ensureMinDisplayTime(startTime)
                    onNavigate("setup")
                }
                .onFailure {
                    ensureMinDisplayTime(startTime)
                    onNavigate("license")
                }
        } else {
            // Si no hay licencia local, vamos directo a activar
            ensureMinDisplayTime(startTime)
            onNavigate("license")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Verificando licencia...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private suspend fun ensureMinDisplayTime(startTime: Long) {
    val elapsed = System.currentTimeMillis() - startTime
    if (elapsed < 1200) {
        delay(1200 - elapsed)
    }
}