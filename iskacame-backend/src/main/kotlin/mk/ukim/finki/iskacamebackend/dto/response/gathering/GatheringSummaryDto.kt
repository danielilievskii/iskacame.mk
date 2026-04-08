package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import java.time.Instant
import java.time.LocalDateTime

data class GatheringSummaryDto(
    val id: Long,
    val creator: UserDto,
    val title: String,
    val status: GatheringStatus,
    val createdAt: Instant,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val finalizedTime: LocalDateTime?,
    val chatRoomId: Long,
    val unseenMessagesCount: Long?
)
