package com.apleq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(linkedMembers, key = { it.id }) { member ->
                        val clientUid = member.linkedUid!!
                        val chatId = "${currentUid}_${groupId}_${clientUid}"
                        val hasUnread = unreadChatIdsForOwner.contains(chatId)
                        val displayName = member.memberName.ifBlank { "Cliente" }
                        val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (hasUnread)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (hasUnread)
                                BorderStroke(1.3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            else
                                null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMemberClick(chatId, clientUid, displayName)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hasUnread)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
