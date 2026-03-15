package mk.ukim.finki.iskacamebackend.dto.response.chat

/**
 * WebSocket notification sent when a message receipt status is updated.
 *
 * Broadcast to clients subscribed to `/topic/chat.{chatRoomId}.receipts`.
 */
data class MessageReceiptNotification(
    val chatRoomId: Long,
    val messageId: Long,
    val recipientId: Long,
    val status: String
)
