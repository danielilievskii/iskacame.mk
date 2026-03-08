package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import java.time.Instant

data class GatheringInvitationDto(
    val id: Long,
    val gatheringCreator: UserDto,
    val gatheringTitle: String,
    val createdAt: Instant
)
