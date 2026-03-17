package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.chat.MessageReceiptDto
import mk.ukim.finki.iskacamebackend.model.domain.MessageReceipt
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Mapper for converting [MessageReceipt] entities to DTOs.
 */
@Mapper(componentModel = "spring")
interface MessageReceiptMapper {

    /**
     * Maps a [MessageReceipt] entity to a [MessageReceiptDto].
     *
     * @param receipt the message receipt entity
     * @return mapped receipt dto
     */
    @Mapping(target = "recipientId", source = "recipient.id")
    fun toMessageReceiptDto(receipt: MessageReceipt): MessageReceiptDto
}