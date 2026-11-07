package com.apleq.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apleq.app.data.model.ChatMessage
import com.apleq.app.ui.util.I18n

@Composable
fun ChatDialog(
    subscriptionName: String,
    otherPersonName: String,
    currentUid: String,
    messages: List<ChatMessage>,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    isBlocked: Boolean = false,
    onBlock: () -> Unit = {},
    onUnblock: () -> Unit = {},
    onReport: (reason: String, comment: String, onResult: (Boolean) -> Unit) -> Unit = { _, _, onResult -> onResult(false) }
) {
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var menuExpanded by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("harassment") }
    var reportComment by remember { mutableStateOf("") }
    // 0 = sin enviar, 1 = enviando, 2 = enviada, 3 = error
    var reportStatus by remember { mutableIntStateOf(0) }

    // Si la persona está bloqueada, sus mensajes no se muestran.
    val visibleMessages = if (isBlocked) messages.filter { it.senderId == currentUid } else messages

    val imeBottom = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current)
    LaunchedEffect(visibleMessages.size, imeBottom) {
        if (visibleMessages.isNotEmpty()) listState.scrollToItem(visibleMessages.size - 1)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .imePadding()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        if (onBack != null) {
                            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = I18n.chatBackToList)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Column {
                            Text(otherPersonName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(subscriptionName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = I18n.chatMenu)
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(I18n.chatReport) },
                                    onClick = {
                                        menuExpanded = false
                                        reportReason = "harassment"
                                        reportComment = ""
                                        reportStatus = 0
                                        showReport = true
                                    }
                                )
                                if (isBlocked) {
                                    DropdownMenuItem(
                                        text = { Text(I18n.chatUnblock) },
                                        onClick = {
                                            menuExpanded = false
                                            onUnblock()
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text(I18n.chatBlock, color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            menuExpanded = false
                                            showBlockConfirm = true
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = I18n.close)
                        }
                    }
                }
                HorizontalDivider()

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (visibleMessages.isEmpty()) {
                        item {
                            Text(
                                if (isBlocked) I18n.chatBlockedNotice else I18n.chatNoMessagesYet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    items(visibleMessages, key = { it.id }) { msg ->
                        val isMine = msg.senderId == currentUid
                        val senderLabel = if (isMine) I18n.chatYouLabel else otherPersonName
                        val timeLabel = java.text.DateFormat
                            .getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
                            .format(java.util.Date(msg.createdAtMs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
                                Text(
                                    "$senderLabel · $timeLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.widthIn(max = 260.dp)
                                ) {
                                    Text(
                                        msg.text,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                if (isBlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            I18n.chatBlockedNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = onUnblock) { Text(I18n.chatUnblock) }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(I18n.chatMessagePlaceholder) },
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val trimmed = text.trim()
                                if (trimmed.isNotEmpty()) {
                                    onSend(trimmed)
                                    text = ""
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = I18n.chatSend)
                        }
                    }
                }
            }
        }
    }

    if (showBlockConfirm) {
        AlertDialog(
            onDismissRequest = { showBlockConfirm = false },
            text = { Text(I18n.chatBlockConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    showBlockConfirm = false
                    onBlock()
                }) { Text(I18n.chatBlock, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirm = false }) { Text(I18n.chatReportCancel) }
            }
        )
    }

    if (showReport) {
        val reasons = listOf(
            "spam" to I18n.reportReasonSpam,
            "harassment" to I18n.reportReasonHarassment,
            "illegal" to I18n.reportReasonIllegal,
            "impersonation" to I18n.reportReasonImpersonation,
            "other" to I18n.reportReasonOther
        )
        AlertDialog(
            onDismissRequest = { if (reportStatus != 1) showReport = false },
            title = { Text(I18n.chatReportTitle) },
            text = {
                if (reportStatus == 2) {
                    Text(I18n.chatReportThanks)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            I18n.chatReportIntro,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        reasons.forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reportReason = key },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = reportReason == key, onClick = { reportReason = key })
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        OutlinedTextField(
                            value = reportComment,
                            onValueChange = { if (it.length <= 500) reportComment = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(I18n.chatReportCommentHint) },
                            maxLines = 4
                        )
                        if (reportStatus == 3) {
                            Text(
                                I18n.chatReportError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (reportStatus == 2) {
                    TextButton(onClick = { showReport = false }) { Text(I18n.chatReportClose) }
                } else {
                    TextButton(
                        enabled = reportStatus != 1,
                        onClick = {
                            reportStatus = 1
                            onReport(reportReason, reportComment.trim()) { ok ->
                                reportStatus = if (ok) 2 else 3
                            }
                        }
                    ) { Text(I18n.chatReportSend, color = MaterialTheme.colorScheme.error) }
                }
            },
            dismissButton = {
                if (reportStatus != 2) {
                    TextButton(
                        enabled = reportStatus != 1,
                        onClick = { showReport = false }
                    ) { Text(I18n.chatReportCancel) }
                }
            }
        )
    }
}
