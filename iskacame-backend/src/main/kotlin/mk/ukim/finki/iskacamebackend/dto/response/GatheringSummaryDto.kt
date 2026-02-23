package mk.ukim.finki.iskacamebackend.dto.response

import mk.ukim.finki.iskacamebackend.dto.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import java.time.Instant

data class GatheringSummaryDto(
    val id: Long,
    val creator: UserDto,
    val title: String,
    val status: GatheringStatus,
    val createdAt: Instant
)
