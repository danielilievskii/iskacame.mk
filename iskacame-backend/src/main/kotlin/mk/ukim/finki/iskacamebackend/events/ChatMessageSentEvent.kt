package mk.ukim.finki.iskacamebackend.events

import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto

data class ChatMessageSentEvent(
    val chatRoomId: Long,
    val message: ChatMessageDto
)