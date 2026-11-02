package com.apleq.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apleq.app.data.reminders.ReminderTexts
import com.apleq.app.ui.viewmodel.SubscriptionViewModel

/** Ayuda para que los avisos lleguen con la app cerrada (batería). */
object BatteryHelper {
    fun isExempt(context: Context): Boolean =
        context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) ?: true

    /** Abre la ficha de la app en los ajustes del sistema, donde se entra en «Batería». */
    fun openAppDetails(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Exception) {
            }
        }
    }

    fun openNotificationSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (_: Exception) {
            openAppDetails(context)
        }
    }
}

/** Aviso de primera vez para quitar las restricciones de batería. */
@Composable
fun BatteryPromptDialog(onRemove: () -> Unit, onLater: () -> Unit, onInfo: () -> Unit) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(ReminderTexts.batteryPromptTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(ReminderTexts.batteryPromptText, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onInfo) { Text(ReminderTexts.moreInfo) }
            }
        },
        confirmButton = { TextButton(onClick = onRemove) { Text(ReminderTexts.batteryRemove, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onLater) { Text(ReminderTexts.later) } }
    )
}

/** Pasos por marca de móvil. */
@Composable
fun BatteryInfoDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(ReminderTexts.batteryInfoTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(ReminderTexts.batteryInfoIntro, style = MaterialTheme.typography.bodyMedium)
                BrandSteps("Xiaomi, Redmi, POCO", ReminderTexts.batteryXiaomi)
                BrandSteps("Oppo, Realme, OnePlus", ReminderTexts.batteryOppo)
                BrandSteps("Samsung", ReminderTexts.batterySamsung)
                BrandSteps("Huawei, Honor", ReminderTexts.batteryHuawei)
                BrandSteps("Vivo", ReminderTexts.batteryVivo)
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com")))
                    } catch (_: Exception) {
                    }
                }) { Text(ReminderTexts.batteryGuide) }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(ReminderTexts.close) } }
    )
}

@Composable
private fun BrandSteps(brand: String, steps: String) {
    Column {
        Text(brand, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(steps, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Sección de Ajustes: avisos en el móvil (activar, hora, permiso y batería). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsCard(viewModel: SubscriptionViewModel) {
    val context = LocalContext.current
    val settings by viewModel.reminderSettings.collectAsStateWithLifecycle()

    var notificationsOn by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    var batteryExempt by remember { mutableStateOf(BatteryHelper.isExempt(context)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showBatteryInfo by remember { mutableStateOf(false) }

    // Al volver de los ajustes del sistema, se vuelve a comprobar.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
                batteryExempt = BatteryHelper.isExempt(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        ReminderTexts.sectionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        ReminderTexts.sectionSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Activar / desactivar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ReminderTexts.enabledLabel, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = settings.enabled, onCheckedChange = { viewModel.setRemindersEnabled(it) })
            }

            if (settings.enabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Hora
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ReminderTexts.timeLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            ReminderTexts.timeHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text("%02d:%02d".format(settings.hour, settings.minute), fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Permiso de notificaciones
                if (!notificationsOn) {
                    Text(
                        ReminderTexts.notificationsOff,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(onClick = { BatteryHelper.openNotificationSettings(context) }) {
                        Text(ReminderTexts.notificationsActivate)
                    }
                } else {
                    Text(
                        ReminderTexts.notificationsOn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Batería
                Text(
                    if (batteryExempt) ReminderTexts.batteryOk else ReminderTexts.batteryBad,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (batteryExempt) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!batteryExempt) {
                        OutlinedButton(onClick = { BatteryHelper.openAppDetails(context) }) {
                            Text(ReminderTexts.batteryRemove)
                        }
                    }
                    TextButton(onClick = { showBatteryInfo = true }) { Text(ReminderTexts.moreInfo) }
                }
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = settings.hour,
            initialMinute = settings.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(ReminderTexts.timeLabel) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderTime(state.hour, state.minute)
                    showTimePicker = false
                }) { Text(ReminderTexts.accept) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(ReminderTexts.cancel) } }
        )
    }

    if (showBatteryInfo) BatteryInfoDialog(onClose = { showBatteryInfo = false })
}
