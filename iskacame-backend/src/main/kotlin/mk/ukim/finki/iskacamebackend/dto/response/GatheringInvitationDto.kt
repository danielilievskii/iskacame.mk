package mk.ukim.finki.iskacamebackend.dto.response

import mk.ukim.finki.iskacamebackend.dto.UserDto
import java.time.Instant

data class GatheringInvitationDto(
    val id: Long,
    val gatheringCreator: UserDto,
    val gatheringTitle: String,
    val createdAt: Instant
)
