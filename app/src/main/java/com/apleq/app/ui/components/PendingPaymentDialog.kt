package com.apleq.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.ui.util.I18n

/** Un miembro pendiente de pago y el nombre de la suscripción a la que pertenece. */
data class PendingPaymentItem(
    val subscriptionName: String,
    val member: MemberEntity
)

@Composable
fun PendingPaymentDialog(
    items: List<PendingPaymentItem>,
    onSettle: (MemberEntity) -> Unit,
    onSettleAll: (List<MemberEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    // Si ya no queda ninguno, se cierra solo.
    if (items.isEmpty()) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = I18n.pendingPaymentDialogTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = I18n.pendingPaymentDialogDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.member.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.member.memberName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF92400E),
                                        maxLines = 1
                                    )
                                    val cycles = item.member.unpaidCycles
                                    Text(
                                        text = if (cycles > 1) "${item.subscriptionName} · ${I18n.pendingPaymentCycles(cycles)}"
                                        else item.subscriptionName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF92400E).copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                }
                                TextButton(onClick = { onSettle(item.member) }) {
                                    Text(I18n.pendingPaymentMarkPaid, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(I18n.close) }
        },
        dismissButton = {
            if (items.size > 1) {
                TextButton(onClick = { onSettleAll(items.map { it.member }) }) {
                    Text(I18n.pendingPaymentMarkAllPaid, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
