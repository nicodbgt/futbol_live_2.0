package com.app.relatosfutbolerosnd_20.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil3.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavController
import com.app.relatosfutbolerosnd_20.model.MatchConfig

@Composable
fun LiveStreamScreen(
    navController: NavController,
    matchConfig: MatchConfig,
    viewModel: LiveStreamViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    var showPlayerSelector by remember { mutableStateOf(false) }
    var selectorIsHome by remember { mutableStateOf(true) }
    
    // Estado para selector de publicidad
    var showAdSelector by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        if (activity != null && window != null) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        onDispose {
            if (activity != null && window != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(matchConfig) { viewModel.setMatchConfig(matchConfig) }
    val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val audioPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(cameraPermission, audioPermission) { viewModel.setPermissions(cameraPermission, audioPermission) }
    DisposableEffect(lifecycleOwner) { val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) viewModel.stopStream() }; lifecycleOwner.lifecycle.addObserver(observer); onDispose { lifecycleOwner.lifecycle.removeObserver(observer) } }
    BackHandler { if (uiState.streamStatus is StreamStatus.Streaming) showExitDialog = true else { viewModel.stopStream(); navController.popBackStack() } }

    if (showPlayerSelector) {
        PlayerSelectionDialog(teamName = if (selectorIsHome) matchConfig.homeTeamName else matchConfig.awayTeamName, players = if (selectorIsHome) matchConfig.homePlayers else matchConfig.awayPlayers, onPlayerSelected = { name, color -> viewModel.addCaution(selectorIsHome, name, color); showPlayerSelector = false }, onDismiss = { showPlayerSelector = false })
    }

    // --- DIÁLOGO DE SELECCIÓN DE PUBLICIDAD ---
    if (showAdSelector) {
        AdSelectionDialog(
            adUris = matchConfig.adUris,
            onAdSelected = { uri -> viewModel.showAd(uri); showAdSelector = false },
            onDismiss = { showAdSelector = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTransformGestures { _, _, zoom, _ -> if (zoom != 1f) viewModel.setZoom(zoom) } }) {
        if (cameraPermission && uiState.isCameraAvailable) { AndroidView(factory = { ctx -> TextureView(ctx).apply { viewModel.initStream(ctx, this) } }, modifier = Modifier.fillMaxSize()) }
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.2f), Color.Transparent, Color.Black.copy(0.4f)))))
            StreamTopBar(onBack = { if (uiState.streamStatus is StreamStatus.Streaming) showExitDialog = true else navController.popBackStack() }, onSwitchCamera = { viewModel.switchCamera(context) }, modifier = Modifier.align(Alignment.TopCenter))
            
            StreamControls(
                isStreaming = uiState.streamStatus is StreamStatus.Streaming,
                isConnecting = uiState.streamStatus is StreamStatus.Connecting,
                isTimerRunning = uiState.isTimerRunning,
                onToggleStream = { if (uiState.streamStatus is StreamStatus.Idle) viewModel.startStream() else viewModel.stopStream() },
                onToggleTimer = { viewModel.toggleTimer() },
                onResetTimer = { viewModel.resetTimer() },
                onHomeUp = { viewModel.incrementHome() },
                onHomeDown = { viewModel.decrementHome() },
                onAwayUp = { viewModel.incrementAway() },
                onAwayDown = { viewModel.decrementAway() },
                onExtraTimeUp = { viewModel.incrementExtraTime() },
                onExtraTimeDown = { viewModel.decrementExtraTime() },
                onCautionClick = { isHome -> selectorIsHome = isHome; showPlayerSelector = true },
                onAdClick = { showAdSelector = true }, // Abrir publicidad
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            if (uiState.streamStatus is StreamStatus.Streaming && uiState.streamBitrate > 0) { Text("${uiState.streamBitrate / 1024} kbps", color = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }

    if (uiState.streamStatus is StreamStatus.Error) { AlertDialog(onDismissRequest = { viewModel.clearError() }, title = { Text("Error") }, text = { Text((uiState.streamStatus as StreamStatus.Error).message) }, confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }) }
    if (showExitDialog) { AlertDialog(onDismissRequest = { showExitDialog = false }, title = { Text("¿Detener transmisión?") }, text = { Text("¿Deseas detener el stream y salir?") }, confirmButton = { TextButton(onClick = { showExitDialog = false; viewModel.stopStream(); navController.popBackStack() }) { Text("Sí, salir") } }, dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Cancelar") } }) }
}

@Composable
private fun AdSelectionDialog(adUris: List<String>, onAdSelected: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.7f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C2C))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Lanzar Publicidad", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                if (adUris.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay publicidades cargadas", color = Color.White.copy(0.5f)) }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(adUris) { uri ->
                            Card(onClick = { onAdSelected(uri) }, modifier = Modifier.height(100.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f))) {
                                Image(painter = rememberAsyncImagePainter(uri), contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("CERRAR") } }
            }
        }
    }
}

@Composable
private fun PlayerSelectionDialog(teamName: String, players: List<String>, onPlayerSelected: (String, Color) -> Unit, onDismiss: () -> Unit) {
    var selectedPlayer by remember { mutableStateOf<String?>(null) }; var cardColor by remember { mutableStateOf(Color.Yellow) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.85f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C2C))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Sancionar: $teamName", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp); Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = { cardColor = Color.Yellow }, colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow), modifier = Modifier.weight(1f).height(40.dp), border = if(cardColor == Color.Yellow) BorderStroke(2.dp, Color.White) else null) {}; Button(onClick = { cardColor = Color.Red }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.weight(1f).height(40.dp), border = if(cardColor == Color.Red) BorderStroke(2.dp, Color.White) else null) {} }
                Spacer(Modifier.height(16.dp)); Text("Selecciona Jugador:", color = Color.White.copy(0.6f), fontSize = 12.sp)
                if (players.isNotEmpty()) { LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) { items(players) { player -> Card(onClick = { selectedPlayer = player }, colors = CardDefaults.cardColors(containerColor = if(selectedPlayer == player) Color.White.copy(0.15f) else Color.Transparent), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) { Text(player, color = if(selectedPlayer == player) Color.Cyan else Color.White, modifier = Modifier.padding(12.dp), fontWeight = if(selectedPlayer == player) FontWeight.Bold else FontWeight.Normal) } } } }
                else { var manualName by remember { mutableStateOf("") }; OutlinedTextField(value = manualName, onValueChange = { manualName = it; selectedPlayer = it }, label = { Text("Escribir nombre") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)); Spacer(Modifier.weight(1f)) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("CANCELAR") }; Button(onClick = { selectedPlayer?.let { onPlayerSelected(it, cardColor) } }, enabled = selectedPlayer != null) { Text("OK") } }
            }
        }
    }
}

@Composable
private fun StreamTopBar(onBack: () -> Unit, onSwitchCamera: () -> Unit, modifier: Modifier) { Row(modifier = modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }; IconButton(onClick = onSwitchCamera) { Icon(Icons.Default.Cameraswitch, null, tint = Color.White) } } }

@Composable
private fun StreamControls(isStreaming: Boolean, isConnecting: Boolean, isTimerRunning: Boolean, onToggleStream: () -> Unit, onToggleTimer: () -> Unit, onResetTimer: () -> Unit, onHomeUp: () -> Unit, onHomeDown: () -> Unit, onAwayUp: () -> Unit, onAwayDown: () -> Unit, onExtraTimeUp: () -> Unit, onExtraTimeDown: () -> Unit, onCautionClick: (Boolean) -> Unit, onAdClick: () -> Unit, modifier: Modifier) {
    Column(modifier = modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            // Publicidad (Nuevo botón izquierdo)
            IconButton(onClick = onAdClick) { Icon(Icons.Default.Campaign, null, tint = Color.White, modifier = Modifier.size(32.dp)) }

            // Controles Local
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onCautionClick(true) }) { Icon(Icons.Default.Style, null, tint = Color.Yellow, modifier = Modifier.size(24.dp)) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Local", color = Color.White, fontSize = 10.sp); Row { IconButton(onClick = onHomeDown) { Icon(Icons.Default.Remove, null, tint = Color.White) }; IconButton(onClick = onHomeUp) { Icon(Icons.Default.Add, null, tint = Color.White) } } }
            }
            // Timer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallFloatingActionButton(onClick = onToggleTimer, containerColor = if (isTimerRunning) Color.Red else Color.Green) { Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White) }; SmallFloatingActionButton(onClick = onResetTimer, containerColor = Color.Gray) { Icon(Icons.Default.Replay, null, tint = Color.White) } }
                Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onExtraTimeDown, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Remove, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp)) }; Text("Extra", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp); IconButton(onClick = onExtraTimeUp, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Add, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp)) } }
            }
            // Controles Visitante
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Visitante", color = Color.White, fontSize = 10.sp); Row { IconButton(onClick = onAwayDown) { Icon(Icons.Default.Remove, null, tint = Color.White) }; IconButton(onClick = onAwayUp) { Icon(Icons.Default.Add, null, tint = Color.White) } } }
                IconButton(onClick = { onCautionClick(false) }) { Icon(Icons.Default.Style, null, tint = Color.Yellow, modifier = Modifier.size(24.dp)) }
            }
            
            // Espacio simétrico derecho
            IconButton(onClick = {}, enabled = false) { Icon(Icons.Default.Campaign, null, tint = Color.Transparent) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(onClick = onToggleStream, containerColor = when { isConnecting -> Color(0xFFFFA500); isStreaming -> Color.Red; else -> Color.Green }, modifier = Modifier.size(64.dp), shape = CircleShape) { Icon(if (isStreaming) Icons.Default.Stop else Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
        Text(if (isStreaming) "DETENER" else "INICIAR LIVE", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
