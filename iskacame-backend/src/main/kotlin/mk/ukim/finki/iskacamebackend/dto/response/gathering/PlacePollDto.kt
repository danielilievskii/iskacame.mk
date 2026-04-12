package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.model.enums.PollStatus
import java.time.Instant

data class PlacePollDto(
    val id: Long,
    val status: PollStatus,
    val endsAt: Instant,
    val createdAt: Instant,
    val places: List<PlacePollOptionDto>,
    val myVotedPlaceIds: List<Long>
)

data class PlacePollOptionDto(
    val place: PlaceDto,
    val voteCount: Int
)
