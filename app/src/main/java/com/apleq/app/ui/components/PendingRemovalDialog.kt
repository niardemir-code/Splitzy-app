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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.ui.util.I18n

/** Un miembro marcado «pendiente de eliminar» y el nombre de la suscripción a la que pertenece. */
data class PendingRemovalItem(
    val subscriptionName: String,
    val member: MemberEntity
)

@Composable
fun PendingRemovalDialog(
    items: List<PendingRemovalItem>,
    onDelete: (MemberEntity) -> Unit,
    onDeleteAll: (List<MemberEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    var itemToConfirm by remember { mutableStateOf<PendingRemovalItem?>(null) }
    var confirmAll by remember { mutableStateOf(false) }

    // Si ya no queda ninguno (se han eliminado todos), se cierra solo.
    if (items.isEmpty()) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = I18n.pendingRemovalDialogTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = I18n.pendingRemovalDialogDesc,
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
                            color = Color(0xFFFFE4E6)
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
                                        color = Color(0xFF991B1B),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.subscriptionName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF991B1B).copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                }
                                TextButton(onClick = { itemToConfirm = item }) {
                                    Text(I18n.delete, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
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
                TextButton(onClick = { confirmAll = true }) {
                    Text(I18n.pendingRemovalDeleteAll, color = Color(0xFFB91C1C))
                }
            }
        }
    )

    itemToConfirm?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToConfirm = null },
            text = { Text(I18n.pendingRemovalConfirm(item.member.memberName, item.subscriptionName)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(item.member)
                    itemToConfirm = null
                }) { Text(I18n.delete, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { itemToConfirm = null }) { Text(I18n.cancel) }
            }
        )
    }

    if (confirmAll) {
        AlertDialog(
            onDismissRequest = { confirmAll = false },
            text = { Text(I18n.pendingRemovalConfirmAll(items.size)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAll(items.map { it.member })
                    confirmAll = false
                }) { Text(I18n.delete, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmAll = false }) { Text(I18n.cancel) }
            }
        )
    }
}
