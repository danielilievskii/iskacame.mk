package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.ChatExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.chat.SendMessageRequest
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.events.ChatMessageSentEvent
import mk.ukim.finki.iskacamebackend.events.ChatMessagesSeenEvent
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.ChatMessageMapper
import mk.ukim.finki.iskacamebackend.model.domain.*
import mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.ChatService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatServiceImpl(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val messageReceiptRepository: MessageReceiptRepository,
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val authService: AuthService,
    private val chatMessageMapper: ChatMessageMapper,
    private val userService: UserService,
    private val eventPublisher: ApplicationEventPublisher
) : ChatService {

    override fun createChatRoom(gathering: Gathering): ChatRoom {

        val chatRoom = ChatRoom(gathering = gathering)
        val savedChatRoom = chatRoomRepository.save(chatRoom)
        gathering.chatRoom = savedChatRoom

        return savedChatRoom
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissionService.isGatheringParticipantByChatRoom(#chatRoomId, authentication.principal.id)")
    override fun getChatRoomMessages(chatRoomId: Long, page: Int, size: Int): Page<ChatMessageDto> {

        val pageable = PageRequest.of(page, size)
        val messagesPage = chatMessageRepository.findByChatRoomId(chatRoomId, pageable)

        return messagesPage.map { chatMessageMapper.toChatMessageDto(it) }
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipantByChatRoom(#chatRoomId, #senderId)")
    override fun sendMessage(chatRoomId: Long, request: SendMessageRequest, senderId: Long) {

        val chatRoom = findChatRoomById(chatRoomId)
        val currentUser = userService.getUserById(senderId)

        val message = ChatMessage(
            chatRoom = chatRoom,
            sender = currentUser,
            content = request.content
        )

        val savedMessage = chatMessageRepository.save(message)

        val recipients = gatheringParticipationRepository
            .findAllByGatheringIdAndStatus(chatRoom.gathering.id!!, ParticipationStatus.JOINED)
            .map { it.user }
            .filter { it.id != currentUser.id }

        val receipts = recipients.map {
            MessageReceipt(
                message = savedMessage,
                recipient = it,
                status = MessageReceiptStatus.SENT
            )
        }

        messageReceiptRepository.saveAll(receipts)

        val chatMessageDto = chatMessageMapper.toChatMessageDto(savedMessage)

        val event = ChatMessageSentEvent(
            chatRoomId = chatRoomId,
            message = chatMessageDto
        )
        eventPublisher.publishEvent(event)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipantByChatRoom(#chatRoomId, authentication.principal.id)")
    override fun markChatRoomMessagesDelivered(chatRoomId: Long) {

        val currentUserId = authService.getCurrentUserId()
        messageReceiptRepository.markDeliveredForChatRoom(chatRoomId, currentUserId)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipantByChatRoom(#chatRoomId, authentication.principal.id)")
    override fun markChatRoomMessagesSeen(chatRoomId: Long) {

        val currentUserId = authService.getCurrentUserId()
        val updatedReceipts = messageReceiptRepository.markSeenForChatRoom(chatRoomId, currentUserId)

        if (updatedReceipts > 0) {
            val event = ChatMessagesSeenEvent(
                chatRoomId = chatRoomId,
                recipientId = currentUserId
            )

            eventPublisher.publishEvent(event)
        }
    }

    private fun findChatRoomById(id: Long): ChatRoom {
        return chatRoomRepository.findById(id)
            .orElseThrow { ResourceNotFoundException(ChatExceptionMessages.CHAT_ROOM_NOT_FOUND) }
    }

    private fun findChatMessageById(id: Long): ChatMessage {
        return chatMessageRepository.findById(id)
            .orElseThrow { ResourceNotFoundException(ChatExceptionMessages.CHAT_MESSAGE_NOT_FOUND) }
    }
}