package com.app.relatosfutbolerosnd_20.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.relatosfutbolerosnd_20.ui.utils.KeyVisualTransformation
import org.koin.androidx.compose.koinViewModel

@Composable
fun LicenseScreen(
    onLicenseValidated: () -> Unit
) {
    val viewModel: LicenseViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    var licenseKey by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is LicenseUiState.Licensed) {
            onLicenseValidated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF161B22))
                )
            )
            .systemBarsPadding()
            .imePadding(), // Esto hace que el diseño se "empuje" hacia arriba con el teclado
        contentAlignment = Alignment.Center
    ) {
        // Envolvemos la Card en una Column con scroll para que todo sea deslizable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Activación",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Ingresa tus datos y la clave de licencia",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = uiState !is LicenseUiState.Validating
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = uiState !is LicenseUiState.Validating
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                    value = licenseKey,
                    onValueChange = { input ->
                        licenseKey = input.replace("-", "").uppercase()
                    },
                    label = { Text("Serial Key") },
                    visualTransformation = KeyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = uiState !is LicenseUiState.Validating
                    )

                    if (uiState is LicenseUiState.Error) {
                        Text(
                            text = (uiState as LicenseUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                    onClick = { 
                        val formattedKey = licenseKey.chunked(4).joinToString("-")
                        viewModel.activateLicense(formattedKey, firstName, lastName) 
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = uiState !is LicenseUiState.Validating && 
                                licenseKey.isNotBlank() && 
                                firstName.isNotBlank() && 
                                lastName.isNotBlank()
                    ) {
                        if (uiState is LicenseUiState.Validating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("ACTIVAR PRODUCTO", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}