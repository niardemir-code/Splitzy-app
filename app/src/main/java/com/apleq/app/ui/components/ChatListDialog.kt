package com.apleq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apleq.app.data.local.MemberEntity

@Composable
fun ChatListDialog(
    subscriptionName: String,
    members: List<MemberEntity>,
    currentUid: String,
    groupId: String,
    unreadChatIdsForOwner: Set<String>,
    onMemberClick: (chatId: String, clientUid: String, clientName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val linkedMembers = members.filter { !it.linkedUid.isNullOrBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chats · $subscriptionName") },
        text = {
            if (linkedMembers.isEmpty()) {
                Text(
                    "Ningún miembro tiene cuenta enlazada todavía.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(linkedMembers, key = { it.id }) { member ->
                        val clientUid = member.linkedUid!!
                        val chatId = "${currentUid}_${groupId}_${clientUid}"
                        val hasUnread = unreadChatIdsForOwner.contains(chatId)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMemberClick(chatId, clientUid, member.memberName.ifBlank { "Cliente" })
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = member.memberName.ifBlank { "Cliente" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal
                            )
                            if (hasUnread) {
                                Box(
                                    modifier = Modifier.size(22.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Mensaje sin leer",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE11D48))
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
