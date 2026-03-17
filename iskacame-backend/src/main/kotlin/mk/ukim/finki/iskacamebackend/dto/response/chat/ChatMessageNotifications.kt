package mk.ukim.finki.iskacamebackend.dto.response.chat

/**
 * Discriminator for chat room WebSocket notification types.
 */
enum class ChatNotificationType {
    MESSAGE_SENT,
    MESSAGE_DELETED
}

/**
 * Base class for WebSocket notifications related to chat activity.
 *
 * Notifications are broadcast to clients subscribed to `/topic/chat.{chatRoomId}.`.
 */
sealed class ChatNotification {
    abstract val chatRoomId: Long
    abstract val type: ChatNotificationType
}

/**
 * Notification broadcast when a new message is sent in a chat room.
 *
 * @property chatRoomId the ID of the chat room the message was posted in
 * @property type the notification type discriminator
 * @property message the full message payload
 */
data class ChatMessageSentNotification(
    override val chatRoomId: Long,
    override val type: ChatNotificationType = ChatNotificationType.MESSAGE_SENT,
    val message: ChatMessageDto
) : ChatNotification()

/**
 * Notification broadcast when a message is soft-deleted in a chat room.
 * Clients should remove or replace the message with a deleted placeholder.
 *
 * @property chatRoomId the ID of the chat room the message belongs to
 * @property type the notification type discriminator
 * @property messageId the ID of the deleted message
 */
data class ChatMessageDeletedNotification(
    override val chatRoomId: Long,
    override val type: ChatNotificationType = ChatNotificationType.MESSAGE_DELETED,
    val messageId: Long
) : ChatNotification()


