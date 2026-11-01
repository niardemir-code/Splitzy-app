package com.apleq.app.data.util

import android.content.Context

/** Recuerda en el dispositivo si ya se mostró la guía de inicio. */
object GuidePreferences {
    private const val PREFS = "app_guide_prefs"
    private const val KEY_SEEN = "key_guide_seen"

    fun isSeen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_SEEN, true).apply()
    }
}
