package com.apleq.app.data.util

import android.content.Context

enum class GuidePath { MANAGER, CLIENT }

/** Acciones del usuario que no se pueden deducir de los datos y se recuerdan en el dispositivo. */
enum class GuideEvent { SHARED, MEMBER_OPENED, BELL_OPENED, CHAT_OPENED, GROUP_OPENED }

data class GuideState(
    val path: GuidePath? = null,
    val dismissed: Boolean = false,
    val collapsed: Boolean = false,
    /** true cuando ya se mostró (o se cerró) el diálogo «¿Qué vienes a hacer?». */
    val choiceShown: Boolean = false,
    val events: Set<GuideEvent> = emptySet()
)

object GuidePreferences {
    private const val PREFS = "app_first_steps_prefs"
    private const val KEY_PATH = "key_path"
    private const val KEY_DISMISSED = "key_dismissed"
    private const val KEY_COLLAPSED = "key_collapsed"
    private const val KEY_CHOICE_SHOWN = "key_choice_shown"
    private const val KEY_EVENTS = "key_events"

    fun load(context: Context): GuideState {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val path = runCatching { p.getString(KEY_PATH, null)?.let { GuidePath.valueOf(it) } }.getOrNull()
        val events = (p.getStringSet(KEY_EVENTS, emptySet()) ?: emptySet())
            .mapNotNull { name -> runCatching { GuideEvent.valueOf(name) }.getOrNull() }
            .toSet()
        return GuideState(
            path = path,
            dismissed = p.getBoolean(KEY_DISMISSED, false),
            collapsed = p.getBoolean(KEY_COLLAPSED, false),
            choiceShown = p.getBoolean(KEY_CHOICE_SHOWN, false),
            events = events
        )
    }

    fun save(context: Context, state: GuideState) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_PATH, state.path?.name)
            .putBoolean(KEY_DISMISSED, state.dismissed)
            .putBoolean(KEY_COLLAPSED, state.collapsed)
            .putBoolean(KEY_CHOICE_SHOWN, state.choiceShown)
            .putStringSet(KEY_EVENTS, state.events.map { it.name }.toSet())
            .apply()
    }
}
