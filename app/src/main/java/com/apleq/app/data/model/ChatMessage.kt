package com.apleq.app.data.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val createdAtMs: Long
)

data class UnreadChatInfo(
    val chatId: String,
    val groupId: String,
    val ownerUid: String,
    val clientUid: String,
    val subscriptionName: String,
    val otherPersonName: String,
    val lastMessageText: String,
    val isOwnerSide: Boolean // true si el "sin leer" es para el gestor; false si es para el cliente
)
