package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.model.domain.ChatMessage
import org.mapstruct.Mapper

@Mapper(
    componentModel = "spring",
    uses = [MessageReceiptMapper::class, UserMapper::class]
)
interface ChatMessageMapper {

    /**
     * Maps a [ChatMessage] entity to a [ChatMessageDto].
     * Automatically maps receipts using [MessageReceiptMapper].
     */
    fun toChatMessageDto(message: ChatMessage): ChatMessageDto
}
