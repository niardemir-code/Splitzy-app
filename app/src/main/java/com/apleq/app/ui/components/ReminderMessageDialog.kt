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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ContentCopy
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
import com.apleq.app.ui.util.I18n
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
            I18n.reminderPeriodLabel(
                unit = member.paymentFrequencyUnit,
                value = member.paymentFrequencyValue,
                fallback = memberPlatformPricing?.billingPeriodObj?.label?.lowercase() ?: I18n.monthlySuffix
            )
        } else {
            memberPlatformPricing?.billingPeriodObj?.label?.lowercase() ?: I18n.monthlySuffix
        }
        val planName = subscription.customPlanName.ifBlank { I18n.reminderDefaultPlanName }
        val formattedAmount = String.format(Locale.getDefault(), "%.2f", memberAmount)
        I18n.buildReminderMessage(
            memberName = member.memberName,
            periodLabel = memberPeriodLabel,
            amount = formattedAmount,
            currencySymbol = memberCurrencySymbol,
            platformName = subscription.platformName,
            planName = planName
        )
    }

    var messageText by remember { mutableStateOf(defaultMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Message,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = I18n.paymentReminderTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = I18n.reminderMessageReadyFor(member.memberName),
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
                            val clip = ClipData.newPlainText(I18n.reminderClipLabel, messageText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, I18n.messageCopied, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(I18n.copy)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, messageText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, I18n.reminderShareChooser)
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(I18n.share)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(I18n.close)
            }
        },
        modifier = modifier
    )
}
