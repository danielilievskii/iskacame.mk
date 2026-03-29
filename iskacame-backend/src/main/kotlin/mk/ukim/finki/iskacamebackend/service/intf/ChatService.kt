package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.chat.SendMessageRequest
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.model.domain.ChatRoom
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import org.springframework.data.domain.Page

/**
 * Service responsible for managing gathering chat rooms and messages.
 */
interface ChatService {

    /**
     * Creates a new chat room for the given gathering.
     *
     * @param gathering the gathering entity for which to create a chat room
     * @return the newly created and persisted chat room
     */
    fun createChatRoom(gathering: Gathering): ChatRoom

    /**
     * Returns paginated messages for a chat room.
     * Messages include receipt information for each participant.
     *
     * @param chatRoomId id of the chat room
     * @param page page number
     * @param size page size
     * @return paginated messages
     */
    fun getChatRoomMessages(chatRoomId: Long, page: Int, size: Int): Page<ChatMessageDto>

    /**
     * Sends a message to the specified chat room and notifies all recipients.
     *
     * Creates a [MessageReceipt] with status [MessageReceiptStatus.SENT] for each
     * participant in the gathering, excluding the sender.
     *
     * Publishes a [ChatMessageSentEvent] after commit to broadcast the message
     * to all room subscribers.
     *
     * @param chatRoomId the ID of the chat room to send the message to
     * @param request the message content
     * @param senderId the ID of the user sending the message
     */
    fun sendMessage(chatRoomId: Long, request: SendMessageRequest, senderId: Long)

    /**
     * Soft-deletes a message.
     *
     * Publishes a [ChatMessageDeletedEvent] after commit to notify room subscribers.
     *
     * @param messageId the ID of the message to delete
     * @throws CustomAccessDeniedException if the current user is not the message sender
     * @throws ConflictException if the message is already deleted
     */
    fun deleteMessage(messageId: Long)

    /**
     * Marks all messages in the chat room as DELIVERED for the given user.
     * Typically called when the client connects or receives messages.
     *
     * @param chatRoomId id of the chat room
     */
    fun markChatRoomMessagesDelivered(chatRoomId: Long)

    /**
     * Marks all unseen messages in the chat room as [MessageReceiptStatus.SEEN] for the current user.
     * Typically called when the user opens the chat view.
     *
     * Publishes a [ChatMessagesSeenEvent] after commit to notify room subscribers
     * only if at least one receipt was updated.
     *
     * @param chatRoomId the ID of the chat room
     */
    fun markChatRoomMessagesSeen(chatRoomId: Long)
}