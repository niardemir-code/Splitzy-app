package com.apleq.app.data.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.apleq.app.data.local.SubscriptionWithMembers
import com.apleq.app.data.util.calculateNextCycleDate
import com.apleq.app.ui.util.I18n
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

/** Calcula qué avisos hay que programar a partir de los datos de la app (lógica pura). */
object ReminderPlanner {

    /** Momento exacto en que debe saltar el aviso: antelación + hora elegida en Ajustes. */
    fun triggerMillis(item: ReminderItem, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = item.paymentMillis }
        val v = item.leadValue.coerceAtLeast(0)
        when (item.leadUnit) {
            "days" -> cal.add(Calendar.DAY_OF_MONTH, -v)
            "weeks" -> cal.add(Calendar.DAY_OF_MONTH, -7 * v)
            "months" -> cal.add(Calendar.DAY_OF_MONTH, -30 * v)
            else -> Unit // "same_day" y "hours": el mismo día de cobro
        }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (item.leadUnit == "hours") cal.add(Calendar.HOUR_OF_DAY, -v)
        return cal.timeInMillis
    }

    private fun advanceOneCycle(item: ReminderItem): ReminderItem =
        item.copy(
            paymentMillis = calculateNextCycleDate(
                fromMillis = item.paymentMillis,
                freqValue = item.freqValue,
                freqUnit = item.freqUnit,
                referenceMillis = item.paymentMillis
            )
        )

    /**
     * Devuelve el aviso listo para programar: si el momento del aviso de este ciclo ya pasó
     * (p. ej. cobro mañana con aviso 3 días antes), salta al siguiente ciclo. Null si no se repite.
     */
    fun upcoming(item: ReminderItem, hour: Int, minute: Int, now: Long = System.currentTimeMillis()): ReminderItem? {
        var cur = item
        var guard = 0
        while (triggerMillis(cur, hour, minute) <= now) {
            if (cur.freqValue <= 0 || guard++ > 60) return null
            cur = advanceOneCycle(cur)
        }
        return cur
    }

    /** Aviso del ciclo siguiente, una vez que ha saltado el actual. Null si no se repite. */
    fun nextAfterFiring(item: ReminderItem, hour: Int, minute: Int): ReminderItem? {
        if (item.freqValue <= 0) return null
        return upcoming(advanceOneCycle(item), hour, minute)
    }

    private fun money(value: Double, symbol: String): String =
        String.format(Locale.getDefault(), "%.2f %s", value, symbol)

    fun build(
        subs: List<SubscriptionWithMembers>,
        groups: List<Map<String, Any?>>,
        clientPrefs: Map<String, Pair<Boolean, Int>>,
        uid: String,
        hour: Int,
        minute: Int
    ): List<ReminderItem> {
        val raw = mutableListOf<ReminderItem>()
        val zone = ZoneId.systemDefault()
        fun dayMillis(iso: String): Long? = runCatching {
            LocalDate.parse(iso.take(10)).atStartOfDay(zone).toInstant().toEpochMilli()
        }.getOrNull()

        // --- Gestor: tu propia suscripción y el cobro a cada miembro ---
        subs.forEach { item ->
            val sub = item.subscription
            val name = sub.platformName.ifBlank { I18n.subscriptionFallbackName }
            val symbol = sub.currency.ifBlank { "€" }

            if (sub.enableAlarm) {
                raw += ReminderItem(
                    id = "s_${sub.id}",
                    kind = "SUB",
                    subName = name,
                    who = "",
                    amount = if (sub.cost > 0.0) money(sub.cost, symbol) else "",
                    paymentMillis = item.nextRenewalTimestamp,
                    leadValue = sub.alarmValue,
                    leadUnit = sub.alarmUnit,
                    freqValue = item.billingPeriodObj.months,
                    freqUnit = "months"
                )
            }

            item.members.forEach { m ->
                if (!m.enableAlarm || m.isPendingRemoval || m.isPendingRegistration) return@forEach
                val pay = dayMillis(m.nextPaymentDate) ?: return@forEach
                val amount = if (m.contributionAmount > 0.0) m.contributionAmount else sub.defaultContributionPerUser
                raw += ReminderItem(
                    id = "m_${sub.id}_${m.id}",
                    kind = "MEMBER",
                    subName = name,
                    who = m.memberName.ifBlank { I18n.coSubscriberFallback },
                    amount = if (amount > 0.0) money(amount, symbol) else "",
                    paymentMillis = pay,
                    leadValue = m.alarmValue,
                    leadUnit = m.alarmUnit,
                    freqValue = if (m.autoRepeatPayment) m.paymentFrequencyValue.coerceAtLeast(1) else 0,
                    freqUnit = m.paymentFrequencyUnit.ifBlank { "months" }
                )
            }
        }

        // --- Cliente: tu cuota en los grupos en los que participas ---
        groups.forEach { group ->
            val groupId = group["_docId"]?.toString() ?: return@forEach
            val (enabled, leadDays) = clientPrefs[groupId] ?: (true to 3)
            if (!enabled) return@forEach
            val members = (group["members"] as? List<*>) ?: return@forEach
            val mine = members.filterIsInstance<Map<String, Any?>>().find {
                it["linkedUid"]?.toString() == uid || it["linked_uid"]?.toString() == uid
            } ?: return@forEach
            if ((mine["isPendingRemoval"] as? Boolean) == true) return@forEach

            val dateStr = (mine["nextPaymentDate"] ?: mine["next_payment_date"] ?: "").toString()
            val pay = dayMillis(dateStr) ?: return@forEach
            val name = (group["platformName"] ?: group["name"] ?: I18n.subscriptionFallbackName).toString()
            val amount = (mine["contributionAmount"] ?: mine["contribution_amount"] ?: mine["amount"] ?: 0)
                .toString().toDoubleOrNull() ?: 0.0
            val freqValue = (mine["paymentFrequencyValue"] as? Number)?.toInt()
                ?: (mine["payment_frequency_value"] as? Number)?.toInt() ?: 1
            val freqUnit = (mine["paymentFrequencyUnit"] ?: mine["payment_frequency_unit"] ?: "months").toString()
            val autoRepeat = (mine["autoRepeatPayment"] as? Boolean) ?: (mine["auto_repeat_payment"] as? Boolean) ?: true

            raw += ReminderItem(
                id = "c_$groupId",
                kind = "CLIENT",
                subName = name,
                who = "",
                amount = if (amount > 0.0) money(amount, "€") else "",
                paymentMillis = pay,
                leadValue = leadDays,
                leadUnit = if (leadDays <= 0) "same_day" else "days",
                freqValue = if (autoRepeat) freqValue.coerceAtLeast(1) else 0,
                freqUnit = freqUnit.ifBlank { "months" }
            )
        }

        return raw.mapNotNull { upcoming(it, hour, minute) }
    }
}

/** Programa y cancela las alarmas del sistema. */
object ReminderScheduler {
    const val ACTION_FIRE = "com.apleq.app.REMINDER_FIRE"
    const val EXTRA_ID = "reminder_id"

    private fun pendingIntent(context: Context, id: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_FIRE).putExtra(EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun cancel(context: Context, id: String) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context, id))
    }

    /** Programa una alarma. Alarma normal (puede retrasarse unos minutos): no necesita permisos especiales. */
    fun arm(context: Context, item: ReminderItem) {
        val at = ReminderPlanner.triggerMillis(item, ReminderPreferences.hour(context), ReminderPreferences.minute(context))
        if (at <= System.currentTimeMillis()) return
        context.getSystemService(AlarmManager::class.java)
            ?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent(context, item.id))
    }

    /** Sustituye todos los avisos programados por [items] (quita los antiguos). */
    fun replaceAll(context: Context, items: List<ReminderItem>) {
        ReminderStore.load(context).forEach { cancel(context, it.id) }
        if (!ReminderPreferences.enabled(context)) {
            ReminderStore.save(context, emptyList())
            return
        }
        ReminderStore.save(context, items)
        items.forEach { arm(context, it) }
    }

    /** Tras reiniciar el móvil o cambiar la hora: vuelve a programar lo guardado. */
    fun rearmAll(context: Context) {
        if (!ReminderPreferences.enabled(context)) return
        val hour = ReminderPreferences.hour(context)
        val minute = ReminderPreferences.minute(context)
        val items = ReminderStore.load(context).mapNotNull { ReminderPlanner.upcoming(it, hour, minute) }
        ReminderStore.save(context, items)
        items.forEach { arm(context, it) }
    }

    /** Sustituye un aviso concreto por el de su ciclo siguiente (o lo quita si no se repite). */
    fun replaceOne(context: Context, oldId: String, next: ReminderItem?) {
        val items = ReminderStore.load(context).filter { it.id != oldId }.toMutableList()
        if (next != null) items += next
        ReminderStore.save(context, items)
        if (next != null) arm(context, next)
    }
}
