package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import java.time.Instant

data class GatheringImageDto(
    val id: Long,
    val gatheringId: Long,
    val url: String,
    val uploader: UserDto,
    val uploadedAt: Instant,
)
