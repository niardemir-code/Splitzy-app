package com.apleq.app.data.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apleq.app.MainActivity
import com.apleq.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Salta la alarma de un aviso: muestra la notificación y programa el del ciclo siguiente. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_FIRE) return
        val id = intent.getStringExtra(ReminderScheduler.EXTRA_ID) ?: return
        val item = ReminderStore.load(context).find { it.id == id } ?: return

        if (ReminderPreferences.enabled(context)) show(context, item)

        val next = ReminderPlanner.nextAfterFiring(
            item,
            ReminderPreferences.hour(context),
            ReminderPreferences.minute(context)
        )
        ReminderScheduler.replaceOne(context, id, next)
    }

    private fun show(context: Context, item: ReminderItem) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        createChannel(context)

        val payDate = Instant.ofEpochMilli(item.paymentMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val days = ChronoUnit.DAYS.between(LocalDate.now(), payDate).toInt()
        val text = ReminderTexts.body(item, days)

        val open = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_bell)
            .setContentTitle(item.subName)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        try {
            manager.notify(item.id.hashCode(), notification)
        } catch (_: SecurityException) {
            // Sin permiso de notificaciones (Android 13+): no se puede mostrar.
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            ReminderTexts.channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = ReminderTexts.channelDescription }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "payment_reminders"
    }
}

/** Reprograma los avisos al reiniciar el móvil, actualizar la app o cambiar la hora o la zona horaria. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rearmAll(context)
    }
}
