package mk.ukim.finki.iskacamebackend.dto.response.chat

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus
import java.time.LocalDateTime

data class ChatMessageDto(
    val id: Long,
    val sender: UserDto,
    val content: String,
    val sentAt: LocalDateTime,
    val receipts: List<MessageReceiptDto>
)

data class MessageReceiptDto(
    val recipientId: Long,
    val status: MessageReceiptStatus,
    val deliveredAt: LocalDateTime?,
    val seenAt: LocalDateTime?
)