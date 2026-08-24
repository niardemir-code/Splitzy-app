package com.apleq.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.data.model.CurrencyManager
import java.util.Locale

@Composable
fun ReminderMessageDialog(
    member: MemberEntity,
    subscription: SubscriptionEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val defaultMessage = remember {
        val parsedPricing = com.apleq.app.data.model.PlatformPricingHelper.parse(subscription.platformPricing)
        val memberPlatformPricing = parsedPricing.find { it.platformName.equals(member.sharingPlatform, ignoreCase = true) }
        val memberAmount = if (member.contributionAmount > 0.0) {
            member.contributionAmount
        } else if (memberPlatformPricing != null && memberPlatformPricing.pricePerUser > 0.0) {
            memberPlatformPricing.pricePerUser
        } else {
            0.0
        }
        val memberCurrencySymbol = if (member.currency.isNotBlank()) {
            CurrencyManager.findCurrency(member.currency).symbol
        } else {
            memberPlatformPricing?.currencyItem?.symbol ?: "€"
        }
        val memberPeriodLabel = if (member.paymentFrequencyUnit.isNotBlank()) {
            val freqVal = if (member.paymentFrequencyValue > 0) member.paymentFrequencyValue else 1
            when (member.paymentFrequencyUnit) {
                "months" -> if (freqVal == 1) "mensual" else "cada $freqVal meses"
                "years" -> if (freqVal == 1) "anual" else "cada $freqVal años"
                "weeks" -> if (freqVal == 1) "semanal" else "cada $freqVal semanas"
                "days" -> if (freqVal == 1) "diaria" else "cada $freqVal días"
                else -> memberPlatformPricing?.billingPeriodObj?.label?.lowercase() ?: "mensual"
            }
        } else {
            memberPlatformPricing?.billingPeriodObj?.label?.lowercase() ?: "mensual"
        }
        "¡Hola ${member.memberName}! Te recuerdo la cuota $memberPeriodLabel de ${String.format(Locale.getDefault(), "%.2f", memberAmount)} $memberCurrencySymbol para la suscripción compartida de ${subscription.platformName} (${subscription.customPlanName.ifBlank { "Cuenta Compartida" }}). ¡Muchas gracias!"
    }

    var messageText by remember { mutableStateOf(defaultMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recordatorio de Pago",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mensaje listo para enviar a ${member.memberName}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_message_input"),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Recordatorio", messageText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Mensaje copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar")
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, messageText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Enviar recordatorio por...")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        modifier = modifier
    )
}
