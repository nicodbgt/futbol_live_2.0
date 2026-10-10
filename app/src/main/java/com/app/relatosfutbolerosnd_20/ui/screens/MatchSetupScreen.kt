package com.app.relatosfutbolerosnd_20.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.app.relatosfutbolerosnd_20.model.MatchConfig
import com.app.relatosfutbolerosnd_20.model.StreamResolution
import com.app.relatosfutbolerosnd_20.ui.utils.KeyVisualTransformation
import org.koin.androidx.compose.koinViewModel

enum class StreamPlatform(val displayName: String, val url: String, val icon: ImageVector, val color: Color) {
    YOUTUBE("YouTube", "rtmp://a.rtmp.youtube.com/live2", Icons.Default.PlayCircle, Color(0xFFFF0000)),
    FACEBOOK("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/", Icons.Default.Facebook, Color(0xFF1877F2)),
    TWITCH("Twitch", "rtmp://live.twitch.tv/app/", Icons.Default.VideogameAsset, Color(0xFF9146FF)),
    TIKTOK("TikTok", "rtmp://live-push.tiktok.com/live/", Icons.Default.MusicNote, Color(0xFFEE1D52)),
    CUSTOM("Otro (RTMP)", "", Icons.Default.Link, Color(0xFF6B7280))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MatchSetupScreen(
    navController: NavController,
    viewModel: MatchSetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var homeTeamColor by remember { mutableLongStateOf(0xFF1E40AF) }
    var homeTeamColor2 by remember { mutableLongStateOf(0xFFFFFFFF) }
    var awayTeamColor by remember { mutableLongStateOf(0xFFDC2626) }
    var awayTeamColor2 by remember { mutableLongStateOf(0xFFFFFFFF) }
    
    val teamColors = listOf(0xFF1E40AF, 0xFFDC2626, 0xFF059669, 0xFFD97706, 0xFF7C3AED, 0xFFDB2777, 0xFF0891B2, 0xFF4B5563, 0xFF000000, 0xFFFFFFFF, 0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF)

    var showColorDialog by remember { mutableStateOf(false) }
    var activeColorValue by remember { mutableLongStateOf(0L) }
    var onColorSelectedCallback by remember { mutableStateOf<((Long) -> Unit)?>(null) }

    var showPlayerDialog by remember { mutableStateOf(false) }
    var dialogIsHome by remember { mutableStateOf(true) }

    val logoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri -> viewModel.onRadioLogoChanged(uri?.toString()) }
    
    // Lanzador para cargar publicidades (múltiples imágenes)
    val adsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) { uris -> 
        viewModel.onAdsChanged(uris.map { it.toString() }) 
    }

    if (showColorDialog) {
        ColorSelectionDialog(colors = teamColors, selectedColor = activeColorValue, onColorSelected = { onColorSelectedCallback?.invoke(it); showColorDialog = false }, onDismiss = { showColorDialog = false })
    }

    if (showPlayerDialog) {
        PlayerEntryDialog(teamName = if (dialogIsHome) uiState.homeTeamName else uiState.awayTeamName, initialPlayers = if (dialogIsHome) uiState.homePlayers else uiState.awayPlayers, onConfirm = { players -> viewModel.onPlayersChanged(dialogIsHome, players); showPlayerDialog = false }, onDismiss = { showPlayerDialog = false })
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Relatos Futboleros ND", fontWeight = FontWeight.Bold) }) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // SECCIÓN: LOGO EMISORA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Logo Radio", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                if (uiState.radioLogoUri != null) {
                    TextButton(onClick = { viewModel.onRadioLogoChanged(null) }) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Quitar", color = MaterialTheme.colorScheme.error) }
                }
            }
            Card(modifier = Modifier.fillMaxWidth().height(85.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), onClick = { logoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    if (uiState.radioLogoUri != null) {
                        Image(painter = rememberAsyncImagePainter(uiState.radioLogoUri), contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                        Spacer(modifier = Modifier.width(16.dp)); Text("Logo cargado", fontSize = 14.sp)
                    } else { Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Seleccionar logo emisora") }
                }
            }

            // SECCIÓN: PUBLICIDAD (NUEVO)
            Text("Gestión de Publicidad", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Card(
                modifier = Modifier.fillMaxWidth().height(85.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                onClick = { adsLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Campaign, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(if(uiState.adUris.isEmpty()) "Cargar Patrocinadores" else "${uiState.adUris.size} Marcas Cargadas", fontWeight = FontWeight.Medium)
                }
            }

            // EQUIPO LOCAL Y VISITANTE (Mantenemos como estaba)
            TeamConfigCard(label = "Equipo Local", teamName = uiState.homeTeamName, onNameChange = { viewModel.onTeamNamesChanged(it, uiState.awayTeamName) }, playerCount = uiState.homePlayers.size, color1 = homeTeamColor, color2 = homeTeamColor2, onConfigurePlayers = { dialogIsHome = true; showPlayerDialog = true }, onPickColor1 = { activeColorValue = homeTeamColor; onColorSelectedCallback = { homeTeamColor = it }; showColorDialog = true }, onPickColor2 = { activeColorValue = homeTeamColor2; onColorSelectedCallback = { homeTeamColor2 = it }; showColorDialog = true })
            TeamConfigCard(label = "Equipo Visitante", teamName = uiState.awayTeamName, onNameChange = { viewModel.onTeamNamesChanged(uiState.homeTeamName, it) }, playerCount = uiState.awayPlayers.size, color1 = awayTeamColor, color2 = awayTeamColor2, onConfigurePlayers = { dialogIsHome = false; showPlayerDialog = true }, onPickColor1 = { activeColorValue = awayTeamColor; onColorSelectedCallback = { awayTeamColor = it }; showColorDialog = true }, onPickColor2 = { activeColorValue = awayTeamColor2; onColorSelectedCallback = { awayTeamColor2 = it }; showColorDialog = true })

            // PLATAFORMA Y BOTÓN INICIO
            Text("Plataforma de Transmisión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { StreamPlatform.entries.forEach { platform -> PlatformButton(platform, uiState.platform == platform, { viewModel.onPlatformSelected(platform) }, Modifier.weight(1f)) } }
            if (uiState.platform == StreamPlatform.CUSTOM) { OutlinedTextField(value = uiState.customUrl, onValueChange = { viewModel.onCustomUrlChanged(it) }, label = { Text("URL RTMP") }, modifier = Modifier.fillMaxWidth()) }
            OutlinedTextField(
                value = uiState.streamKey, 
                onValueChange = { viewModel.onStreamKeyChanged(it) }, 
                label = { Text("Stream Key") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    val finalUrl = if (uiState.platform == StreamPlatform.CUSTOM) uiState.customUrl else uiState.platform.url
                    val config = MatchConfig(
                        homeTeamName = uiState.homeTeamName.trim(), awayTeamName = uiState.awayTeamName.trim(),
                        homeTeamColor = homeTeamColor, homeTeamColor2 = homeTeamColor2,
                        awayTeamColor = awayTeamColor, awayTeamColor2 = awayTeamColor2,
                        rtmpUrl = finalUrl, streamKey = uiState.streamKey,
                        radioLogoUri = uiState.radioLogoUri, homePlayers = uiState.homePlayers, awayPlayers = uiState.awayPlayers,
                        adUris = uiState.adUris // Enviamos los anuncios
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set("matchConfig", config)
                    navController.navigate("live")
                },
                enabled = uiState.homeTeamName.isNotBlank() && uiState.awayTeamName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)
            ) { Text("INICIAR TRANSMISIÓN", fontWeight = FontWeight.Black) }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PlayerEntryDialog(teamName: String, initialPlayers: List<String>, onConfirm: (List<String>) -> Unit, onDismiss: () -> Unit) {
    var textValue by remember { mutableStateOf(initialPlayers.joinToString("\n")) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Plantilla: $teamName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Un jugador por línea", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = textValue, onValueChange = { textValue = it }, modifier = Modifier.fillMaxWidth().height(250.dp), placeholder = { Text("10 - MESSI...") })
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCELAR") }
                    Button(onClick = { val list = textValue.split("\n").map { it.trim() }.filter { it.isNotBlank() }; onConfirm(list) }) { Text("GUARDAR") }
                }
            }
        }
    }
}

@Composable
private fun TeamConfigCard(label: String, teamName: String, onNameChange: (String) -> Unit, playerCount: Int, color1: Long, color2: Long, onConfigurePlayers: () -> Unit, onPickColor1: () -> Unit, onPickColor2: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onConfigurePlayers) { Icon(Icons.Default.People, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(if(playerCount > 0) "$playerCount Jugadores" else "Cargar Plantilla", fontSize = 12.sp) }
            }
            OutlinedTextField(value = teamName, onValueChange = onNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorButton(label = "Principal", color = color1, onClick = onPickColor1, modifier = Modifier.weight(1f))
                ColorButton(label = "Secundario", color = color2, onClick = onPickColor2, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PlatformButton(platform: StreamPlatform, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier.height(70.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) platform.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), border = if (isSelected) BorderStroke(2.dp, platform.color) else null) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(platform.icon, null, tint = if (isSelected) platform.color else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(platform.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ColorButton(label: String, color: Long, onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(48.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(color)).border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape))
            Text(label, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSelectionDialog(colors: List<Long>, selectedColor: Long, onColorSelected: (Long) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Seleccionar Color", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(20.dp))
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEach { color ->
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(color)).border(width = if (selectedColor == color) 3.dp else 1.dp, color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f), shape = CircleShape).clickable { onColorSelected(color) })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("CANCELAR") }
            }
        }
    }
}
