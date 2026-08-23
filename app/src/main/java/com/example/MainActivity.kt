package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.AuthState
import com.example.data.util.AppThemeMode
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SubscriptionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val authState by viewModel.authState.collectAsStateWithLifecycle()
                    val showSettings by viewModel.showSettingsScreen.collectAsStateWithLifecycle()

                    Crossfade(
                        targetState = authState is AuthState.Authenticated,
                        label = "auth_crossfade"
                    ) { isAuthenticated ->
                        if (isAuthenticated) {
                            if (showSettings) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeSettingsScreen() }
                                )
                            } else {
                                HomeScreen(viewModel = viewModel)
                            }
                        } else {
                            AuthGateScreen(
                                viewModel = viewModel,
                                authState = authState
                            )
                        }
                    }
                }
            }
        }
    }
}


