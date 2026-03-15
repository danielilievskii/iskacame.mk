package mk.ukim.finki.iskacamebackend.listener

import mk.ukim.finki.iskacamebackend.common.WebSocketDestinations
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageNotification
import mk.ukim.finki.iskacamebackend.dto.response.chat.MessageReceiptNotification
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
     * Broadcasts a new message to all chat room subscribers.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChatMessageSent(event: ChatMessageSentEvent) {

        val destination = WebSocketDestinations.chatTopic(event.chatRoomId)

        val payload = ChatMessageNotification(
            chatRoomId = event.chatRoomId,
            message = event.message
        )

        messagingTemplate.convertAndSend(destination, payload)
    }

    /**
     * Notifies chat room subscribers that a recipient has seen all messages.
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