package com.apleq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apleq.app.data.remote.AuthState
import com.apleq.app.data.util.AppThemeMode
import com.apleq.app.data.util.GuidePreferences
import com.apleq.app.data.util.LanguagePreferences
import com.apleq.app.ui.screens.AuthGateScreen
import com.apleq.app.ui.screens.GuideScreen
import com.apleq.app.ui.screens.HomeScreen
import com.apleq.app.ui.screens.SettingsScreen
import com.apleq.app.ui.theme.MyApplicationTheme
import com.apleq.app.ui.viewmodel.SubscriptionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguagePreferences.apply(this)
        installSplashScreen()
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
                    val guideContext = LocalContext.current
                    var guideSeen by rememberSaveable { mutableStateOf(GuidePreferences.isSeen(guideContext)) }
                    var showGuide by rememberSaveable { mutableStateOf(false) }

                    Crossfade(
                        targetState = authState is AuthState.Authenticated,
                        label = "auth_crossfade"
                    ) { isAuthenticated ->
                        if (isAuthenticated) {
                            if (!guideSeen || showGuide) {
                                GuideScreen(
                                    onFinish = {
                                        GuidePreferences.markSeen(guideContext)
                                        guideSeen = true
                                        showGuide = false
                                    }
                                )
                            } else if (showSettings) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeSettingsScreen() },
                                    onOpenGuide = { showGuide = true }
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


