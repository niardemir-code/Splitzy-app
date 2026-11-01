package com.apleq.app.data.util

import android.content.Context
import android.content.res.Resources
import java.util.Locale

/**
 * Idioma elegido por el usuario: "system" (por defecto), "es", "ca" o "en".
 * [apply] fija el Locale por defecto del proceso, que es lo que lee I18n.
 */
object LanguagePreferences {
    const val SYSTEM = "system"
    private const val PREFS = "app_language_prefs"
    private const val KEY = "key_language_choice"

    fun getChoice(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, SYSTEM) ?: SYSTEM

    fun setChoice(context: Context, choice: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, choice).apply()
        apply(context)
    }

    fun apply(context: Context) {
        val locale = when (getChoice(context)) {
            "es" -> Locale.forLanguageTag("es")
            "ca" -> Locale.forLanguageTag("ca")
            "en" -> Locale.forLanguageTag("en")
            else -> Resources.getSystem().configuration.locales[0]
        }
        Locale.setDefault(locale)
    }
}
