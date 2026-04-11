package mk.ukim.finki.iskacamebackend.dto.response.chat

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus
import java.time.LocalDateTime

data class ChatMessageDto(
    val id: Long,
    val sender: UserDto,
    val content: String,
    val sentAt: LocalDateTime,
    val latestSeenBy: List<Long>
)

