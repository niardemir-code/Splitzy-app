package com.apleq.app.data.reminders

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Un aviso de cobro programado en el móvil. */
data class ReminderItem(
    val id: String,
    /** "MEMBER" (cobras a alguien), "SUB" (se cobra tu suscripción), "CLIENT" (te toca pagar). */
    val kind: String,
    val subName: String,
    val who: String,
    val amount: String,
    /** Día de cobro, a las 00:00 locales, en milisegundos. */
    val paymentMillis: Long,
    val leadValue: Int,
    /** "same_day", "hours", "days", "weeks" o "months". */
    val leadUnit: String,
    /** Cada cuánto se repite el cobro (0 = no se repite). */
    val freqValue: Int,
    val freqUnit: String
)

/** Guarda en el dispositivo los avisos programados, para poder reprogramarlos tras un reinicio. */
object ReminderStore {
    private const val PREFS = "reminder_store"
    private const val KEY_ITEMS = "items"

    fun load(context: Context): List<ReminderItem> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, null)
            ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ReminderItem(
                    id = o.getString("id"),
                    kind = o.getString("kind"),
                    subName = o.optString("subName"),
                    who = o.optString("who"),
                    amount = o.optString("amount"),
                    paymentMillis = o.getLong("paymentMillis"),
                    leadValue = o.optInt("leadValue"),
                    leadUnit = o.optString("leadUnit", "days"),
                    freqValue = o.optInt("freqValue"),
                    freqUnit = o.optString("freqUnit", "months")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, items: List<ReminderItem>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("kind", it.kind)
                    .put("subName", it.subName)
                    .put("who", it.who)
                    .put("amount", it.amount)
                    .put("paymentMillis", it.paymentMillis)
                    .put("leadValue", it.leadValue)
                    .put("leadUnit", it.leadUnit)
                    .put("freqValue", it.freqValue)
                    .put("freqUnit", it.freqUnit)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ITEMS, arr.toString()).apply()
    }
}

/** Ajustes de los avisos (en el dispositivo). */
object ReminderPreferences {
    private const val PREFS = "reminder_prefs"

    fun enabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", true)

    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", value).apply()
    }

    fun hour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("hour", 9)

    fun minute(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("minute", 0)

    fun setTime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("hour", hour).putInt("minute", minute).apply()
    }

    fun permissionAsked(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("permission_asked", false)

    fun markPermissionAsked(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("permission_asked", true).apply()
    }

    fun batteryAsked(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("battery_asked", false)

    fun markBatteryAsked(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("battery_asked", true).apply()
    }
}

/** Avisos ya mostrados (uno por aviso y ciclo de cobro), para no repetirlos al recuperar los perdidos. */
object ReminderFired {
    private const val PREFS = "reminder_fired"
    private const val INIT = "__init"
    private const val KEEP_MILLIS = 60L * 24 * 60 * 60 * 1000

    private fun key(item: ReminderItem) = "${item.id}|${item.paymentMillis}"

    fun wasFired(context: Context, item: ReminderItem): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(key(item))

    fun mark(context: Context, item: ReminderItem) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val edit = prefs.edit()
        prefs.all.forEach { (k, v) ->
            if (k != INIT && v is Long && now - v > KEEP_MILLIS) edit.remove(k)
        }
        edit.putLong(key(item), now).apply()
    }

    /** True solo la primera vez: sirve para no avisar de golpe de lo que ya se mostró antes de esta versión. */
    fun firstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.contains(INIT)) return false
        prefs.edit().putBoolean(INIT, true).apply()
        return true
    }
}
