package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.ChatExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.chat.SendMessageRequest
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.events.ChatMessageDeletedEvent
import mk.ukim.finki.iskacamebackend.events.ChatMessageSentEvent
import mk.ukim.finki.iskacamebackend.events.ChatMessagesSeenEvent
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.CustomAccessDeniedException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.ChatMessageMapper
import mk.ukim.finki.iskacamebackend.model.domain.*
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.ChatService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import mk.ukim.finki.iskacamebackend.websocket.WebSocketSessionRegistry
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ChatServiceImpl(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomReceiptRepository: ChatRoomReceiptRepository,
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val authService: AuthService,
    private val chatMessageMapper: ChatMessageMapper,
    private val userService: UserService,
    private val eventPublisher: ApplicationEventPublisher,
    private val webSocketSessionRegistry: WebSocketSessionRegistry
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

        val currentUser = authService.getCurrentUser()

        val pageable = PageRequest.of(page, size)
        val messagesPage = chatMessageRepository.findByChatRoomId(chatRoomId, pageable)

        return messagesPage.map { chatMessageMapper.toChatMessageDto(it, currentUser) }
    }

    override fun createReceiptForUser(chatRoom: ChatRoom, user: User): ChatRoomReceipt {

        val unseenMessagesCount = chatMessageRepository.countByChatRoomIdAndDeletedAtIsNull(chatRoom.id!!)

        val receipt = ChatRoomReceipt(
            chatRoom = chatRoom,
            user = user,
            lastSeenMessage = null,
            unseenMessagesCounter = unseenMessagesCount
        )

        return chatRoomReceiptRepository.save(receipt)
    }

    override fun deleteReceiptForUser(chatRoom: ChatRoom, user: User) {

        val receipt = chatRoomReceiptRepository.findByChatRoomIdAndUserId(chatRoom.id!!, user.id!!)
            ?: throw ResourceNotFoundException(ChatExceptionMessages.CHAT_ROOM_RECEIPT_NOT_FOUND)

        chatRoomReceiptRepository.delete(receipt)
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

        val receipt = chatRoomReceiptRepository.findByChatRoomIdAndUserId(chatRoomId, currentUser.id!!)
            ?: throw ResourceNotFoundException(ChatExceptionMessages.CHAT_ROOM_RECEIPT_NOT_FOUND)

        receipt.lastSeenMessage = null
        receipt.unseenMessagesCounter = 0

        chatRoomReceiptRepository.save(receipt)

        val chatMessageDto = chatMessageMapper.toChatMessageDto(savedMessage, currentUser)

        val recipientIds = gatheringParticipationRepository
            .findAllByGatheringIdAndStatus(chatRoom.gathering.id!!, ParticipationStatus.JOINED)
            .map { it.user.id!! }
            .filter { it != currentUser.id }

        val receipts = chatRoomReceiptRepository.findByChatRoomIdAndUserIdIn(chatRoomId, recipientIds)

        receipts.forEach { receipt ->
            receipt.unseenMessagesCounter = receipt.unseenMessagesCounter + 1
        }

        chatRoomReceiptRepository.saveAll(receipts)

        val offlineRecipientIds = recipientIds
            .filter { !webSocketSessionRegistry.isConnected(it) }

        val event = ChatMessageSentEvent(
            chatRoomId = chatRoomId,
            message = chatMessageDto,
            offlineRecipientIds = offlineRecipientIds
        )
        eventPublisher.publishEvent(event)
    }

    @Transactional
    override fun deleteMessage(messageId: Long) {

        val message = findChatMessageById(messageId)
        val currentUserId = authService.getCurrentUserId()

        if (message.sender.id != currentUserId) {
            throw CustomAccessDeniedException(AuthExceptionMessages.ACCESS_DENIED)
        }

        if (message.deletedAt != null) {
            throw ConflictException(ChatExceptionMessages.CHAT_MESSAGE_ALREADY_DELETED)
        }

        message.deletedAt = LocalDateTime.now()
        chatMessageRepository.save(message)

        val receipts =
            chatRoomReceiptRepository.findByChatRoomIdAndUserIdNot(message.chatRoom.id!!, currentUserId)

        receipts.forEach { receipt ->
            if (receipt.unseenMessagesCounter > 0) {
                receipt.unseenMessagesCounter = receipt.unseenMessagesCounter - 1
            }
        }
        chatRoomReceiptRepository.saveAll(receipts)

        val event = ChatMessageDeletedEvent(
            chatRoomId = message.chatRoom.id!!,
            messageId = messageId
        )
        eventPublisher.publishEvent(event)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipantByChatRoom(#chatRoomId, authentication.principal.id)")
    override fun markChatRoomMessagesSeen(chatRoomId: Long) {

        val currentUser = authService.getCurrentUser()
        val chatRoom = findChatRoomById(chatRoomId)

        val receipt = chatRoomReceiptRepository.findByChatRoomIdAndUserId(chatRoomId, currentUser.id!!)
            ?: throw ResourceNotFoundException(ChatExceptionMessages.CHAT_ROOM_RECEIPT_NOT_FOUND)

        val lastMessage = chatMessageRepository.findFirstByChatRoomIdOrderBySentAtDesc(chatRoomId)

        lastMessage?.let {
            if (receipt.lastSeenMessage != it) {
                receipt.unseenMessagesCounter = 0

                if (lastMessage.sender != currentUser) {
                    receipt.lastSeenMessage = lastMessage
                }
            }
        }

        chatRoomReceiptRepository.save(receipt)

        val event = ChatMessagesSeenEvent(
            chatRoomId = chatRoomId,
            recipientId = currentUser.id!!,
        )

        eventPublisher.publishEvent(event)
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