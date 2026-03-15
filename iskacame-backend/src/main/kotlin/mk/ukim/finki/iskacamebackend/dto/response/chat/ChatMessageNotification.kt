package mk.ukim.finki.iskacamebackend.dto.response.chat

/**
 * WebSocket notification sent when a new message is posted in a chat room.
 *
 * Broadcast to clients subscribed to `/topic/chat.{chatRoomId}`.
 */
data class ChatMessageNotification(
    val chatRoomId: Long,
    val message: ChatMessageDto
)


