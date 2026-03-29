package mk.ukim.finki.iskacamebackend.dto.response.chat

/**
 * WebSocket notification sent when a message receipt status is updated.
 *
 * Notifications are broadcast to clients subscribed to `/topic/chat.{chatRoomId}.receipts`.
 *
 * @property chatRoomId the ID of the chat room
 * @property messageId the ID of the affected message, or -1 for bulk updates
 * @property recipientId the ID of the user whose receipt status was updated
 * @property status the new receipt status
 */
data class MessageReceiptNotification(
    val chatRoomId: Long,
    val messageId: Long,
    val recipientId: Long,
    val status: String
)
