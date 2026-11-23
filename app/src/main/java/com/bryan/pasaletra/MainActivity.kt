package com.bryan.pasaletra

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val fonts = loadFonts(this)
        setContent {
            CompositionLocalProvider(LocalFonts provides fonts) {
                MaterialTheme {
                    val vm: GameViewModel = viewModel()
                    App(vm)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Si sales de la app en mitad de un turno, la partida queda en pausa.
        try {
            val vm = androidx.lifecycle.ViewModelProvider(this)[GameViewModel::class.java]
            vm.pause()
        } catch (e: Exception) {
        }
    }
}

@Composable
fun App(vm: GameViewModel) {
    val screen = vm.screen
    val dark = screen == Screen.SORTEO || screen == Screen.PAUSA || screen == Screen.FALLO
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val ctrl = WindowCompat.getInsetsController(window, view)
            ctrl.isAppearanceLightStatusBars = !dark
            ctrl.isAppearanceLightNavigationBars = !dark
        }
    }

    BackHandler(enabled = screen != Screen.INICIO) { vm.back() }

    Crossfade(targetState = screen, animationSpec = tween(180), label = "pantallas") { s ->
        val bg = when (s) {
            Screen.SORTEO, Screen.PAUSA -> C.Ink
            Screen.FALLO -> C.Red
            else -> C.Cream
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(bg)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            when (s) {
                Screen.INICIO -> InicioScreen(vm)
                Screen.COMO -> ComoScreen(vm)
                Screen.JUGADORES -> JugadoresScreen(vm)
                Screen.MODO -> ModoScreen(vm)
                Screen.SORTEO -> SorteoScreen(vm)
                Screen.TURNO -> TurnoScreen(vm)
                Screen.VALIDAR -> ValidarScreen(vm)
                Screen.FALLO -> FalloScreen(vm)
                Screen.PAUSA -> PausaScreen(vm)
                Screen.RESULTADOS -> ResultadosScreen(vm)
            }
        }
    }
}
