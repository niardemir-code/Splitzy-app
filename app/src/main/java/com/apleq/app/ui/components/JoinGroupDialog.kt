package com.apleq.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.apleq.app.ui.util.I18n

@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onJoin: (String, (Boolean, String) -> Unit) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n.joinGroupDialogTitle) },
        text = {
            Column {
                Text(
                    I18n.joinGroupDialogDescription,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    placeholder = { Text(I18n.joinGroupCodePlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                message?.let { (ok, msg) ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ok) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading && code.isNotBlank(),
                onClick = {
                    loading = true
                    message = null
                    onJoin(code) { ok, msg ->
                        loading = false
                        message = ok to msg
                        if (ok) onDismiss()
                    }
                }
            ) { Text(if (loading) I18n.joinGroupJoiningInProgress else I18n.joinGroupAction) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.close) }
        }
    )
}
