package com.apleq.app.data.util

import android.content.Context

/** Recuerda si ya se mostró la pantalla de introducción en este dispositivo. */
object IntroPreferences {
    private const val PREFS = "app_intro_prefs"
    private const val KEY_SEEN = "key_intro_seen"

    fun isSeen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SEEN, true)
            .apply()
    }
}
