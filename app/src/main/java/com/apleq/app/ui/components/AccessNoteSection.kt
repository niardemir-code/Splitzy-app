package com.apleq.app.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apleq.app.ui.util.I18n
import kotlinx.coroutines.launch

/** Datos de acceso privados del gestor (usuario, contraseña…). Solo los ve su dueño. */
@Composable
fun AccessNoteSection(
    subscriptionId: Long,
    load: suspend (Long) -> String,
    save: suspend (Long, String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var note by remember(subscriptionId) { mutableStateOf<String?>(null) } // null = cargando
    var revealed by remember(subscriptionId) { mutableStateOf(false) }
    var editing by remember(subscriptionId) { mutableStateOf(false) }
    var draft by remember(subscriptionId) { mutableStateOf("") }
    var saving by remember(subscriptionId) { mutableStateOf(false) }
    var hasError by remember(subscriptionId) { mutableStateOf(false) }
    var confirmDelete by remember(subscriptionId) { mutableStateOf(false) }

    LaunchedEffect(subscriptionId) {
        note = load(subscriptionId)
    }

    val current = note ?: return

    fun persist(value: String) {
        saving = true
        hasError = false
        scope.launch {
            val ok = save(subscriptionId, value)
            saving = false
            if (ok) {
                note = value.trim().take(500)
                editing = false
                revealed = false
            } else {
                hasError = true
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = I18n.accessNoteTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = I18n.accessNoteHelp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            when {
                editing -> {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 500) draft = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(I18n.accessNotePlaceholder) },
                        minLines = 2,
                        maxLines = 6
                    )
                    if (hasError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            I18n.accessNoteError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = !saving, onClick = { persist(draft) }) { Text(I18n.accessNoteSave) }
                        OutlinedButton(enabled = !saving, onClick = { editing = false; hasError = false }) {
                            Text(I18n.accessNoteCancel)
                        }
                    }
                }
                current.isNotBlank() -> {
                    Text(
                        text = if (revealed) current else "••••••••",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { revealed = !revealed }) {
                            Text(if (revealed) I18n.accessNoteHide else I18n.accessNoteShow)
                        }
                        TextButton(onClick = { draft = current; editing = true }) { Text(I18n.accessNoteEdit) }
                        TextButton(enabled = !saving, onClick = { confirmDelete = true }) { Text(I18n.accessNoteDelete) }
                    }
                }
                else -> {
                    OutlinedButton(onClick = { draft = ""; editing = true }) { Text(I18n.accessNoteAdd) }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(I18n.accessNoteDeleteConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    persist("")
                }) { Text(I18n.accessNoteDelete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(I18n.accessNoteCancel) }
            }
        )
    }
}
