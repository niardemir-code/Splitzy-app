package com.apleq.app

import android.content.Intent
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.apleq.app.data.remote.AuthState
import com.apleq.app.data.reminders.ReminderPreferences
import com.apleq.app.data.util.AppThemeMode
import com.apleq.app.data.util.LanguagePreferences
import com.apleq.app.ui.screens.AuthGateScreen
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
        handleBellIntent(intent)
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
                    val permissionContext = LocalContext.current
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }
                    val isAuthenticatedNow = authState is AuthState.Authenticated
                    LaunchedEffect(isAuthenticatedNow) {
                        if (isAuthenticatedNow &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                permissionContext, Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED &&
                            !ReminderPreferences.permissionAsked(permissionContext)
                        ) {
                            ReminderPreferences.markPermissionAsked(permissionContext)
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    Crossfade(
                        targetState = authState is AuthState.Authenticated,
                        label = "auth_crossfade"
                    ) { isAuthenticated ->
                        if (isAuthenticated) {
                            if (showSettings) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeSettingsScreen() },
                                    onOpenGuide = {
                                        viewModel.guideReopen()
                                        viewModel.closeSettingsScreen()
                                    }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleBellIntent(intent)
    }

    private fun handleBellIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(com.apleq.app.ui.util.BellRequest.EXTRA, false) == true) {
            com.apleq.app.ui.util.BellRequest.pending.value = true
            intent.removeExtra(com.apleq.app.ui.util.BellRequest.EXTRA)
        }
    }
}


