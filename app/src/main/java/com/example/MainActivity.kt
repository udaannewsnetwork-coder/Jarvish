package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.HudTab
import com.example.ui.hud.HudCodebaseTab
import com.example.ui.hud.HudFilesTab
import com.example.ui.hud.HudHeader
import com.example.ui.hud.HudSettingsTab
import com.example.ui.hud.HudTerminalTab
import com.example.ui.hud.HudVoiceTab
import com.example.ui.theme.JarvisHudBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisApp()
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            viewModel.getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission = granted
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // BackHandler: return to VOICE_CORE if user presses Back while on another tab
    BackHandler(enabled = currentTab != HudTab.VOICE_CORE) {
        viewModel.setTab(HudTab.VOICE_CORE)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            HudHeader(
                telemetry = telemetry,
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JarvisHudBackground)
        ) {
            when (currentTab) {
                HudTab.VOICE_CORE -> HudVoiceTab(viewModel = viewModel)
                HudTab.TACTICAL_TERMINAL -> HudTerminalTab(viewModel = viewModel)
                HudTab.FILE_SYSTEM -> HudFilesTab(viewModel = viewModel)
                HudTab.MODULAR_CODEBASE -> HudCodebaseTab()
                HudTab.SETTINGS -> HudSettingsTab(viewModel = viewModel)
            }
        }
    }
}
