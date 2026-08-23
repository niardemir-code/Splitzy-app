package com.example.data.util

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode(val title: String, val description: String) {
    SYSTEM("Usar configuración del sistema", "Sigue automáticamente el tema claro u oscuro de Android"),
    LIGHT("Claro", "Tema luminoso de alto contraste"),
    DARK("Oscuro", "Tema oscuro que reduce el brillo y ahorra batería")
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "key_selected_theme_mode"
    }

    fun getThemeMode(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
