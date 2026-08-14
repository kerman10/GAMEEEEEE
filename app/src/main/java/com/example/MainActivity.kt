package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CurrentScreen
import com.example.ui.GameViewModel
import com.example.ui.screens.CodexVaultScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.GloveCustomizerScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SectorSelectScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          val gameViewModel: GameViewModel = viewModel()
          val currentScreen by gameViewModel.currentScreen.collectAsStateWithLifecycle()

          when (currentScreen) {
            CurrentScreen.MAIN_MENU -> MainMenuScreen(viewModel = gameViewModel)
            CurrentScreen.IN_GAME -> GameScreen(viewModel = gameViewModel)
            CurrentScreen.SECTOR_SELECT -> SectorSelectScreen(viewModel = gameViewModel)
            CurrentScreen.CODEX_VAULT -> CodexVaultScreen(viewModel = gameViewModel)
            CurrentScreen.GLOVE_CUSTOMIZER -> GloveCustomizerScreen(viewModel = gameViewModel)
          }
        }
      }
    }
  }
}

