package mk.ukim.finki.iskacamebackend.assembler

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.mapper.GatheringMapper
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.repository.ChatMessageRepository
import org.springframework.stereotype.Component

/**
 * Assembler responsible for constructing [GatheringSummaryDto] instances.
 */
@Component
class GatheringSummaryAssembler(
    private val gatheringMapper: GatheringMapper,
    private val chatMessageRepository: ChatMessageRepository,
) {

    /**
     * Assembles a [GatheringSummaryDto] from a [Gathering] entity,
     * including unseen message count
     *
     * @param gathering the gathering entity to construct the DTO from
     * @return fully populated [GatheringSummaryDto]
     */
    fun assembleAll(gatherings: List<Gathering>, currentUserId: Long): List<GatheringSummaryDto> {

        val chatRoomIds = gatherings.map { it.chatRoom!!.id!! }

        val unseenMessagesCountByChatRoom = chatMessageRepository
            .countUnseenForUserGrouped(chatRoomIds, currentUserId)
            .associateBy(
                { it.chatRoomId },
                { it.unseenMessagesCount }
            )

        return gatherings.map { gathering ->
            val unseenMessagesCount = unseenMessagesCountByChatRoom[gathering.chatRoom!!.id] ?: 0

            gatheringMapper
                .toGatheringSummaryDto(gathering)
                .copy(unseenMessagesCount = unseenMessagesCount)
        }
    }
}