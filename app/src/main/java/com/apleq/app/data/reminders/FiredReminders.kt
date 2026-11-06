package com.apleq.app.data.reminders

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Avisos que el móvil ha notificado de verdad. La campanita los muestra para que nunca quede vacía. */
object FiredReminders {
    private const val PREFS = "fired_reminders"
    private const val KEY = "items"
    private const val KEEP_MILLIS = 7L * 24 * 60 * 60 * 1000

    data class Fired(val item: ReminderItem, val firedAt: Long)

    @Synchronized
    fun add(context: Context, item: ReminderItem) {
        clearDismissedFor(context, item)
        val now = System.currentTimeMillis()
        val list = load(context)
            .filterNot { it.item.id == item.id && it.item.paymentMillis == item.paymentMillis }
            .toMutableList()
        list += Fired(item, now)
        save(context, list.filter { now - it.firedAt < KEEP_MILLIS })
    }

    @Synchronized
    fun load(context: Context): List<Fired> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Fired(
                    ReminderItem(
                        id = o.getString("id"),
                        kind = o.getString("kind"),
                        subName = o.getString("subName"),
                        who = o.getString("who"),
                        amount = o.getString("amount"),
                        paymentMillis = o.getLong("paymentMillis"),
                        leadValue = o.getInt("leadValue"),
                        leadUnit = o.getString("leadUnit"),
                        freqValue = o.getInt("freqValue"),
                        freqUnit = o.getString("freqUnit")
                    ),
                    o.getLong("firedAt")
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun save(context: Context, list: List<Fired>) {
        val arr = JSONArray()
        list.forEach { f ->
            arr.put(JSONObject().apply {
                put("id", f.item.id); put("kind", f.item.kind); put("subName", f.item.subName)
                put("who", f.item.who); put("amount", f.item.amount)
                put("paymentMillis", f.item.paymentMillis)
                put("leadValue", f.item.leadValue); put("leadUnit", f.item.leadUnit)
                put("freqValue", f.item.freqValue); put("freqUnit", f.item.freqUnit)
                put("firedAt", f.firedAt)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    /** Un aviso nuevo del móvil vuelve a mostrar los avisos de ese miembro que se hubieran descartado antes. */
    private fun clearDismissedFor(context: Context, item: ReminderItem) {
        if (item.kind != "MEMBER") return
        // item.id = "m_<subId>_<memberId>"  ->  los ids de la campanita son "notif_<subId>_<memberId>_..."
        val prefix = "notif_" + item.id.removePrefix("m_") + "_"
        val prefs = context.getSharedPreferences("apleq_notifications_prefs", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("dismissed_notification_ids", emptySet()) ?: emptySet()
        val cleaned = current.filterNot { it.startsWith(prefix) }.toSet()
        if (cleaned.size != current.size) {
            prefs.edit().putStringSet("dismissed_notification_ids", cleaned).apply()
        }
    }
}
