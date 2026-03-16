package mk.ukim.finki.iskacamebackend.listener

import mk.ukim.finki.iskacamebackend.common.WebSocketDestinations
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDeletedNotification
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageSentNotification
import mk.ukim.finki.iskacamebackend.dto.response.chat.MessageReceiptNotification
import mk.ukim.finki.iskacamebackend.events.ChatMessageDeletedEvent
import mk.ukim.finki.iskacamebackend.events.ChatMessageSentEvent
import mk.ukim.finki.iskacamebackend.events.ChatMessagesSeenEvent
import mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Handles WebSocket push notifications for chat events.
 */
@Component
class ChatMessageEventListener(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * Broadcasts a [ChatMessageSentNotification] to all subscribers of the chat room topic.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChatMessageSent(event: ChatMessageSentEvent) {

        val destination = WebSocketDestinations.chatTopic(event.chatRoomId)

        val payload = ChatMessageSentNotification(
            chatRoomId = event.chatRoomId,
            message = event.message
        )

        messagingTemplate.convertAndSend(destination, payload)
    }

    /**
     * Broadcasts a [ChatMessageDeletedNotification] to all subscribers of the chat room topic.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChatMessageDeleted(event: ChatMessageDeletedEvent) {

        val destination = WebSocketDestinations.chatTopic(event.chatRoomId)

        val payload = ChatMessageDeletedNotification(
            chatRoomId = event.chatRoomId,
            messageId = event.messageId
        )

        messagingTemplate.convertAndSend(destination, payload)
    }

    /**
     * Broadcasts a [MessageReceiptNotification] to all subscribers of the chat room receipts topic.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChatMessagesSeen(event: ChatMessagesSeenEvent) {

        val destination = WebSocketDestinations.chatReceiptsTopic(event.chatRoomId)

        val payload = MessageReceiptNotification(
            chatRoomId = event.chatRoomId,
            messageId = -1,
            recipientId = event.recipientId,
            status = MessageReceiptStatus.SEEN.name
        )

        messagingTemplate.convertAndSend(destination, payload)
    }
}