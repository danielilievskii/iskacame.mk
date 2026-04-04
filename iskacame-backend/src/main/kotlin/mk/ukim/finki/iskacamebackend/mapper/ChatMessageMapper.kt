package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.model.domain.ChatMessage
import mk.ukim.finki.iskacamebackend.model.domain.User
import org.springframework.stereotype.Component

/**
* Custom mapper for converting [ChatMessage] entities into [ChatMessageDto].
*/
@Component
class ChatMessageMapper(
    private val userMapper: UserMapper
) {

    /**
     * Converts a [ChatMessage] entity into a [ChatMessageDto].
     *
     * @param message the chat message entity
     * @return mapped DTO
     */
    fun toChatMessageDto(message: ChatMessage, currentUser: User): ChatMessageDto {

        val userDto = userMapper.toUserDto(message.sender)
        val latestSeenBy = extractLatestSeenBy(message, currentUser)

        return ChatMessageDto(
            id = message.id!!,
            sender = userDto,
            content = message.content,
            sentAt = message.sentAt,
            latestSeenBy = latestSeenBy
        )
    }

    /**
     * Extracts IDs of users whose last seen message is this message.
     *
     * @param message chat message entity
     * @return list of user IDs
     */
    private fun extractLatestSeenBy(message: ChatMessage, currentUser: User): List<Long> {
        return message.receipts
            .mapNotNull { it.user.id }
            .filter { it != currentUser.id }

    }
}