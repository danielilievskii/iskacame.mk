package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.chat.SendMessageRequest
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.model.domain.ChatRoom
import mk.ukim.finki.iskacamebackend.model.domain.ChatRoomReceipt
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.domain.User
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
     * Creates a [ChatRoomReceipt] for the given user in the specified chat room.
     *
     * Sets [unseenMessagesCounter] to the current number of non-deleted messages in the room,
     * and [lastSeenMessage] to null, assuming the user has not seen any messages yet.
     *
     * @param chatRoom the chat room to create the receipt for
     * @param user the user to create the receipt for
     * @return the newly created [ChatRoomReceipt]
     */
    fun createReceiptForUser(chatRoom: ChatRoom, user: User): ChatRoomReceipt

    /**
     * Deletes the [ChatRoomReceipt] for the given user in the specified chat room.
     *
     * @param chatRoom the chat room to delete the receipt from
     * @param user the user whose receipt should be deleted
     * @throws ResourceNotFoundException if no receipt is found for the user in the chat room
     */
    fun deleteReceiptForUser(chatRoom: ChatRoom, user: User)

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
     * Sends a message to the specified chat room.
     *
     * After saving the message, updates receipts as follows:
     * - Sender's receipt: sets [lastSeenMessage] to the new message and resets [unseenMessagesCounter] to 0.
     * - All other participants' receipts: increments [unseenMessagesCounter] by 1.
     *
     * Publishes a [ChatMessageSentEvent] after commit to broadcast the message to all room subscribers,
     * including the list of offline recipient IDs for push notification handling.
     *
     * @param chatRoomId the ID of the chat room to send the message to
     * @param request the message content
     * @param senderId the ID of the user sending the message
     * @throws ResourceNotFoundException if the sender's chat room receipt is not found
     */
    fun sendMessage(chatRoomId: Long, request: SendMessageRequest, senderId: Long)

    /**
     * Soft-deletes a message by setting its [deletedAt] timestamp.
     *
     * Also adjusts all other participants' receipts in the chat room:
     * - Decrements [unseenMessagesCounter] if the deleted message was unseen by that participant.
     * - Updates [lastSeenMessage] to the previous non-deleted message if the deleted message was their last seen.
     *
     * Publishes a [ChatMessageDeletedEvent] after commit to notify room subscribers.
     *
     * @param messageId the ID of the message to delete
     * @throws CustomAccessDeniedException if the current user is not the message sender
     * @throws ConflictException if the message is already deleted
     */
    fun deleteMessage(messageId: Long)

    /**
     * Marks the chat room as fully seen for the current user by updating their receipt
     * to point to the latest message and resetting the unseen message counter.
     * Typically called when the user opens the chat view.
     *
     * Publishes a [ChatMessagesSeenEvent] after commit to notify room subscribers
     * only if the last message was not already marked as seen.
     *
     * @param chatRoomId the ID of the chat room
     */
    fun markChatRoomMessagesSeen(chatRoomId: Long)
}